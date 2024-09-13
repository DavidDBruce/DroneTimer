package com.rychotech.dronetimer

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.rychotech.dronetimer.databinding.ActivityMainBinding
import java.lang.String.format
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        binding.submitButton.setOnClickListener {
            scheduleNotification(
                applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager,
                applicationContext,
                binding.titleET.text.toString(),
                binding.messageET.text.toString(),
                getTimeFromInput()
            )
        }
        binding.droneHackButton.setOnClickListener {
            val currentTime = getCurrentTime();
            val nextHour = getNextHour()
            val dateFormat = android.text.format.DateFormat.getLongDateFormat(applicationContext)
            val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)
            scheduleNotification(
                applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager,
                applicationContext,
                "Drone Hack",
                format(
                    "Last hack was: %s\nTime to Drone Hack!",
                    dateFormat.format(currentTime) + " " + timeFormat.format(currentTime)
                ),
                nextHour
            )
        }
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)
        AlertDialog.Builder(this)
            .setTitle("Notification Scheduled")
            .setMessage(
                "Title: " + title +
                        "\nMessage: " + message +
                        "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date)
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun getTimeFromInput(): Long {
        val minute = binding.timePicker.minute
        val hour = binding.timePicker.hour
        val day = binding.datePicker.dayOfMonth
        val month = binding.datePicker.month
        val year = binding.datePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    private fun getNextHour(): Long {
        val currentTime = LocalDateTime.now()
        val hourFuture = currentTime.plusHours(1)
        return hourFuture.toInstant(ZoneId.systemDefault().rules.getOffset(currentTime)).toEpochMilli()
    }

    private fun getCurrentTime(): Long {
        val currentTime = LocalDateTime.now()
        return currentTime.toInstant(ZoneId.systemDefault().rules.getOffset(currentTime)).toEpochMilli()
    }

    private fun createNotificationChannel() {
        val name = "Notif Channel"
        val desc = "Description of Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        fun scheduleNotification(
            alarmManager: AlarmManager,
            applicationContext: Context, title: String, message: String, time: Long
        ) {
            val intent = Intent(applicationContext, Notification::class.java)
            intent.putExtra(titleExtra, title)
            intent.putExtra(titleExtra, message)

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            } catch (securityException: SecurityException) {
                Toast.makeText(
                    applicationContext,
                    format("Notification \"%s\" Failed", title),
                    Toast.LENGTH_SHORT
                ).show()
            }
            Toast.makeText(
                applicationContext,
                format("Notification \"%s\" Scheduled", title),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}