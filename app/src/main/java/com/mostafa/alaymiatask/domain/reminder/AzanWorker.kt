package com.mostafa.alaymiatask.domain.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mostafa.alaymiatask.R
import com.mostafa.alaymiatask.domain.reminder.NotificationActionReceiver.Companion.CANCEL_NOTIFICATION_ACTION
import java.lang.System.currentTimeMillis
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar

class AzanWorker(
    private val context: Context,
    private val workerParams: WorkerParameters,
    ) : CoroutineWorker(context, workerParams) {


    private lateinit var notificationManager: NotificationManager


    override suspend fun doWork(): Result {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val prayerTimesInMillis = inputData.getLongArray(KEY_PRAYER_TIMES_IN_MILLIS)?.toList()

        if (prayerTimesInMillis != null) {
            performAzanTask(prayerTimesInMillis)
        }


        return Result.success()
    }


    private fun performAzanTask(prayerTimesInMillis: List<Long>) {
        val currentTime = currentTimeMillis() + 7200000
        for (i in prayerTimesInMillis.indices) {
            val prayerTime = prayerTimesInMillis[i]
            if (prayerTime == currentTime) {
                val prayerName = getPrayerName(i)
                updateForegroundNotification(
                    "$prayerName Prayer Time",
                    "It's time for $prayerName prayer!"
                )

            }
        }
    }

    private fun getPrayerName(index: Int): String {
        val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        return prayerNames.getOrNull(index) ?: ""
    }

    private fun updateForegroundNotification(title: String, message: String) {
        val channelId = "prayer_notification_channel"
        val channelName = "Prayer Timings"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationId = 123

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)


        val cancelIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = CANCEL_NOTIFICATION_ACTION
        }
        val pendingCancelIntent = PendingIntent.getBroadcast(
            context,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        val cancelAction = NotificationCompat.Action.Builder(
            R.drawable.mosque_svgrepo_com,
            "Cancel",
            pendingCancelIntent
        ).build()
        notificationBuilder.addAction(cancelAction)


        val notification = notificationBuilder.build()
        notificationManager.notify(notificationId, notification)

        val soundUri: Int = if (title == "Fajr") {
            R.raw.adzan_fajr
        } else {
            R.raw.adzan_makkah
        }


        AzanPlayer.play(context, soundUri)
    }




    companion object {
        const val KEY_PRAYER_TIMES_IN_MILLIS = "prayer_times_in_millis"
        const val NOTIFICATION_ID = 0
    }

}
