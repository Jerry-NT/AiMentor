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

    data class NotificationDetail(
        val id: Int,
        val title: String,
        val message: String,
        val hour: Int,
        val minute: Int,
        val state: String,
        val tags: List<String>
    )

    fun scheduleDailyNotification(
        context: Context,
        title: String,
        message: String,
        hour: Int,
        minute: Int,
        notificationId: Int = generateNotificationId()
    ) {
        // Tạo unique work name cho mỗi thông báo
        val workName = "daily_notification_${hour}"

        // Tạo input data cho Worker
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putInt("notificationId", notificationId)
            .putInt("hour", hour)
            .putInt("minute", minute)
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
            .addTag("notification_$notificationId")
            .build()

        // Enqueue work với KEEP policy để giữ lại các work khác
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
        saveNotificationInfo(context, notificationId, title, message, hour, minute, workName)
        showScheduleAlert(context, hour, minute, title, message, notificationId)
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

    fun cancelDailyNotification(context: Context, notificationId: Int? = null) {
        if (notificationId != null) {
            // Hủy một thông báo cụ thể
            WorkManager.getInstance(context).cancelAllWorkByTag("notification_$notificationId")
            // Xóa thông tin khỏi SharedPreferences
            removeNotificationInfo(context, notificationId)
            showCancelAlert(context, "Thông báo #$notificationId đã được hủy")
        } else {
            // Hủy tất cả thông báo hằng ngày
            WorkManager.getInstance(context).cancelAllWorkByTag("daily_notification")
            // Xóa tất cả thông tin khỏi SharedPreferences
            clearAllNotificationInfo(context)
            showCancelAlert(context, "Tất cả thông báo hằng ngày đã được hủy")
        }
    }

    fun getScheduledNotificationDetails(context: Context): List<NotificationDetail> {
        return try {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosByTag("daily_notification")
                .get()

            // Lấy thông tin từ SharedPreferences
            val savedNotifications = getSavedNotificationInfo(context)

            // Kết hợp WorkInfo với thông tin đã lưu
            workInfos.mapNotNull { workInfo ->
                // Tìm thông tin tương ứng từ SharedPreferences
                val workTags = workInfo.tags
                val notificationTag = workTags.find { it.startsWith("notification_") }
                val notificationId = notificationTag?.substringAfter("notification_")?.toIntOrNull()

                val savedInfo = savedNotifications.find { it.id == notificationId }
                savedInfo?.let {
                    NotificationDetail(
                        id = it.id,
                        title = it.title,
                        message = it.message,
                        hour = it.hour,
                        minute = it.minute,
                        state = workInfo.state.name,
                        tags = workInfo.tags.toList()
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("daily_notification")
        WorkManager.getInstance(context).cancelAllWorkByTag("one_time_notification")
        clearAllNotificationInfo(context)
        showCancelAlert(context, "Tất cả thông báo đã được hủy")
    }

    private fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }
    // SharedPreferences methods
    private fun saveNotificationInfo(
        context: Context,
        id: Int,
        title: String,
        message: String,
        hour: Int,
        minute: Int,
        workName: String
    ) {
        val prefs = context.getSharedPreferences("scheduled_notifications", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val notificationData = "$id|$title|$message|$hour|$minute|$workName"
        editor.putString("notification_$id", notificationData)
        editor.apply()
    }

    private fun getSavedNotificationInfo(context: Context): List<NotificationDetail> {
        val prefs = context.getSharedPreferences("scheduled_notifications", Context.MODE_PRIVATE)
        val allPrefs = prefs.all

        return allPrefs.mapNotNull { (key, value) ->
            if (key.startsWith("notification_") && value is String) {
                val id = key.substringAfter("notification_").toIntOrNull()
                val parts = value.split("|")
                // Correct mapping: id|title|message|hour|minute|workName
                if (id != null && parts.size >= 6) {
                    NotificationDetail(
                        id = id,
                        title = parts[1],
                        message = parts[2],
                        hour = parts[3].toIntOrNull() ?: 0,
                        minute = parts[4].toIntOrNull() ?: 0,
                        state = "SAVED",
                        tags = emptyList()
                    )
                } else null
            } else null
        }
    }

    private fun removeNotificationInfo(context: Context, id: Int) {
        val prefs = context.getSharedPreferences("scheduled_notifications", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove("notification_$id")
        editor.apply()
    }

    private fun clearAllNotificationInfo(context: Context) {
        val prefs = context.getSharedPreferences("scheduled_notifications", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
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

    private fun showScheduleAlert(context: Context, hour: Int, minute: Int, title: String, message: String, notificationId: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)

        AlertDialog.Builder(context)
            .setTitle("Thông báo hằng ngày đã được lên lịch")
            .setMessage(
                "ID: #$notificationId\n" +
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

    private fun showCancelAlert(context: Context, message: String = "Thông báo đã được hủy") {
        AlertDialog.Builder(context)
            .setTitle("Đã hủy")
            .setMessage(message)
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