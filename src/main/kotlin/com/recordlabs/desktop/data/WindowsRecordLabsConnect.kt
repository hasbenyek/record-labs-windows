package com.recordlabs.desktop.data

import java.net.DatagramPacket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

data class ConnectDevice(
    val id: String,
    val name: String,
    val platform: String,
    val lastSeenMs: Long,
    val isPlaybackDevice: Boolean,
)

data class WindowsConnectDiscoveryStatus(
    val running: Boolean = false,
    val listenerReady: Boolean = false,
    val accountIdentityCount: Int = 0,
    val ipv4InterfaceCount: Int = 0,
    val sentPackets: Long = 0,
    val receivedPackets: Long = 0,
    val rejectedAccountPackets: Long = 0,
    val lastError: String? = null,
)

data class ConnectPlaybackState(
    val ownerId: String,
    val track: Track?,
    val positionMs: Long,
    val durationMs: Long,
    val isPlaying: Boolean,
    val sentAtMs: Long,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0,
    val isReady: Boolean = false,
)

/**
 * Account-scoped, local-network transport for Record Labs Connect.
 *
 * Discovery and playback packets never contain Google credentials. Both apps
 * derive the same opaque SHA-256 fingerprint from YouTube Music's account sync
 * id and ignore packets from every other account. Multicast keeps discovery
 * automatic on the same Wi-Fi while all playback still streams independently
 * from its normal provider (only state/control data crosses the LAN).
 */
object WindowsRecordLabsConnect {
    private const val GROUP = "239.255.42.99"
    private const val PORT = 45_922
    private const val TCP_PORT = 45_923
    private const val PROTOCOL = 3
    private val json = Json { ignoreUnknownKeys = true }
    private val running = AtomicBoolean(false)
    // Three listeners can block indefinitely (UDP discovery, UDP return path,
    // and TCP fallback). Keep spare workers for TCP peers and heartbeats.
    private val executor = Executors.newScheduledThreadPool(8) { task ->
        Thread(task, "record-labs-connect").apply { isDaemon = true }
    }
    // Each TCP link has a reader for its whole life: they get their own threads,
    // so many peers can never starve the heartbeat on [executor].
    private val linkExecutor = Executors.newCachedThreadPool { task ->
        Thread(task, "record-labs-connect-link").apply { isDaemon = true }
    }
    // The /24 scan opens up to 254 short connects; bounded and away from the heartbeat.
    private val scanExecutor = Executors.newFixedThreadPool(16) { task ->
        Thread(task, "record-labs-connect-scan").apply { isDaemon = true }
    }
    private val sendExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "record-labs-connect-send").apply { isDaemon = true }
    }
    // Every packet (UDP, TCP, Bluetooth) is handled on one thread, in arrival
    // order: ownership and terms were previously updated from three listener
    // threads at once.
    private val handleExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "record-labs-connect-handle").apply { isDaemon = true }
    }
    /**
     * A peer sends each message over several transports at once (TCP, limited
     * and directed broadcast, multicast, unicast), so the same message arrives
     * up to six times. Duplicates are byte-identical (same sentAt), so they are
     * dropped here; otherwise one "toggle" flipped play/pause several times and
     * one "next" skipped several songs.
     */
    private val recentPackets = LinkedHashMap<String, Long>()
    private const val DUPLICATE_WINDOW_MS = 20_000L
    /** The current output may stay silent this long (lost heartbeats) before it is considered gone. */
    private const val OWNER_GRACE_MS = 15_000L
    private const val PEER_GRACE_MS = 8_000L
    /** A mirrored output not heard from this long yields to another device claiming the sound. */
    private const val SILENT_OWNER_MS = 4_000L
    /** A requested takeover waits this long for this player to be ready, then takes over anyway. */
    private const val TAKEOVER_WAIT_MS = 8_000L
    /** A handoff to another device that has not happened by then is reported as stalled. */
    private const val TRANSFER_TIMEOUT_MS = 12_000L
    /** A TCP link counts as alive while something arrived on it this recently. */
    private const val LINK_FRESH_MS = 6_000L
    private const val LINK_DEAD_MS = 25_000L
    /** A write blocked this long means the peer stopped reading: the link is dropped. */
    private const val LINK_WRITE_STALL_MS = 3_000L
    private const val TCP_RETRY_MS = 10_000L
    private var socket: MulticastSocket? = null
    private var outboundSocket: MulticastSocket? = null
    private var tcpServer: ServerSocket? = null
    private val tcpLinks = ConcurrentHashMap.newKeySet<TcpLink>()
    private var heartbeat: ScheduledFuture<*>? = null
    private var subnetScan: ScheduledFuture<*>? = null
    private var accountFingerprints = emptySet<String>()
    private var deviceName = "Windows PC"
    private val localId = WindowsPreferences.connectDeviceId()
    @Volatile private var ownerTerm = 1L
    @Volatile private var localOwnerActive = false
    @Volatile private var localPlaybackInProgress = false
    @Volatile private var pendingTakeoverTerm: Long? = null
    @Volatile private var pendingTakeoverSince = 0L
    /** The output that asked this PC to take over (the takeover is for it only). */
    @Volatile private var pendingTakeoverFrom: String? = null
    @Volatile private var pendingTransferTarget: String? = null
    @Volatile private var pendingTransferSince = 0L
    @Volatile private var transferStallReported = false
    /** Highest term offered in a handoff: every new pick offers a higher one, so the last pick wins. */
    @Volatile private var lastOfferedTerm = 0L
    private val tcpConnecting = ConcurrentHashMap.newKeySet<String>()
    private val tcpLastAttempt = ConcurrentHashMap<String, Long>()
    /** This device became the output because another device asked it to (prepare_takeover / transfer). */
    private var localOwnershipHandler: (() -> Unit)? = null
    /** A handoff this device started did not complete; the argument is the target's name. */
    private var transferStalledHandler: ((String) -> Unit)? = null

    /** Called by the app once its player is prepared, so a deferred takeover can run. */
    fun notifyPlayerReady() {
        if (pendingTakeoverTerm != null) handleExecutor.execute { completePendingTakeover(force = false) }
    }

    private fun completePendingTakeover(force: Boolean) {
        val term = pendingTakeoverTerm ?: return
        // Only for the output that asked; if the sound has moved on since, drop it.
        val from = pendingTakeoverFrom
        if (from != null && _remoteOwnerId.value != null && _remoteOwnerId.value != from) {
            pendingTakeoverTerm = null
            return
        }
        if (!force && playbackProvider?.invoke()?.isReady != true) return
        pendingTakeoverTerm = null
        takeRequestedOwnership(term)
    }

    /** Takes the output because a peer asked for it, and lets the app start sound here. */
    private fun takeRequestedOwnership(requestedTerm: Long?) {
        pendingTakeoverTerm = null
        takePlaybackOwnership(requestedTerm, handoff = true)
        localOwnershipHandler?.invoke()
    }

    private fun requestTakeover(requestedTerm: Long?, from: String) {
        val currentOwner = _remoteOwnerId.value
        if (currentOwner != null && currentOwner != from) {
            // Asked by a device that is itself only a mirror (it picked this PC from
            // its list). Taking the sound here would leave the real output playing
            // too, so ask the real output to hand it over instead.
            requestTransferFrom(currentOwner, localId)
            return
        }
        if (playbackProvider?.invoke()?.isReady == true) {
            takeRequestedOwnership(requestedTerm)
        } else {
            pendingTakeoverTerm = requestedTerm ?: (ownerTerm + 1L)
            pendingTakeoverSince = System.currentTimeMillis()
            pendingTakeoverFrom = from
        }
    }

    private var playbackProvider: (() -> ConnectPlaybackState?)? = null
    private var remoteStateHandler: ((ConnectPlaybackState) -> Unit)? = null
    private var commandHandler: ((String, Long?, Track?) -> Unit)? = null
    private var bluetoothSender: ((String) -> Unit)? = null
    private val seen = linkedMapOf<String, ConnectDevice>()
    private val peerEndpoints = linkedMapOf<String, InetSocketAddress>()
    private val peerObservedEndpoints = linkedMapOf<String, InetSocketAddress>()
    private val _devices = MutableStateFlow<List<ConnectDevice>>(emptyList())
    val devices: StateFlow<List<ConnectDevice>> = _devices.asStateFlow()
    private val sentPackets = AtomicLong(0)
    private val receivedPackets = AtomicLong(0)
    private val rejectedAccountPackets = AtomicLong(0)
    private val _discoveryStatus = MutableStateFlow(WindowsConnectDiscoveryStatus())
    val discoveryStatus: StateFlow<WindowsConnectDiscoveryStatus> = _discoveryStatus.asStateFlow()
    private val _remoteOwnerId = MutableStateFlow<String?>(null)
    val remoteOwnerId: StateFlow<String?> = _remoteOwnerId.asStateFlow()
    val localDeviceId: String get() = localId

    fun start(
        accountKeys: Collection<String>,
        deviceName: String,
        playbackProvider: () -> ConnectPlaybackState?,
        onRemoteState: (ConnectPlaybackState) -> Unit,
        onCommand: (String, Long?, Track?) -> Unit,
        onLocalOwnership: (() -> Unit)? = null,
        onTransferStalled: ((String) -> Unit)? = null,
    ) {
        val fingerprints = accountKeys.map(String::trim).filter(String::isNotBlank)
            .map(::fingerprint).toSet()
        if (fingerprints.isEmpty()) {
            stop()
            _discoveryStatus.value = WindowsConnectDiscoveryStatus(
                lastError = "Google account identity is unavailable",
            )
            return
        }
        this.playbackProvider = playbackProvider
        this.remoteStateHandler = onRemoteState
        this.commandHandler = onCommand
        this.localOwnershipHandler = onLocalOwnership
        this.transferStalledHandler = onTransferStalled
        this.deviceName = deviceName.ifBlank { "Windows PC" }
        if (running.get() && fingerprints == accountFingerprints) return
        stop()
        accountFingerprints = fingerprints
        running.set(true)
        sentPackets.set(0)
        receivedPackets.set(0)
        rejectedAccountPackets.set(0)
        _discoveryStatus.value = WindowsConnectDiscoveryStatus(
            running = true,
            accountIdentityCount = accountFingerprints.size,
            ipv4InterfaceCount = ipv4Networks().size,
        )
        outboundSocket = createOutboundSocket()
        executor.execute(::receiveLoop)
        outboundSocket?.let { outgoing ->
            executor.execute { receivePackets(outgoing) }
        }
        executor.execute(::tcpAcceptLoop)
        heartbeat = executor.scheduleAtFixedRate(::announce, 0L, 1_500L, TimeUnit.MILLISECONDS)
        // Subnet scanner: bridges the gap when Android (WSA) UDP never reaches Windows.
        // Starts after 6 s to let UDP work first; retries every 30 s while no peers seen.
        subnetScan = executor.scheduleAtFixedRate(::doSubnetScan, 6_000L, 30_000L, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        running.set(false)
        heartbeat?.cancel(true)
        heartbeat = null
        subnetScan?.cancel(true)
        subnetScan = null
        socket?.close()
        socket = null
        outboundSocket?.close()
        outboundSocket = null
        tcpServer?.close()
        tcpServer = null
        tcpLinks.toList().forEach(TcpLink::close)
        tcpLinks.clear()
        synchronized(seen) {
            seen.clear()
            peerEndpoints.clear()
            peerObservedEndpoints.clear()
        }
        _devices.value = emptyList()
        _remoteOwnerId.value = null
        localOwnerActive = false
        pendingTakeoverTerm = null
        pendingTakeoverFrom = null
        pendingTransferTarget = null
        tcpLastAttempt.clear()
        synchronized(recentPackets) { recentPackets.clear() }
        _discoveryStatus.value = _discoveryStatus.value.copy(running = false, listenerReady = false)
    }

    fun takePlaybackOwnership(requestedTerm: Long? = null) = takePlaybackOwnership(requestedTerm, handoff = false)

    private fun takePlaybackOwnership(requestedTerm: Long?, handoff: Boolean) {
        ownerTerm = requestedTerm?.coerceAtLeast(ownerTerm + 1L) ?: (ownerTerm + 1L)
        _remoteOwnerId.value = null
        localOwnerActive = true
        pendingTakeoverTerm = null
        pendingTransferTarget = null
        sendOwnershipClaim(handoff)
        announce()
    }

    /**
     * [handoff]: the claim answers a pick of this device as the output. Such a
     * claim with a newer term wins even over a device that is playing (that
     * device was the previous pick), so the user's last pick always ends up as
     * the only output. Claims without it (an app just opened) never take sound
     * away from a playing device.
     */
    private fun sendOwnershipClaim(handoff: Boolean = false) {
        send(buildJsonObject {
            envelope("ownership")
            put("owner", localId)
            put("ownerTerm", ownerTerm)
            if (handoff) put("handoff", true)
        }.toString())
    }

    fun selectPlaybackDevice(deviceId: String): Boolean {
        if (deviceId == localId) {
            // Let the current output initiate the handoff. A local ownership
            // claim from the controller is intentionally rejected while the
            // host is playing, otherwise both devices can remain audible.
            val currentOwner = _remoteOwnerId.value
            if (currentOwner != null) {
                requestTransferFrom(currentOwner, deviceId)
                return true
            }
            takePlaybackOwnership()
            return true
        }
        synchronized(seen) { if (seen[deviceId] == null) return false }
        val currentOwner = _remoteOwnerId.value
        if (currentOwner != null) {
            // This PC is only a mirror: the device playing the sound must make the
            // handoff, otherwise it keeps playing next to the new output.
            if (currentOwner != deviceId) requestTransferFrom(currentOwner, deviceId)
            return true
        }
        return handOverTo(deviceId)
    }

    /** This PC plays the sound and asks [deviceId] to take it over. */
    private fun handOverTo(deviceId: String): Boolean {
        // Do not abdicate immediately (deferred handoff).
        // Request the target to take over with a strictly greater term
        // so we gracefully abdicate when they broadcast their new state.
        val requestedTerm = maxOf(ownerTerm, lastOfferedTerm) + 1L
        lastOfferedTerm = requestedTerm
        pendingTransferTarget = deviceId
        pendingTransferSince = System.currentTimeMillis()
        transferStallReported = false
        // Defer handoff: Wait until the target device acknowledges it is ready.
        sendTo(deviceId, buildJsonObject {
            envelope("command")
            put("target", deviceId)
            put("command", "prepare_takeover")
            put("requestedOwnerTerm", requestedTerm)
        }.toString())
        return true
    }

    /** Requests an atomic handoff from [currentOwner] to [deviceId]. */
    private fun requestTransferFrom(currentOwner: String, deviceId: String) {
        sendTo(currentOwner, buildJsonObject {
            envelope("command")
            put("target", currentOwner)
            put("command", "transfer_playback")
            put("outputDevice", deviceId)
        }.toString())
    }

    fun configureBluetooth(sender: ((String) -> Unit)?) {
        bluetoothSender = sender
    }

    fun receiveBluetooth(raw: String) = dispatch(raw, null, PORT)

    fun sendCommand(command: String, positionMs: Long? = null): Boolean {
        val target = _remoteOwnerId.value ?: return false
        sendTo(target, buildJsonObject {
            envelope("command")
            put("target", target)
            put("command", command)
            positionMs?.let { put("positionMs", it) }
        }.toString())
        return true
    }

    fun hasRemotePlaybackDevice(): Boolean = _remoteOwnerId.value != null

    fun sendPlayTrack(track: Track): Boolean {
        val target = _remoteOwnerId.value ?: return false
        sendTo(target, buildJsonObject {
            envelope("command")
            put("target", target)
            put("command", "play_track")
            put("trackId", track.id)
            put("title", track.title)
            put("artist", track.artist)
            put("album", track.album)
            put("artwork", track.artworkUrl.orEmpty())
            put("durationMs", track.durationMs)
        }.toString())
        return true
    }

    /** Adds a song immediately after the host's current queue item. */
    fun sendQueueNext(track: Track): Boolean {
        val target = _remoteOwnerId.value ?: return false
        sendTo(target, buildJsonObject {
            envelope("command")
            put("target", target)
            put("command", "queue_next")
            put("trackId", track.id)
            put("title", track.title)
            put("artist", track.artist)
            put("album", track.album)
            put("artwork", track.artworkUrl.orEmpty())
            put("durationMs", track.durationMs)
        }.toString())
        return true
    }

    fun sendPlayQueueIndex(index: Int): Boolean {
        val target = _remoteOwnerId.value ?: return false
        sendTo(target, buildJsonObject {
            envelope("command")
            put("target", target)
            put("command", "play_queue_index")
            put("positionMs", index.toLong())
        }.toString())
        return true
    }

    /** Reorders the active output queue using a stable track id. */
    fun sendQueueMove(trackId: String, toIndex: Int): Boolean {
        val target = _remoteOwnerId.value ?: return false
        sendTo(target, buildJsonObject {
            envelope("command")
            put("target", target)
            put("command", "queue_move")
            put("trackId", trackId)
            put("positionMs", toIndex.toLong())
        }.toString())
        return true
    }

    /** Acknowledges that this silent controller has prepared the host track. */
    fun sendTrackReady(ownerId: String, trackId: String) {
        if (ownerId.isBlank() || trackId.isBlank()) return
        sendTo(ownerId, buildJsonObject {
            envelope("command")
            put("target", ownerId)
            put("command", "track_ready")
            put("trackId", trackId)
        }.toString())
    }

    /** The heartbeat runs on the packet-handling thread, so ownership is only ever changed there. */
    private fun announce() {
        if (!running.get()) return
        runCatching { handleExecutor.execute(::announceNow) }
    }

    private fun announceNow() {
        if (!running.get()) return
        // Deferred takeover (requested while this player was still loading).
        if (pendingTakeoverTerm != null) {
            val waited = System.currentTimeMillis() - pendingTakeoverSince
            completePendingTakeover(force = waited >= TAKEOVER_WAIT_MS)
        }
        pendingTransferTarget?.let { target ->
            // Report a slow handoff once, but keep accepting the target's claim:
            // it may still become ready (and must then not be refused while this
            // PC keeps playing).
            if (!transferStallReported && System.currentTimeMillis() - pendingTransferSince >= TRANSFER_TIMEOUT_MS) {
                transferStallReported = true
                val name = synchronized(seen) { seen[target]?.name } ?: "the device"
                transferStalledHandler?.invoke(name)
            }
        }
        // Links that went quiet (peer gone, Wi-Fi switched) or stopped reading are dropped.
        val nowMs = System.currentTimeMillis()
        tcpLinks.toList().forEach { link ->
            val stalledWrite = link.writeStartedAt.let { it > 0L && nowMs - it > LINK_WRITE_STALL_MS }
            if (stalledWrite || nowMs - link.lastRxAt > LINK_DEAD_MS) link.close()
        }
        val state = playbackProvider?.invoke()
        if (state?.isPlaying == true && _remoteOwnerId.value == null && !localOwnerActive) {
            ownerTerm += 1L
            localOwnerActive = true
        }
        val localOwnsPlayback = state?.track != null && _remoteOwnerId.value == null && localOwnerActive
        send(buildJsonObject {
            envelope(if (localOwnsPlayback) "state" else "hello")
            put("name", deviceName)
            put("platform", "windows")
            if (state != null && localOwnsPlayback) {
                put("owner", localId)
                put("ownerTerm", ownerTerm)
                put("trackId", state.track?.id.orEmpty())
                put("title", state.track?.title.orEmpty())
                put("artist", state.track?.artist.orEmpty())
                put("album", state.track?.album.orEmpty())
                put("artwork", state.track?.artworkUrl.orEmpty())
                put("positionMs", state.positionMs)
                put("durationMs", state.durationMs)
                put("playing", state.isPlaying)
                put("queueIndex", state.queueIndex)
                put("queue", JsonArray(state.queue.take(100).map { track ->
                    buildJsonObject {
                        put("trackId", track.id)
                        put("title", track.title)
                        put("artist", track.artist)
                        put("album", track.album)
                        put("artwork", track.artworkUrl.orEmpty())
                        put("durationMs", if (track.id == state.track?.id) state.durationMs else track.durationMs)
                    }
                }))
            }
        }.toString())
        val cutoff = System.currentTimeMillis() - PEER_GRACE_MS
        // The device playing the sound gets longer: a phone's Wi-Fi power saving
        // can hold its heartbeats for several seconds, and dropping the owner
        // too early made both devices audible.
        val ownerCutoff = System.currentTimeMillis() - OWNER_GRACE_MS
        val currentOwner = _remoteOwnerId.value
        var expiredOwner: String? = null
        synchronized(seen) {
            val connectedTcpPeers = tcpLinks.mapNotNullTo(mutableSetOf()) { it.peerId }
            // Once the first account-validated packet has identified a TCP
            // link, the open connection is a stronger liveness signal than a
            // lossy UDP heartbeat. Keep that peer visible until the socket is
            // actually closed.
            seen.entries.removeIf {
                it.key !in connectedTcpPeers &&
                    it.value.lastSeenMs < (if (it.key == currentOwner) ownerCutoff else cutoff)
            }
            _remoteOwnerId.value?.takeIf { it !in seen }?.let { expiredOwner = it }
            peerEndpoints.keys.removeIf { it !in seen }
            peerObservedEndpoints.keys.removeIf { it !in seen }
            _devices.value = seen.values.sortedByDescending { it.lastSeenMs }
        }
        expiredOwner?.let(::clearRemoteOwnership)
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.envelope(type: String) {
        put("v", PROTOCOL)
        put("type", type)
        put("account", accountFingerprints.firstOrNull().orEmpty())
        put("accounts", JsonArray(accountFingerprints.sorted().map(::JsonPrimitive)))
        put("device", localId)
        put("sentAt", System.currentTimeMillis())
    }

    private fun send(payload: String) {
        if (!running.get()) return
        sendExecutor.execute {
            if (!running.get()) return@execute
            // Ownership/state handoff uses the persistent validated TCP link.
            // Send it before optional Bluetooth and UDP work so switching the
            // output device can never be delayed by a fallback transport.
            tcpLinks.toList().forEach { it.send(payload) }
            bluetoothSender?.invoke(payload)
            runCatching {
                val bytes = payload.toByteArray(Charsets.UTF_8)
                val outgoing = outboundSocket?.takeUnless { it.isClosed }
                if (outgoing != null) {
                    sendUdp(outgoing, bytes)
                } else {
                    createOutboundSocket()?.use { temporary -> sendUdp(temporary, bytes) }
                }
            }
        }
    }

    /**
     * A message meant for one peer (commands, track_ready, takeover) goes over
     * that peer's TCP link when there is one: a single reliable copy. Without a
     * link it falls back to every transport like other messages.
     */
    private fun sendTo(target: String, payload: String) {
        if (!running.get()) return
        // The most recently active link to that peer; a stale one (half-open after a
        // Wi-Fi change) would swallow the message, so then every transport is used.
        val now = System.currentTimeMillis()
        val link = tcpLinks.filter { it.peerId == target && now - it.lastRxAt <= LINK_FRESH_MS }
            .maxByOrNull { it.lastRxAt }
        if (link == null || !link.send(payload)) send(payload)
    }

    /**
     * Opens a TCP link to another Windows PC once it is discovered over UDP, so
     * commands and handoffs between two PCs do not depend on lossy datagrams.
     * (Android peers open their own link to this PC.)
     */
    private fun ensureTcpLink(peerId: String, address: InetAddress) {
        if (!running.get() || tcpLinks.any { it.peerId == peerId }) return
        val now = System.currentTimeMillis()
        if (now - (tcpLastAttempt[peerId] ?: 0L) < TCP_RETRY_MS) return
        if (!tcpConnecting.add(peerId)) return
        tcpLastAttempt[peerId] = now
        linkExecutor.execute {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(address, TCP_PORT), 2_000)
                val link = TcpLink(socket, peerId)
                tcpLinks.add(link)
                link.send(buildJsonObject {
                    envelope("hello")
                    put("name", deviceName)
                    put("platform", "windows")
                }.toString())
                link.start()
            } catch (_: Throwable) {
            } finally {
                tcpConnecting.remove(peerId)
            }
        }
    }

    /**
     * UDP discovery can be asymmetric with virtual Android bridges: Android
     * receives Windows broadcasts but Windows Firewall/NAT never sees the
     * return datagram. Once Android has learned this PC's address, it opens
     * this local TCP uplink. It carries the exact same account-filtered JSON
     * messages and is not a manual pairing mechanism.
     */
    private fun tcpAcceptLoop() {
        val server = runCatching { ServerSocket(TCP_PORT) }.getOrElse {
            _discoveryStatus.value = _discoveryStatus.value.copy(
                lastError = "Connect TCP listener failed: ${it.message ?: it.javaClass.simpleName}",
            )
            return
        }
        tcpServer = server
        while (running.get() && !server.isClosed) {
            val client = runCatching { server.accept() }.getOrNull() ?: continue
            if (!running.get()) {
                runCatching { client.close() }
                break
            }
            attachTcp(client)
        }
    }

    private fun attachTcp(socket: Socket) {
        val link = TcpLink(socket)
        tcpLinks.add(link)
        link.start()
    }

    /**
     * Active subnet scanner for Windows: symmetric to Android's doSubnetScan.
     * When no peers are discovered via UDP (e.g. Android on WSA where Hyper-V
     * drops inbound multicast/broadcast before the guest OS sees it), Windows
     * actively probes every IP in the same /24 subnet on TCP_PORT. On a
     * successful connection it sends an account-fingerprinted hello so the
     * remote peer can validate the account and register this device.
     *
     * Interfaces with a .1 host address (VirtualBox, VMware, WSA-internal)
     * are skipped to avoid flooding internal-only subnets.
     */
    private fun doSubnetScan() {
        if (!running.get()) return
        val anyPeerKnown = synchronized(seen) { seen.isNotEmpty() }
        if (anyPeerKnown) return
        val selfAddrs = ipv4Networks()
            .flatMap { it.inetAddresses.toList().filterIsInstance<Inet4Address>() }
            .map { it.address.toList() }
            .toSet()
        val targets = ipv4Networks()
            .flatMap { iface ->
                iface.inetAddresses.toList().filterIsInstance<Inet4Address>()
                    .filter { addr -> addr.address[3] != 1.toByte() }  // skip virtual host adapters
                    .flatMap { myAddr ->
                        (1..254).mapNotNull { i ->
                            val b = myAddr.address.copyOf().also { it[3] = i.toByte() }
                            if (b.toList() in selfAddrs) null else InetAddress.getByAddress(b)
                        }
                    }
            }
            .distinctBy { it.hostAddress }
            .filter { target ->
                // Skip IPs already known via existing peer endpoints
                synchronized(seen) {
                    peerEndpoints.values.none { it.address.hostAddress == target.hostAddress }
                }
            }
        targets.forEach { target ->
            scanExecutor.execute {
                if (!running.get()) return@execute
                runCatching {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(target, TCP_PORT), 400)
                    attachTcp(socket)
                    // Send hello immediately so the remote peer can validate the account
                    // and register this device. The heartbeat alone would work eventually
                    // but sending right away cuts discovery time to one round-trip.
                    send(buildJsonObject {
                        envelope("hello")
                        put("name", deviceName)
                        put("platform", "windows")
                    }.toString())
                }
            }
        }
    }

    private class TcpLink(private val socket: Socket, knownPeer: String? = null) {
        private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8))
        // Writes run on the link's own thread: a peer that stops reading can block
        // only this link (it is then closed), never the shared sender.
        private val writeQueue = Executors.newSingleThreadExecutor { task ->
            Thread(task, "record-labs-connect-write").apply { isDaemon = true }
        }
        @Volatile private var closed = false
        @Volatile var peerId: String? = knownPeer
            private set
        @Volatile var lastRxAt: Long = System.currentTimeMillis()
            private set
        @Volatile var writeStartedAt: Long = 0L
            private set

        fun start() {
            linkExecutor.execute {
                try {
                    BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8)).use { reader ->
                        while (running.get() && !socket.isClosed) {
                            val line = reader.readLine() ?: break
                            lastRxAt = System.currentTimeMillis()
                            if (line.isNotBlank()) {
                                val candidateId = runCatching {
                                    json.parseToJsonElement(line).jsonObject["device"]
                                        ?.jsonPrimitive?.contentOrNull
                                }.getOrNull()
                                handleExecutor.execute {
                                    handle(line, socket.inetAddress, TCP_PORT)
                                    if (peerId == null && !candidateId.isNullOrBlank() && synchronized(seen) { candidateId in seen }) {
                                        peerId = candidateId
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Throwable) {
                } finally {
                    close()
                }
            }
        }

        /** Queues [payload]; false when the link is already closed. */
        fun send(payload: String): Boolean {
            if (closed) return false
            return runCatching {
                writeQueue.execute {
                    if (closed) return@execute
                    writeStartedAt = System.currentTimeMillis()
                    try {
                        writer.write(payload)
                        writer.newLine()
                        writer.flush()
                    } catch (_: Throwable) {
                        close()
                    } finally {
                        writeStartedAt = 0L
                    }
                }
                true
            }.getOrDefault(false)
        }

        fun close() {
            closed = true
            tcpLinks.remove(this)
            runCatching { socket.close() }
            runCatching { writer.close() }
            writeQueue.shutdown()
        }
    }

    /**
     * Keep discovery traffic on a persistent ephemeral port. Besides avoiding
     * contention with the well-known listener port, this gives WSA's NAT a
     * stable return route for the peer's unicast heartbeat response.
     */
    private fun createOutboundSocket(): MulticastSocket? = runCatching {
        MulticastSocket(null).apply {
            reuseAddress = true
            bind(InetSocketAddress(0))
            timeToLive = 1
            broadcast = true
        }
    }.getOrNull()

    private fun sendUdp(outgoing: MulticastSocket, bytes: ByteArray) {
        synchronized(outgoing) {
            val networks = ipv4Networks()
            outgoing.timeToLive = 1
            outgoing.broadcast = true
            // Try the default route, then send each directed broadcast while
            // its own interface is selected. This avoids a Hyper-V/WSA or VPN
            // adapter accidentally capturing traffic intended for Wi-Fi.
            runCatching {
                outgoing.send(DatagramPacket(bytes, bytes.size, InetAddress.getByName("255.255.255.255"), PORT))
            }.onSuccess { sentPackets.incrementAndGet() }
            networks.forEach { network ->
                runCatching { outgoing.networkInterface = network }
                runCatching {
                    outgoing.send(DatagramPacket(bytes, bytes.size, InetAddress.getByName(GROUP), PORT))
                }.onSuccess { sentPackets.incrementAndGet() }
                network.interfaceAddresses.mapNotNull { it.broadcast }.distinct().forEach { broadcast ->
                    runCatching {
                        outgoing.send(DatagramPacket(bytes, bytes.size, broadcast, PORT))
                    }.onSuccess { sentPackets.incrementAndGet() }
                }
            }
            synchronized(seen) {
                (peerEndpoints.values + peerObservedEndpoints.values).distinct().forEach { endpoint ->
                    runCatching {
                        outgoing.send(DatagramPacket(bytes, bytes.size, endpoint.address, endpoint.port))
                    }.onSuccess { sentPackets.incrementAndGet() }
                }
            }
            publishPacketCounts()
        }
    }

    private fun receiveLoop() {
        val multicast = runCatching {
            MulticastSocket(null).apply {
                reuseAddress = true
                bind(InetSocketAddress(PORT))
                timeToLive = 1
                ipv4Networks()
                    .forEach { network -> runCatching { joinGroup(InetSocketAddress(GROUP, PORT), network) } }
            }
        }.getOrElse {
            _discoveryStatus.value = _discoveryStatus.value.copy(
                listenerReady = false,
                lastError = "UDP listener failed: ${it.message ?: it.javaClass.simpleName}",
            )
            return
        }
        socket = multicast
        _discoveryStatus.value = _discoveryStatus.value.copy(listenerReady = true, lastError = null)
        receivePackets(multicast)
    }

    private fun receivePackets(input: MulticastSocket) {
        val buffer = ByteArray(32 * 1024)
        while (running.get() && !input.isClosed) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                input.receive(packet)
                dispatch(String(packet.data, packet.offset, packet.length, Charsets.UTF_8), packet.address, packet.port)
            } catch (_: Throwable) {
                if (running.get()) Thread.sleep(120L)
            }
        }
    }

    private fun dispatch(raw: String, sourceAddress: InetAddress?, sourcePort: Int) {
        if (!running.get()) return
        handleExecutor.execute { handle(raw, sourceAddress, sourcePort) }
    }

    /** True the first time this exact message is seen (see [recentPackets]). */
    private fun firstCopy(raw: String): Boolean {
        val now = System.currentTimeMillis()
        val key = "${raw.length}:${raw.hashCode()}:${raw.takeLast(48)}"
        synchronized(recentPackets) {
            val iterator = recentPackets.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value > DUPLICATE_WINDOW_MS || recentPackets.size > 1_024) iterator.remove() else break
            }
            if (recentPackets.containsKey(key)) return false
            recentPackets[key] = now
            return true
        }
    }

    private fun handle(raw: String, sourceAddress: InetAddress? = null, sourcePort: Int = PORT) {
        val value = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull() ?: return
        if (value["v"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() != PROTOCOL) return
        val sender = value["device"]?.jsonPrimitive?.contentOrNull.orEmpty()
        if (sender.isBlank() || sender == localId) return
        receivedPackets.incrementAndGet()
        publishPacketCounts()
        val remoteFingerprints = buildSet {
            value["account"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)?.let(::add)
            (value["accounts"] as? JsonArray)?.mapNotNullTo(this) { it.jsonPrimitive.contentOrNull }
        }
        if (remoteFingerprints.none(accountFingerprints::contains)) {
            rejectedAccountPackets.incrementAndGet()
            publishPacketCounts()
            return
        }
        val type = value["type"]?.jsonPrimitive?.contentOrNull.orEmpty()
        val now = System.currentTimeMillis()
        val owner = value["owner"]?.jsonPrimitive?.contentOrNull
        val isNewPeer = synchronized(seen) {
            val newPeer = sender !in seen
            // Reply to the peer's fixed discovery listener rather than the
            // ephemeral source port used by its outbound heartbeat. This is
            // important for Windows Firewall and bridged Android/Waydroid
            // adapters, where only UDP 45922 is intentionally opened.
            sourceAddress?.let {
                peerEndpoints[sender] = InetSocketAddress(it, PORT)
                if (sourcePort != TCP_PORT) peerObservedEndpoints[sender] = InetSocketAddress(it, sourcePort)
            }
            // Commands and ownership claims carry no name: keep the one the
            // device announced instead of flashing "Record Labs device".
            val known = seen[sender]
            seen[sender] = ConnectDevice(
                id = sender,
                name = value["name"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
                    ?: known?.name ?: "Record Labs device",
                platform = value["platform"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
                    ?: known?.platform.orEmpty(),
                lastSeenMs = now,
                isPlaybackDevice = owner == sender,
            )
            _devices.value = seen.values.sortedByDescending { it.lastSeenMs }
            newPeer
        }
        if (isNewPeer) executor.execute(::announce)
        if (sourceAddress != null && sourcePort != TCP_PORT &&
            value["platform"]?.jsonPrimitive?.contentOrNull == "windows"
        ) ensureTcpLink(sender, sourceAddress)
        // Presence and addresses above are updated for every copy; the message
        // itself is applied once.
        if (!firstCopy(raw)) return
        when (type) {
            // Presence packets can arrive after ownership packets because UDP,
            // TCP, and Bluetooth are independent transports. A delayed hello is
            // not an ownership release; only an explicit handoff or peer expiry
            // may change the selected output.
            "hello" -> Unit
            "ownership" -> if (owner == sender) {
                val incomingTerm = value["ownerTerm"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: return
                val handoff = value["handoff"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() == true
                if (ownerWins(sender, incomingTerm, handoff = handoff)) {
                    ownerTerm = incomingTerm
                    _remoteOwnerId.value = sender
                    pendingTransferTarget = null
                    // A device claimed the sound: any takeover this PC was waiting
                    // for is outdated (the user picked another output meanwhile).
                    pendingTakeoverTerm = null
                    localOwnerActive = false
                    remoteStateHandler?.invoke(
                        ConnectPlaybackState(sender, null, 0L, 0L, false, now),
                    )
                }
            }
            "state" -> {
                if (owner != sender) return
                val incomingTerm = value["ownerTerm"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
                val claimantPlaying = value["playing"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false
                if (!ownerWins(sender, incomingTerm, claimantPlaying)) {
                    // This PC keeps its own playback. Tell an Android output not to
                    // hold its next song for us (its track-ready barrier).
                    value["trackId"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
                        ?.let { sendTrackReady(sender, it) }
                    return
                }
                ownerTerm = incomingTerm
                _remoteOwnerId.value = sender
                pendingTransferTarget = null
                // Following a different output than the one that asked: drop that takeover.
                if (pendingTakeoverFrom != sender) pendingTakeoverTerm = null
                localOwnerActive = false
                val trackId = value["trackId"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val track = trackId.takeIf(String::isNotBlank)?.let {
                    Track(
                        id = it,
                        title = value["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        artist = value["artist"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        album = value["album"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        artworkUrl = value["artwork"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank),
                        durationMs = value["durationMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L,
                    )
                }
                val queue = (value["queue"] as? JsonArray).orEmpty().mapNotNull { entry ->
                    val item = runCatching { entry.jsonObject }.getOrNull() ?: return@mapNotNull null
                    val id = item["trackId"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    id.takeIf(String::isNotBlank)?.let {
                        Track(
                            id = it,
                            title = item["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                            artist = item["artist"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                            album = item["album"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                            artworkUrl = item["artwork"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank),
                            durationMs = item["durationMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L,
                        )
                    }
                }
                remoteStateHandler?.invoke(ConnectPlaybackState(
                    ownerId = sender,
                    track = track,
                    positionMs = value["positionMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L,
                    durationMs = value["durationMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L,
                    isPlaying = value["playing"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false,
                    sentAtMs = value["sentAt"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: now,
                    queue = queue,
                    queueIndex = value["queueIndex"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0,
                ))
            }
            "command" -> if (value["target"]?.jsonPrimitive?.contentOrNull == localId) {
                val command = value["command"]?.jsonPrimitive?.contentOrNull.orEmpty()
                if (command == "transfer_playback") {
                    val outputDevice = value["outputDevice"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    if (outputDevice.isNotBlank() && outputDevice != localId) {
                        // Only the device playing the sound hands it over; a stale
                        // request reaching a mirror is ignored (no forwarding loops).
                        val known = synchronized(seen) { outputDevice in seen }
                        if (_remoteOwnerId.value == null && known) handOverTo(outputDevice)
                    } else if (outputDevice == localId) {
                        requestTakeover(value["requestedOwnerTerm"]?.jsonPrimitive?.contentOrNull?.toLongOrNull(), sender)
                    }
                    return
                }
                if (command == "take_ownership") {
                    val currentOwner = _remoteOwnerId.value
                    if (currentOwner != null && currentOwner != sender) {
                        requestTransferFrom(currentOwner, localId)
                    } else {
                        takeRequestedOwnership(value["requestedOwnerTerm"]?.jsonPrimitive?.contentOrNull?.toLongOrNull())
                    }
                    return
                }
                if (command == "prepare_takeover") {
                    // Become the output as soon as this (silently mirroring) player is ready.
                    requestTakeover(value["requestedOwnerTerm"]?.jsonPrimitive?.contentOrNull?.toLongOrNull(), sender)
                    return
                }
                val commandTrack = if (command == "play_track" || command == "queue_next" || command == "queue_move") {
                    value["trackId"]?.jsonPrimitive?.contentOrNull
                        ?.takeIf(String::isNotBlank)
                        ?.let { trackId ->
                            Track(
                                id = trackId,
                                title = value["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                artist = value["artist"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                album = value["album"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                artworkUrl = value["artwork"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank),
                                durationMs = value["durationMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L,
                                isOfficial = true,
                            )
                        }
                } else null
                commandHandler?.invoke(
                    command,
                    value["positionMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull(),
                    commandTrack,
                )
            }
        }
    }

    private fun ownerWins(sender: String, incomingTerm: Long, claimantPlaying: Boolean? = null, handoff: Boolean = false): Boolean {
        val currentRemote = _remoteOwnerId.value
        // The current output's own updates always count. Its term restarts at 1
        // when its app restarts; requiring a higher term froze this controller
        // on a stale picture of that device.
        if (currentRemote == sender) return true
        if (pendingTransferTarget == sender) return incomingTerm >= ownerTerm
        // The answer to a newer pick of that device as the output.
        if (handoff && incomingTerm > ownerTerm) return true
        // The output this PC mirrors has gone quiet (app closed, reinstalled with a
        // new id, phone asleep) while another device claims the sound: follow it
        // now instead of waiting for the old one to expire.
        if (currentRemote != null) {
            val heardAt = synchronized(seen) { seen[currentRemote]?.lastSeenMs } ?: 0L
            val claimIsReal = claimantPlaying == true || (claimantPlaying == null && incomingTerm > ownerTerm)
            if (System.currentTimeMillis() - heardAt > SILENT_OWNER_MS && claimIsReal) return true
        }
        // Opening Android can briefly expose a paused queue restored from its previous process.
        // An unsolicited state/claim must not replace Windows audio that is already progressing.
        // Explicit Windows -> Android transfer sets currentRemote first and is accepted above.
        if (currentRemote == null && playbackProvider?.invoke()?.isPlaying == true) return false
        val localHasTrack = localOwnerActive
        if (currentRemote == null && !localHasTrack) return true
        val currentOwner = currentRemote ?: localId
        return incomingTerm > ownerTerm || (incomingTerm == ownerTerm && sender > currentOwner)
    }

    private fun clearRemoteOwnership(owner: String) {
        if (_remoteOwnerId.value != owner) return
        _remoteOwnerId.value = null
        localOwnerActive = false
        remoteStateHandler?.invoke(
            ConnectPlaybackState(owner, null, 0L, 0L, false, System.currentTimeMillis()),
        )
    }

    private fun fingerprint(accountKey: String): String = MessageDigest.getInstance("SHA-256")
        .digest("record-labs-connect-v2:${accountKey.trim().lowercase()}".toByteArray())
        .joinToString("") { "%02x".format(it) }

    private fun ipv4Networks(): List<NetworkInterface> = runCatching {
        NetworkInterface.getNetworkInterfaces().toList().filter { network ->
            network.isUp && !network.isLoopback &&
                network.inetAddresses.toList().any { it is Inet4Address }
        }
    }.getOrDefault(emptyList())

    private fun publishPacketCounts() {
        _discoveryStatus.value = _discoveryStatus.value.copy(
            sentPackets = sentPackets.get(),
            receivedPackets = receivedPackets.get(),
            rejectedAccountPackets = rejectedAccountPackets.get(),
        )
    }

}
