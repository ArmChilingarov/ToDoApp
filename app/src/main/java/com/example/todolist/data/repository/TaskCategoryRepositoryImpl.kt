package com.example.todolist.data.repository

import androidx.lifecycle.LiveData
import com.example.todolist.data.db.TaskCategory
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.TaskDataStore
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.domain.TaskCategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class TaskCategoryRepositoryImpl @Inject constructor(private val taskCategory: TaskCategory) :
    TaskCategoryRepository {

    override suspend fun updateTaskStatus(task: TaskInfo) {
        return taskCategory.updateTaskStatus(task)
    }

    override suspend fun deleteTask(task: TaskInfo) {
        taskCategory.deleteTask(task)
    }

    override suspend fun insertTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        taskCategory.insertTaskAndCategory(taskInfo, categoryInfo)
    }

    override suspend fun deleteTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        taskCategory.deleteTaskAndCategory(taskInfo, categoryInfo)
    }

    override suspend fun updateTaskAndAddDeleteCategory(
        taskInfo: TaskInfo,
        categoryInfoAdd: CategoryInfo,
        categoryInfoDelete: CategoryInfo
    ) {
        taskCategory.updateTaskAndAddDeleteCategory(taskInfo, categoryInfoAdd, categoryInfoDelete)
    }

    override suspend fun updateTaskAndAddCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        taskCategory.updateTaskAndAddCategory(taskInfo, categoryInfo)
    }

    override fun getDateStore(): Flow<TaskDataStore> {
        return taskCategory.getDateStore()
    }

    override fun getCompletedTask(): LiveData<List<TaskInfo>> =
        taskCategory.getCompletedTask()

    override fun getUncompletedTaskOfCategory(category: String): LiveData<List<TaskInfo>> =
        taskCategory.getUncompletedTaskOfCategory(category)

    override fun getCategories(): LiveData<Set<CategoryInfo>> = taskCategory.getCategories()
    override suspend fun getCountOfCategory(category: String): Int =
        taskCategory.getCountOfCategory(category)

    override suspend fun getActiveAlarms(currentTime: Date): List<TaskInfo> {
        var list: List<TaskInfo>
        coroutineScope {
            list = withContext(Dispatchers.IO) { taskCategory.getActiveAlarms(currentTime) }
        }
        return list
    }
}