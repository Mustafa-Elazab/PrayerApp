package com.mostafa.alaymiatask.domain.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.mostafa.alaymiatask.R

object AzanPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context,soundResId: Int) {
        if (mediaPlayer == null) {
            val soundUri = Uri.parse("android.resource://${context.packageName}/${soundResId}")
            mediaPlayer = MediaPlayer.create(context, soundUri)
            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer?.setOnCompletionListener {
                stop()
            }
        }

        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.let {
            it.stop()
            it.release()
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}
