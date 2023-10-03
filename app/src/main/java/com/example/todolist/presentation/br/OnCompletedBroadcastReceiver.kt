package com.example.todolist.presentation.br

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.data.model.toTaskInfo
import com.example.todolist.data.repository.TaskCategoryRepositoryImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnCompletedBroadcastReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: TaskCategoryRepositoryImpl

    override fun onReceive(p0: Context?, p1: Intent?) {
        val taskInfo = p1?.getStringExtra("task_info")?.toTaskInfo()
        var taskInfoCopy: TaskInfo? = null
        if (taskInfo != null) {
            taskInfoCopy = taskInfo.copy(status = true)
        }
        CoroutineScope(IO).launch {
            taskInfoCopy?.let {
                repository.updateTaskStatus(it)
            }
        }
        if (p0 != null && taskInfo != null) {
            NotificationManagerCompat.from(p0).cancel(null, taskInfo.id)
        }
    }
}