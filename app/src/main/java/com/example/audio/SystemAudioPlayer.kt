package com.example.audio

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SystemAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState = _playbackState.asStateFlow()

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val progress: Float = 0f,
        val currentPosition: Long = 0,
        val duration: Long = 0
    )

    fun play(path: String) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepareAsync()
            setOnPreparedListener {
                start()
                _playbackState.update { it.copy(isPlaying = true, duration = duration.toLong()) }
                startProgressTracker()
            }
            setOnCompletionListener {
                _playbackState.update { it.copy(isPlaying = false, progress = 1f, currentPosition = duration.toLong()) }
                stopProgressTracker()
            }
            setOnErrorListener { _, _, _ ->
                _playbackState.update { it.copy(isPlaying = false) }
                stopProgressTracker()
                true
            }
        }
    }

    fun togglePlay() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.update { state -> state.copy(isPlaying = false) }
                stopProgressTracker()
            } else {
                it.start()
                _playbackState.update { state -> state.copy(isPlaying = true) }
                startProgressTracker()
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val pos = mp.currentPosition.toLong()
                        val dur = mp.duration.toLong()
                        _playbackState.update { 
                            it.copy(
                                progress = if (dur > 0) pos.toFloat() / dur.toFloat() else 0f,
                                currentPosition = pos,
                                duration = dur
                            ) 
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun stop() {
        stopProgressTracker()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.update { PlaybackState() }
    }

    fun release() {
        stop()
    }
}
