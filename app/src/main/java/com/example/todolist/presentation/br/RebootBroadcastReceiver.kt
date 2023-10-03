package com.example.todolist.presentation.br

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.data.model.toJson
import com.example.todolist.data.repository.TaskCategoryRepositoryImpl
import com.example.todolist.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class RebootBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: TaskCategoryRepositoryImpl
    override fun onReceive(context: Context?, p1: Intent?) {
        when (p1?.action) {
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_BOOT_COMPLETED -> {
                val time = Date()
                CoroutineScope(Main).launch {
                    val list = repository.getActiveAlarms(time)
                    for (taskInfo in list) setAlarm(taskInfo, context)
                }
            }
        }
    }

    private fun setAlarm(taskInfo: TaskInfo, context: Context?) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("task_info", taskInfo.toJson())
        val pendingIntent =
            PendingIntent.getBroadcast(context, taskInfo.id, intent, PendingIntent.FLAG_IMMUTABLE)
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        val basicPendingIntent = PendingIntent.getActivity(
            context,
            taskInfo.id,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val clockInfo = AlarmManager.AlarmClockInfo(taskInfo.date.time, basicPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(clockInfo, pendingIntent)
            }
        }
    }

}