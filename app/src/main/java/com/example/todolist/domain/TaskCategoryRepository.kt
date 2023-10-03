package com.example.todolist.domain

import androidx.lifecycle.LiveData
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.TaskDataStore
import com.example.todolist.data.model.TaskInfo
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TaskCategoryRepository {
    suspend fun updateTaskStatus(task: TaskInfo)
    suspend fun deleteTask(task: TaskInfo)
    suspend fun insertTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo)
    suspend fun deleteTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo)
    suspend fun updateTaskAndAddDeleteCategory(
        taskInfo: TaskInfo,
        categoryInfoAdd: CategoryInfo,
        categoryInfoDelete: CategoryInfo
    )

    fun getDateStore(): Flow<TaskDataStore>
    suspend fun updateTaskAndAddCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo)
    fun getCompletedTask(): LiveData<List<TaskInfo>>
    fun getUncompletedTaskOfCategory(category: String): LiveData<List<TaskInfo>>
    fun getCategories(): LiveData<Set<CategoryInfo>>
    suspend fun getCountOfCategory(category: String): Int

    suspend fun getActiveAlarms(currentTime: Date): List<TaskInfo>
}