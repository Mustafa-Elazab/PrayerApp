package com.mostafa.alaymiatask.domain.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mostafa.alaymiatask.domain.reminder.AzanWorker.Companion.NOTIFICATION_ID

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == CANCEL_NOTIFICATION_ACTION) {
            cancelNotification(context)
            stopSoundPlayback()
        }
    }

    private fun cancelNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun stopSoundPlayback() {
        if (AzanPlayer.isPlaying()) {
            AzanPlayer.stop()
        }
    }

    companion object {
        const val CANCEL_NOTIFICATION_ACTION = "CANCEL_NOTIFICATION"
    }
}

