package com.recordlabs.desktop.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors

enum class ListenTogetherConnection { Disconnected, Connecting, Connected, InRoom, Error }
data class ListenTogetherJoinRequest(val userId: String, val username: String)
data class ListenTogetherPlaybackSync(
    val action: String,
    val track: Track? = null,
    val trackId: String? = null,
    val positionMs: Long = 0L,
    val serverTimeMs: Long = 0L,
    val queue: List<Track> = emptyList(),
    val queueTitle: String? = null,
    val sequence: Long = 0L,
)
data class ListenTogetherTrackSuggestion(val suggestionId: String, val username: String, val track: Track)
data class ListenTogetherUser(val userId: String, val username: String, val isHost: Boolean, val isConnected: Boolean = true)
data class ListenTogetherGuestCommand(val action: String, val sequence: Long)

/** First Windows implementation of Android's MetroServer protobuf protocol. */
object WindowsListenTogetherClient {
    // Record Labs' own server (Render): the Meowery server only lets Metrolist host rooms.
    const val serverUrl = "wss://record-labs-listen-server.onrender.com/ws"
    // Generous timeouts: a sleeping free server takes up to a minute to wake.
    private val http = OkHttpClient.Builder()
        // Same rhythm as the server's own pings: a slow pong no longer ends the session.
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(70, TimeUnit.SECONDS)
        .build()
    private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "recordlabs-listen-reconnect").apply { isDaemon = true }
    }
    // Only the newest socket's events count: replacing a socket (cancel) used
    // to report a failure that scheduled a reconnect, which then cancelled the
    // new socket — the endless disconnect/connecting loop.
    @Volatile private var socket: WebSocket? = null
    @Volatile private var pendingMessage: ByteArray? = null
    @Volatile private var sessionToken = ""
    @Volatile private var reconnectAttempt = 0
    @Volatile private var intentionalClose = false
    @Volatile private var reconnectTask: java.util.concurrent.ScheduledFuture<*>? = null
    @Volatile private var reconnectingSince = 0L
    @Volatile private var lastRoomCode: String? = null
    @Volatile private var wasHost = false
    @Volatile private var rejoinAttempted = false
    // Keeps the connection busy like the Android app does (every 25 s), so
    // proxies in front of the server never see it as idle.
    private val keepAlive = reconnectExecutor.scheduleWithFixedDelay({
        val current = socket
        if (current != null && _connection.value == ListenTogetherConnection.InRoom) {
            runCatching { current.send(ByteString.of(*envelope("ping", byteArrayOf()))) }
        }
    }, 25, 25, TimeUnit.SECONDS)

    private val _connection = MutableStateFlow(ListenTogetherConnection.Disconnected)
    val connection = _connection.asStateFlow()
    private val _roomCode = MutableStateFlow<String?>(null)
    val roomCode = _roomCode.asStateFlow()
    private val _message = MutableStateFlow("Not connected")
    val message = _message.asStateFlow()
    private val _isHost = MutableStateFlow(false)
    val isHost = _isHost.asStateFlow()
    private val _pendingJoinRequests = MutableStateFlow<List<ListenTogetherJoinRequest>>(emptyList())
    val pendingJoinRequests = _pendingJoinRequests.asStateFlow()
    private val _playbackSync = MutableStateFlow<ListenTogetherPlaybackSync?>(null)
    val playbackSync = _playbackSync.asStateFlow()
    private var syncSequence = 0L
    private val _pendingSuggestions = MutableStateFlow<List<ListenTogetherTrackSuggestion>>(emptyList())
    val pendingSuggestions = _pendingSuggestions.asStateFlow()
    private val _users = MutableStateFlow<List<ListenTogetherUser>>(emptyList())
    val users = _users.asStateFlow()
    private val _bufferingUsers = MutableStateFlow<List<String>>(emptyList())
    val bufferingUsers = _bufferingUsers.asStateFlow()
    private val _guestCommand = MutableStateFlow<ListenTogetherGuestCommand?>(null)
    val guestCommand = _guestCommand.asStateFlow()
    private var guestCommandSequence = 0L
    private var localUsername = ""
    private var localUserId = ""
    private var lastRemoteTrack: Track? = null
    private var lastRemoteQueue: List<Track> = emptyList()

    fun createRoom(username: String) {
        if (username.isBlank()) return
        localUsername = username.trim()
        intentionalClose = false
        // A new room never continues an old (lost) session.
        clearSession()
        connect(envelope("create_room", protoMessage(1 to username.trim())))
    }

    fun joinRoom(roomCode: String, username: String) {
        if (roomCode.isBlank() || username.isBlank()) return
        localUsername = username.trim()
        intentionalClose = false
        clearSession()
        rejoinAttempted = false
        connect(envelope("join_room", protoMessage(1 to roomCode.trim().uppercase(), 2 to username.trim())))
    }

    fun leave() {
        intentionalClose = true
        val current = socket
        socket = null
        current?.send(ByteString.of(*envelope("leave_room", byteArrayOf())))
        current?.close(1000, "Leaving room")
        clearSession()
        lastRoomCode = null
        _connection.value = ListenTogetherConnection.Disconnected
        _message.value = "Disconnected"
    }

    /** Forgets the room and session (and any reconnect waiting to run). */
    private fun clearSession() {
        reconnectTask?.cancel(false)
        reconnectTask = null
        pendingMessage = null
        _roomCode.value = null
        _isHost.value = false
        _pendingJoinRequests.value = emptyList()
        _pendingSuggestions.value = emptyList()
        _users.value = emptyList()
        _bufferingUsers.value = emptyList()
        localUserId = ""
        sessionToken = ""
        reconnectAttempt = 0
        reconnectingSince = 0L
        lastRemoteTrack = null
        lastRemoteQueue = emptyList()
    }

    /**
     * The server no longer has this session (it expired, or the server
     * restarted). A listener asks to join the same room again once; a host's
     * room is gone, so the page offers to create a new one.
     */
    private fun endLostSession(reason: String) {
        val roomCode = lastRoomCode
        val rejoin = !wasHost && roomCode != null && localUsername.isNotBlank() && !rejoinAttempted
        intentionalClose = true
        val current = socket
        socket = null
        current?.close(1000, "Session ended")
        clearSession()
        if (rejoin) {
            rejoinAttempted = true
            intentionalClose = false
            _message.value = "Rejoining room $roomCode…"
            connect(envelope("join_room", protoMessage(1 to roomCode!!, 2 to localUsername)))
            return
        }
        lastRoomCode = null
        _connection.value = ListenTogetherConnection.Disconnected
        _message.value = reason
    }

    fun approveJoin(userId: String) {
        if (userId.isBlank()) return
        socket?.send(ByteString.of(*envelope("approve_join", protoMessage(1 to userId))))
        _pendingJoinRequests.value = _pendingJoinRequests.value.filterNot { it.userId == userId }
        _message.value = "Join request approved"
    }

    fun rejectJoin(userId: String) {
        if (userId.isBlank()) return
        socket?.send(ByteString.of(*envelope("reject_join", protoMessage(1 to userId))))
        _pendingJoinRequests.value = _pendingJoinRequests.value.filterNot { it.userId == userId }
        _message.value = "Join request rejected"
    }

    fun broadcastPlayback(
        action: String,
        track: Track? = null,
        positionMs: Long = 0L,
        queue: List<Track> = emptyList(),
        queueTitle: String? = null,
    ) {
        if (_connection.value != ListenTogetherConnection.InRoom || !_isHost.value) return
        val fields = buildList<ByteArray> {
            add(wireBytes(1, action.toByteArray()))
            track?.id?.takeIf(String::isNotBlank)?.let { add(wireBytes(2, it.toByteArray())) }
            add(wireVarInt(3, positionMs))
            track?.let { add(wireBytes(4, trackPayload(it))) }
            queue.forEach { add(wireBytes(6, trackPayload(it))) }
            queueTitle?.takeIf(String::isNotBlank)?.let { add(wireBytes(7, it.toByteArray())) }
            add(wireVarInt(9, System.currentTimeMillis()))
        }
        socket?.send(ByteString.of(*envelope("playback_action", fields.joinBytes())))
    }

    fun sendBufferReady(trackId: String) {
        if (_connection.value != ListenTogetherConnection.InRoom || trackId.isBlank()) return
        socket?.send(ByteString.of(*envelope("buffer_ready", protoMessage(1 to trackId.trim()))))
        _message.value = "Ready, waiting for everyone…"
    }

    fun kickUser(userId: String) {
        if (!_isHost.value || userId.isBlank() || userId == localUserId) return
        socket?.send(ByteString.of(*envelope("kick_user", protoMessage(1 to userId))))
    }

    fun transferHost(userId: String) {
        if (!_isHost.value || userId.isBlank() || userId == localUserId) return
        socket?.send(ByteString.of(*envelope("transfer_host", protoMessage(1 to userId))))
    }

    fun requestSync() {
        if (_connection.value == ListenTogetherConnection.InRoom) {
            socket?.send(ByteString.of(*envelope("request_sync", byteArrayOf())))
        }
    }

    fun suggestTrack(track: Track) {
        if (_connection.value != ListenTogetherConnection.InRoom || _isHost.value) return
        socket?.send(ByteString.of(*envelope("suggest_track", protoBytes(1 to trackPayload(track)))))
    }

    fun sendGuestCommand(action: String) {
        if (_connection.value != ListenTogetherConnection.InRoom || _isHost.value) return
        suggestTrack(Track(id = "GUEST_CMD:$action", title = "Guest Command", artist = ""))
    }

    fun approveSuggestion(suggestionId: String) {
        socket?.send(ByteString.of(*envelope("approve_suggestion", protoMessage(1 to suggestionId))))
        _pendingSuggestions.value = _pendingSuggestions.value.filterNot { it.suggestionId == suggestionId }
    }

    fun rejectSuggestion(suggestionId: String) {
        socket?.send(ByteString.of(*envelope("reject_suggestion", protoMessage(1 to suggestionId))))
        _pendingSuggestions.value = _pendingSuggestions.value.filterNot { it.suggestionId == suggestionId }
    }

    private fun connect(message: ByteArray) {
        reconnectTask?.cancel(false)
        reconnectTask = null
        pendingMessage = message
        val previous = socket
        socket = null
        previous?.cancel()
        _connection.value = ListenTogetherConnection.Connecting
        if (reconnectingSince == 0L) _message.value = "Connecting to Listen Together…"
        socket = http.newWebSocket(
            Request.Builder().url(serverUrl).header("User-Agent", "RecordLabs-Windows/0.1.1")
                .apply { ListenTogetherAuth.token()?.let { header(ListenTogetherAuth.HEADER, it) } }
                .build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (webSocket !== socket) return
                    _connection.value = ListenTogetherConnection.Connected
                    _message.value = "Connected, waiting for room…"
                    
                    val cap = wireVarInt(1, 1L) + wireBytes(3, "RecordLabs-Windows/0.1.1".toByteArray())
                    webSocket.send(ByteString.of(*envelope("client_capabilities", cap)))
                    
                    pendingMessage?.let { webSocket.send(ByteString.of(*it)) }
                    pendingMessage = null
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    if (webSocket !== socket) return
                    handleEnvelope(bytes.toByteArray())
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (webSocket !== socket) return
                    if (!intentionalClose && sessionToken.isNotBlank()) {
                        scheduleReconnect()
                    } else {
                        _connection.value = ListenTogetherConnection.Error
                        _message.value = t.message ?: "Listen Together connection failed"
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    if (webSocket === socket) webSocket.close(1000, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (webSocket !== socket) return
                    if (!intentionalClose && sessionToken.isNotBlank()) {
                        scheduleReconnect()
                    } else if (_connection.value != ListenTogetherConnection.Error) {
                        _connection.value = ListenTogetherConnection.Disconnected
                        _message.value = reason.ifBlank { "Disconnected" }
                    }
                }
            },
        )
    }

    private fun scheduleReconnect() {
        if (intentionalClose || sessionToken.isBlank()) return
        val now = System.currentTimeMillis()
        if (reconnectingSince == 0L) reconnectingSince = now
        // The server keeps a lost session for 15 minutes.
        if (now - reconnectingSince > 15L * 60L * 1000L) {
            endLostSession("Connection lost for too long. The room has ended.")
            return
        }
        _connection.value = ListenTogetherConnection.Connecting
        _message.value = "Connection lost, reconnecting…"
        val delaySeconds = (1L shl reconnectAttempt.coerceAtMost(4)).coerceAtMost(15L)
        reconnectAttempt++
        reconnectTask?.cancel(false)
        val token = sessionToken
        reconnectTask = reconnectExecutor.schedule({
            if (!intentionalClose && token.isNotBlank() && sessionToken == token) {
                connect(envelope("reconnect", protoMessage(1 to token)))
            }
        }, delaySeconds, TimeUnit.SECONDS)
    }

    private fun handleEnvelope(raw: ByteArray) {
        val fields = parseFields(raw)
        val type = fields[1]?.toString(Charsets.UTF_8).orEmpty()
        var payload = fields[2] ?: byteArrayOf()
        val compressed = fields[3]?.firstOrNull()?.toInt() == 1
        if (compressed) payload = runCatching { GZIPInputStream(ByteArrayInputStream(payload)).readBytes() }.getOrDefault(payload)
        val payloadFields = parseFields(payload)
        when (type) {
            "room_created", "join_approved", "reconnected" -> {
                _roomCode.value = payloadFields[1]?.toString(Charsets.UTF_8)
                _connection.value = ListenTogetherConnection.InRoom
                reconnectAttempt = 0
                reconnectingSince = 0L
                _roomCode.value?.takeIf(String::isNotBlank)?.let { lastRoomCode = it }
                _message.value = if (type == "room_created") "Room created" else "Joined room"
                _isHost.value = type == "room_created"
                val userId = payloadFields[2]?.toString(Charsets.UTF_8).orEmpty()
                localUserId = userId
                if (type != "reconnected") {
                    payloadFields[3]?.toString(Charsets.UTF_8)?.takeIf(String::isNotBlank)?.let { sessionToken = it }
                }
                if (userId.isNotBlank()) {
                    _users.value = listOf(ListenTogetherUser(userId, localUsername.ifBlank { "You" }, type == "room_created"))
                }
                val stateField = when (type) {
                    "join_approved" -> payloadFields[4]
                    "reconnected" -> payloadFields[3]
                    else -> null
                }
                stateField?.let(::applyRoomState)
                if (type == "reconnected") {
                    _isHost.value = payloadFields[4]?.let(::decodeVarInt) == 1L
                    updateHostFlags()
                    _message.value = "Reconnected"
                }
                wasHost = _isHost.value
                if (type != "room_created") requestSync()
            }
            "join_request" -> {
                val userId = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                val username = payloadFields[2]?.toString(Charsets.UTF_8).orEmpty()
                if (userId.isNotBlank()) {
                    // Listeners blocked in Listen Together settings are turned down at once.
                    if (AppSettings.isListenerBlocked(username)) {
                        rejectJoin(userId)
                        return
                    }
                    if (AppSettings.listenAutoApproveJoins.value) {
                        approveJoin(userId)
                        return
                    }
                    _pendingJoinRequests.value = (_pendingJoinRequests.value + ListenTogetherJoinRequest(userId, username.ifBlank { "A listener" }))
                        .distinctBy(ListenTogetherJoinRequest::userId)
                    _message.value = "Approval needed for $username"
                }
            }
            "sync_playback" -> {
                val fieldList = parseFieldList(payload)
                val action = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                val trackId = payloadFields[2]?.toString(Charsets.UTF_8)
                val position = payloadFields[3]?.let(::decodeVarInt) ?: 0L
                val track = payloadFields[4]?.let(::trackFromPayload)
                val serverTime = payloadFields[9]?.let(::decodeVarInt) ?: 0L
                val queue = fieldList.filter { it.first == 6 }.mapNotNull { trackFromPayload(it.second) }
                val queueTitle = payloadFields[7]?.toString(Charsets.UTF_8)
                if (track != null) lastRemoteTrack = track
                if (queue.isNotEmpty()) lastRemoteQueue = queue
                val effectiveTrack = track ?: lastRemoteTrack?.takeIf { trackId == null || it.id == trackId }
                _playbackSync.value = ListenTogetherPlaybackSync(
                    action, effectiveTrack, trackId ?: effectiveTrack?.id, position, serverTime,
                    queue.ifEmpty { lastRemoteQueue }, queueTitle, ++syncSequence,
                )
            }
            "sync_state" -> {
                val fieldList = parseFieldList(payload)
                val track = payloadFields[1]?.let(::trackFromPayload)
                val playing = payloadFields[2]?.let(::decodeVarInt) == 1L
                val position = payloadFields[3]?.let(::decodeVarInt) ?: 0L
                val lastUpdate = payloadFields[4]?.let(::decodeVarInt) ?: 0L
                val queue = fieldList.filter { it.first == 5 }.mapNotNull { trackFromPayload(it.second) }
                if (track != null) lastRemoteTrack = track
                if (queue.isNotEmpty()) lastRemoteQueue = queue
                _playbackSync.value = ListenTogetherPlaybackSync(
                    if (playing) "play" else "pause", track, track?.id, position, lastUpdate, queue, null, ++syncSequence,
                )
            }
            "suggestion_received" -> {
                val suggestionId = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                val username = payloadFields[3]?.toString(Charsets.UTF_8).orEmpty()
                val track = payloadFields[4]?.let(::trackFromPayload)
                if (suggestionId.isNotBlank() && track != null) {
                    if (track.id.startsWith("GUEST_CMD:")) {
                        rejectSuggestion(suggestionId)
                        _guestCommand.value = ListenTogetherGuestCommand(track.id.removePrefix("GUEST_CMD:"), ++guestCommandSequence)
                        return
                    }
                    if (AppSettings.listenAutoApproveSuggestions.value) {
                        approveSuggestion(suggestionId)
                        return
                    }
                    _pendingSuggestions.value = (_pendingSuggestions.value + ListenTogetherTrackSuggestion(suggestionId, username, track))
                }
            }
            "user_joined", "user_reconnected" -> {
                val userId = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                val username = payloadFields[2]?.toString(Charsets.UTF_8).orEmpty()
                if (userId.isNotBlank()) {
                    _users.value = (_users.value.filterNot { it.userId == userId } + ListenTogetherUser(userId, username, false, true))
                }
            }
            "user_left" -> {
                val userId = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                _users.value = _users.value.filterNot { it.userId == userId }
            }
            "user_disconnected" -> {
                val userId = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                _users.value = _users.value.map { if (it.userId == userId) it.copy(isConnected = false) else it }
            }
            "buffer_wait" -> {
                val fieldsList = parseFieldList(payload)
                _bufferingUsers.value = fieldsList.filter { it.first == 2 }.map { it.second.toString(Charsets.UTF_8) }
                _message.value = "Waiting for ${_bufferingUsers.value.size} listener(s)…"
            }
            "buffer_complete" -> {
                _bufferingUsers.value = emptyList()
                _message.value = "Everyone is ready"
            }
            "host_changed" -> {
                val newHostId = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                _isHost.value = newHostId == localUserId
                wasHost = _isHost.value
                _users.value = _users.value.map { it.copy(isHost = it.userId == newHostId) }
                _message.value = if (_isHost.value) "You are now the host" else "Host changed"
            }
            "kicked" -> {
                _message.value = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty().ifBlank { "Removed from room" }
                _roomCode.value = null
                _isHost.value = false
                _users.value = emptyList()
                // Removed from the room: nothing left to reconnect to.
                sessionToken = ""
                lastRoomCode = null
                _connection.value = ListenTogetherConnection.Connected
            }
            "join_rejected" -> {
                _connection.value = ListenTogetherConnection.Error
                _message.value = payloadFields[1]?.toString(Charsets.UTF_8) ?: "Join request rejected"
            }
            "error" -> {
                val code = payloadFields[1]?.toString(Charsets.UTF_8).orEmpty()
                val text = payloadFields[2]?.toString(Charsets.UTF_8) ?: "Server error"
                when {
                    // The session (or its room) is gone on the server.
                    code in lostSessionErrors && (sessionToken.isNotBlank() || _connection.value == ListenTogetherConnection.InRoom) ->
                        endLostSession(if (wasHost) "The room ended (the server lost it). Create a new room." else "The room has ended.")
                    // While in a room, other errors answer one message (a stale
                    // track, too many messages…): the room carries on, as on Android.
                    _connection.value == ListenTogetherConnection.InRoom -> _message.value = text
                    // Creating or joining failed.
                    else -> {
                        _connection.value = ListenTogetherConnection.Error
                        _message.value = text
                    }
                }
            }
            "pong" -> Unit
        }
    }

    private val lostSessionErrors = setOf("session_not_found", "session_expired", "room_not_found", "room_invalid")

    private fun applyRoomState(payload: ByteArray) {
        val fields = parseFieldList(payload)
        val hostId = fields.lastOrNull { it.first == 2 }?.second?.toString(Charsets.UTF_8).orEmpty()
        val parsedUsers = fields.filter { it.first == 3 }.mapNotNull { (_, bytes) ->
            val userFields = parseFields(bytes)
            val id = userFields[1]?.toString(Charsets.UTF_8).orEmpty()
            if (id.isBlank()) null else ListenTogetherUser(
                userId = id,
                username = userFields[2]?.toString(Charsets.UTF_8).orEmpty().ifBlank { "Listener" },
                isHost = userFields[3]?.let(::decodeVarInt) == 1L || id == hostId,
                isConnected = userFields[4]?.let(::decodeVarInt) != 0L,
            )
        }
        if (parsedUsers.isNotEmpty()) _users.value = parsedUsers
        _isHost.value = hostId.isNotBlank() && hostId == localUserId
        updateHostFlags()
    }

    private fun updateHostFlags() {
        if (_isHost.value && localUserId.isNotBlank()) {
            _users.value = _users.value.map { it.copy(isHost = it.userId == localUserId) }
        }
    }

    private fun envelope(type: String, payload: ByteArray): ByteArray =
        wireBytes(1, type.toByteArray()) + wireBytes(2, payload)

    private fun protoMessage(vararg fields: Pair<Int, String>): ByteArray =
        protoBytes(*fields.map { it.first to it.second.toByteArray() }.toTypedArray())

    private fun protoBytes(vararg fields: Pair<Int, ByteArray>): ByteArray = buildList<Byte> {
        fields.forEach { (number, value) ->
            addAll(wireBytes(number, value).toList())
        }
    }.toByteArray()

    private fun wireBytes(number: Int, value: ByteArray): ByteArray =
        varInt(number shl 3 or 2) + varInt(value.size) + value

    private fun wireVarInt(number: Int, value: Long): ByteArray =
        varInt(number shl 3) + varIntLong(value)

    private fun List<ByteArray>.joinBytes(): ByteArray = fold(byteArrayOf()) { result, bytes -> result + bytes }

    private fun varInt(value: Int): ByteArray {
        var remaining = value
        val output = mutableListOf<Byte>()
        do {
            var next = remaining and 0x7f
            remaining = remaining ushr 7
            if (remaining != 0) next = next or 0x80
            output += next.toByte()
        } while (remaining != 0)
        return output.toByteArray()
    }

    private fun varIntLong(value: Long): ByteArray {
        var remaining = value.coerceAtLeast(0L)
        val output = mutableListOf<Byte>()
        do {
            var next = (remaining and 0x7f).toInt()
            remaining = remaining ushr 7
            if (remaining != 0L) next = next or 0x80
            output += next.toByte()
        } while (remaining != 0L)
        return output.toByteArray()
    }

    private fun decodeVarInt(bytes: ByteArray): Long {
        var result = 0L
        var shift = 0
        bytes.forEach { byte ->
            val value = byte.toInt() and 0xff
            result = result or ((value and 0x7f).toLong() shl shift)
            if (value and 0x80 == 0) return result
            shift += 7
        }
        return result
    }

    private fun trackPayload(track: Track): ByteArray = listOf(
        wireBytes(1, track.id.toByteArray()),
        wireBytes(2, track.title.toByteArray()),
        wireBytes(3, track.artist.toByteArray()),
        wireBytes(4, track.album.toByteArray()),
        wireVarInt(5, track.durationMs),
        wireBytes(6, track.artworkUrl.orEmpty().toByteArray()),
    ).joinBytes()

    private fun trackFromPayload(payload: ByteArray): Track? {
        val fields = parseFields(payload)
        val id = fields[1]?.toString(Charsets.UTF_8).orEmpty()
        if (id.isBlank()) return null
        return Track(
            id = id,
            title = fields[2]?.toString(Charsets.UTF_8).orEmpty(),
            artist = fields[3]?.toString(Charsets.UTF_8).orEmpty(),
            album = fields[4]?.toString(Charsets.UTF_8).orEmpty(),
            artworkUrl = fields[6]?.toString(Charsets.UTF_8),
            durationMs = fields[5]?.let(::decodeVarInt) ?: 0L
        )
    }

    private fun parseFields(bytes: ByteArray): Map<Int, ByteArray> {
        val result = mutableMapOf<Int, ByteArray>()
        var cursor = 0
        while (cursor < bytes.size) {
            val tag = readVarInt(bytes, cursor) ?: break
            cursor = tag.second
            val field = tag.first ushr 3
            when (tag.first and 7) {
                0 -> {
                    val value = readLongVarInt(bytes, cursor) ?: break
                    cursor = value.second
                    result[field] = varIntLong(value.first)
                }
                2 -> {
                    val length = readVarInt(bytes, cursor) ?: break
                    cursor = length.second
                    val end = (cursor + length.first).coerceAtMost(bytes.size)
                    result[field] = bytes.copyOfRange(cursor, end)
                    cursor = end
                }
                else -> break
            }
        }
        return result
    }

    private fun parseFieldList(bytes: ByteArray): List<Pair<Int, ByteArray>> {
        val result = mutableListOf<Pair<Int, ByteArray>>()
        var cursor = 0
        while (cursor < bytes.size) {
            val tag = readVarInt(bytes, cursor) ?: break
            cursor = tag.second
            val field = tag.first ushr 3
            when (tag.first and 7) {
                0 -> {
                    val value = readLongVarInt(bytes, cursor) ?: break
                    cursor = value.second
                    result += field to varIntLong(value.first)
                }
                2 -> {
                    val length = readVarInt(bytes, cursor) ?: break
                    cursor = length.second
                    val end = (cursor + length.first).coerceAtMost(bytes.size)
                    result += field to bytes.copyOfRange(cursor, end)
                    cursor = end
                }
                else -> break
            }
        }
        return result
    }

    private fun readVarInt(bytes: ByteArray, start: Int): Pair<Int, Int>? {
        var result = 0
        var shift = 0
        var cursor = start
        while (cursor < bytes.size && shift < 32) {
            val value = bytes[cursor++].toInt() and 0xff
            result = result or ((value and 0x7f) shl shift)
            if (value and 0x80 == 0) return result to cursor
            shift += 7
        }
        return null
    }

    private fun readLongVarInt(bytes: ByteArray, start: Int): Pair<Long, Int>? {
        var result = 0L
        var shift = 0
        var cursor = start
        while (cursor < bytes.size && shift < 64) {
            val value = bytes[cursor++].toInt() and 0xff
            result = result or ((value and 0x7f).toLong() shl shift)
            if (value and 0x80 == 0) return result to cursor
            shift += 7
        }
        return null
    }
}
