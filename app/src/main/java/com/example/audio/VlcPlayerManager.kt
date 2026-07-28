package com.example.audio

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class VlcPlayerManager(context: Context) {
    private val libVlc = LibVLC(context)
    private val mediaPlayer = MediaPlayer(libVlc)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState = _playbackState.asStateFlow()

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val progress: Float = 0f,
        val currentPosition: Long = 0,
        val duration: Long = 0
    )

    init {
        mediaPlayer.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.PositionChanged -> {
                    _playbackState.update { 
                        it.copy(
                            progress = mediaPlayer.position,
                            currentPosition = mediaPlayer.time
                        ) 
                    }
                }
                MediaPlayer.Event.LengthChanged -> {
                    _playbackState.update { it.copy(duration = mediaPlayer.length) }
                }
                MediaPlayer.Event.Playing -> {
                    _playbackState.update { it.copy(isPlaying = true) }
                }
                MediaPlayer.Event.Paused, MediaPlayer.Event.Stopped, MediaPlayer.Event.EndReached -> {
                    _playbackState.update { it.copy(isPlaying = false) }
                }
            }
        }
    }

    fun play(path: String) {
        val media = Media(libVlc, path)
        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()
    }

    fun togglePlay() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.play()
        }
    }

    fun stop() {
        mediaPlayer.stop()
    }

    fun release() {
        mediaPlayer.release()
        libVlc.release()
    }
}
