package com.recordlabs.desktop.data

import com.recordlabs.desktop.platform.IoDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * How loud each song is mastered, from YouTube Music's player answer
 * (playerConfig.audioConfig.loudnessDb: dB above its reference level, below 0 when
 * quieter). Recorded wherever a player answer is read; [fetcher] asks for it when a
 * song plays whose answer was not read here (a cached stream, another source).
 */
object TrackLoudness {
    private val known = HashMap<String, Double>()
    private val asked = HashSet<String>()
    private val lock = Any()
    private val scope = CoroutineScope(SupervisorJob() + IoDispatcher)

    /** Set by the repository: the loudness of a video id, or null. */
    @kotlin.concurrent.Volatile var fetcher: (suspend (String) -> Double?)? = null

    fun record(videoId: String, loudnessDb: Double?) {
        if (loudnessDb == null || loudnessDb.isNaN()) return
        com.recordlabs.desktop.platform.locked(lock) { known[videoId] = loudnessDb }
    }

    fun of(videoId: String): Double? = com.recordlabs.desktop.platform.locked(lock) { known[videoId] }

    /** Looks [videoId]'s loudness up once (in the background) when it is not known yet. */
    fun request(videoId: String) {
        if (videoId.startsWith("local:")) return
        val first = com.recordlabs.desktop.platform.locked(lock) { videoId !in known && asked.add(videoId) }
        if (!first) return
        val fetch = fetcher ?: return
        scope.launch { record(videoId, runCatching { fetch(videoId) }.getOrNull()) }
    }
}

/**
 * Settings > Player and audio > Normalize volume and Crossfade, the same on every
 * platform, over whichever player plays ([inner]):
 * - Normalize volume: a song mastered louder than YouTube Music's reference level is
 *   turned down by as much (as YouTube Music does), so songs play at an even loudness.
 * - Crossfade: a song fades out over its last seconds and the next fades in over its
 *   first (the players play one song at a time, so the two meet in the middle instead
 *   of overlapping).
 * The volume the app sets is kept as it is; these are applied on top of it. A few times
 * a second the position is looked at ([tick]), in the background (not tied to the app's
 * window: playback goes on without it on phones).
 */
class EffectsPlaybackEngine(private val inner: PlaybackEngine) : PlaybackEngine by inner {
    @kotlin.concurrent.Volatile private var userVolume = 1f
    @kotlin.concurrent.Volatile private var applied = -1f
    @kotlin.concurrent.Volatile private var fadingIn = false
    @kotlin.concurrent.Volatile private var playingId: String? = null
    private val ticker = CoroutineScope(SupervisorJob() + IoDispatcher)

    init {
        ticker.launch {
            while (true) {
                kotlinx.coroutines.delay(80)
                runCatching { tick() }
            }
        }
    }

    override fun release() {
        ticker.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        inner.release()
    }

    override fun presetState(volume: Float, tempo: Float, pitch: Int) {
        userVolume = volume.coerceIn(0f, 1f)
        applied = effectiveVolume()
        inner.presetState(applied, tempo, pitch)
    }

    override fun setVolume(volume: Float) {
        userVolume = volume.coerceIn(0f, 1f)
        apply(force = true)
    }

    override fun play(track: Track, url: String, startPositionMs: Long) {
        playingId = track.id
        // A song starting from its beginning fades in (not one picked up part way).
        fadingIn = startPositionMs < 1_000L
        if (AppSettings.normalizeVolume.value) TrackLoudness.request(track.id)
        applied = effectiveVolume(positionMs = startPositionMs, durationMs = 0L)
        inner.setVolume(applied)
        inner.play(track, url, startPositionMs)
    }

    /** Follows the position: the fades and a loudness found after the song started. */
    fun tick() {
        // Nothing until the app has set a volume and a song is loaded (no player started early).
        if (applied < 0f) return
        val id = inner.state.value.trackId ?: inner.currentTrack?.id ?: return
        if (id != playingId) {
            // The player moved on by itself (or Connect handed it a song).
            playingId = id
            fadingIn = inner.state.value.positionMs < 1_000L
            if (AppSettings.normalizeVolume.value) TrackLoudness.request(id)
        }
        apply(force = false)
    }

    private fun apply(force: Boolean) {
        val target = effectiveVolume()
        if (force || kotlin.math.abs(target - applied) > 0.004f || (target == 0f) != (applied == 0f)) {
            applied = target
            inner.setVolume(target)
        }
    }

    private fun effectiveVolume(
        positionMs: Long = inner.state.value.positionMs,
        durationMs: Long = inner.state.value.durationMs,
    ): Float = (userVolume * normalizationGain() * fade(positionMs, durationMs)).coerceIn(0f, 1f)

    private fun normalizationGain(): Float {
        if (!AppSettings.normalizeVolume.value) return 1f
        val db = playingId?.let(TrackLoudness::of) ?: return 1f
        // Only turned down (a player cannot go above full volume), at most by 18 dB.
        return 10.0.pow(-db.coerceIn(0.0, 18.0) / 20.0).toFloat()
    }

    private fun fade(positionMs: Long, durationMs: Long): Float {
        val seconds = AppSettings.crossfadeSeconds.value
        if (seconds <= 0) return 1f
        // A short song fades over at most a third of itself at each end.
        val fadeMs = (seconds * 1_000L).let { if (durationMs > 0L) minOf(it, durationMs / 3) else it }.coerceAtLeast(1L)
        var level = 1f
        if (fadingIn) {
            if (positionMs >= fadeMs) fadingIn = false
            else level = minOf(level, (positionMs.coerceAtLeast(0L).toFloat() / fadeMs))
        }
        if (durationMs > 0L) {
            val left = durationMs - positionMs
            if (left < fadeMs) level = minOf(level, (left.coerceAtLeast(0L).toFloat() / fadeMs))
        }
        // Even loudness along the fade (a straight line sounds like it drops at the end).
        return sin(level.coerceIn(0f, 1f) * PI.toFloat() / 2f)
    }
}
