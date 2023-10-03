package com.example.todolist.presentation.br

import android.Manifest
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.app.PendingIntent.getBroadcast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todolist.R
import com.example.todolist.data.model.toTaskInfo
import com.example.todolist.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject


@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    private var notificationManager: NotificationManagerCompat? = null

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(p0: Context?, p1: Intent?) {
        val taskInfo = p1?.getStringExtra("task_info")?.toTaskInfo()
        if (sharedPreferences.getBoolean(taskInfo?.priority.toString(), true)) {
            val tapResultIntent = Intent(p0, MainActivity::class.java)
            tapResultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent: PendingIntent =
                getActivity(p0, 0, tapResultIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)

            val intent1 = Intent(p0, OnCompletedBroadcastReceiver::class.java).apply {
                putExtra("task_info", Json.encodeToString(taskInfo))
            }
            val pendingIntent1: PendingIntent? =
                taskInfo?.let {
                    getBroadcast(
                        p0,
                        it.id,
                        intent1,
                        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
                    )
                }
            val action1: NotificationCompat.Action =
                NotificationCompat.Action.Builder(0, "Completed", pendingIntent1).build()

            val notification = p0?.let {
                NotificationCompat.Builder(it, "to_do_list")
                    .setContentTitle("Task Reminder")
                    .setContentText(taskInfo?.description)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .addAction(action1)
                    .build()
            }
            notificationManager = p0?.let { NotificationManagerCompat.from(it) }
            notification?.let {
                taskInfo?.let { it1 ->
                    if (ActivityCompat.checkSelfPermission(
                            p0,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationManager?.notify(it1.id, it)
                    }

                }
            }
        }
    }
}