package com.example.aisupabase.config

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.aisupabase.R
import com.example.aisupabase.config.channelID
import com.example.aisupabase.config.notificationID

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Thông báo"
        val message = inputData.getString("message") ?: "Đây là thông báo hằng ngày"
        val notificationId = inputData.getInt("notificationId", notificationID)

        showNotification(title, message, notificationId)

        return Result.success()
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}