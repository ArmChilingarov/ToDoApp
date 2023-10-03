package com.example.todolist.data.db

import androidx.lifecycle.LiveData
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.TaskDataStore
import com.example.todolist.data.model.TaskInfo
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TaskCategory {
    suspend fun insertTask(task: TaskInfo)

    suspend fun updateTaskStatus(task: TaskInfo)

    suspend fun insertCategory(categoryInfo: CategoryInfo)

    suspend fun deleteTask(task: TaskInfo)

    suspend fun deleteCategory(categoryInfo: CategoryInfo)

    suspend fun insertTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        insertTask(taskInfo)
        insertCategory(categoryInfo)
    }

    suspend fun updateTaskAndAddCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        updateTaskStatus(taskInfo)
        insertCategory(categoryInfo)
    }

    suspend fun updateTaskAndAddDeleteCategory(
        taskInfo: TaskInfo,
        categoryInfoAdd: CategoryInfo,
        categoryInfoDelete: CategoryInfo
    ) {
        updateTaskStatus(taskInfo)
        insertCategory(categoryInfoAdd)
        deleteCategory(categoryInfoDelete)
    }

    suspend fun deleteTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        deleteTask(taskInfo)
        deleteCategory(categoryInfo)
    }

    fun getCompletedTask(): LiveData<List<TaskInfo>>

    fun getDateStore(): Flow<TaskDataStore>
    fun getUncompletedTaskOfCategory(category: String): LiveData<List<TaskInfo>>

    fun getCategories(): LiveData<Set<CategoryInfo>>
    fun getCountOfCategory(category: String): Int

    fun getActiveAlarms(currentTime: Date): List<TaskInfo>
}