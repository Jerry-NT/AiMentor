package com.example.aisupabase.controllers

import android.app.*
import android.content.Context
import android.text.format.DateFormat
import androidx.work.*
import com.example.aisupabase.config.channelID
import com.example.aisupabase.config.NotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

object notification_crud {

    private const val WORK_NAME = "daily_notification_work"

    fun scheduleDailyNotification(
        context: Context,
        title: String,
        message: String,
        hour: Int,
        minute: Int
    ) {
        // Tạo input data cho Worker
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        // Tính toán thời gian delay cho lần chạy đầu tiên
        val initialDelay = calculateInitialDelay(hour, minute)

        // Tạo PeriodicWorkRequest với chu kỳ 24 giờ
        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInputData(inputData)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_notification")
            .build()

        // Enqueue work với REPLACE policy để thay thế work cũ nếu có
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )

        showScheduleAlert(context, hour, minute, title, message)
    }

    fun scheduleOneTimeNotification(
        context: Context,
        title: String,
        message: String,
        delayInMillis: Long
    ) {
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .addTag("one_time_notification")
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)

        val futureTime = System.currentTimeMillis() + delayInMillis
        showAlert(context, futureTime, title, message)
    }

    fun cancelDailyNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        showCancelAlert(context)
    }

    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("daily_notification")
        WorkManager.getInstance(context).cancelAllWorkByTag("one_time_notification")
    }

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Nếu thời gian target đã qua trong ngày hôm nay, lên lịch cho ngày mai
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        return targetTime.timeInMillis - currentTime.timeInMillis
    }

    private fun showScheduleAlert(context: Context, hour: Int, minute: Int, title: String, message: String) {
        val timeString = String.format("%02d:%02d", hour, minute)

        AlertDialog.Builder(context)
            .setTitle("Thông báo hằng ngày đã được lên lịch")
            .setMessage(
                "Tiêu đề: $title\n" +
                        "Nội dung: $message\n" +
                        "Thời gian: $timeString hằng ngày"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAlert(context: Context, time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = DateFormat.getLongDateFormat(context)
        val timeFormat = DateFormat.getTimeFormat(context)

        AlertDialog.Builder(context)
            .setTitle("Thông báo đã được lên lịch")
            .setMessage(
                "Tiêu đề: $title\n" +
                        "Nội dung: $message\n" +
                        "Thời gian: ${dateFormat.format(date)} ${timeFormat.format(date)}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showCancelAlert(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Đã hủy")
            .setMessage("Thông báo hằng ngày đã được hủy")
            .setPositiveButton("OK", null)
            .show()
    }

    fun createNotificationChannel(context: Context) {
        val name = "Thông báo hằng ngày"
        val desc = "Kênh thông báo cho các thông báo hằng ngày"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance).apply {
            description = desc
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}