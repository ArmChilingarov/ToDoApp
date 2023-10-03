package com.example.todolist.data.db

import androidx.datastore.core.DataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.TaskDataStore
import com.example.todolist.data.model.TaskInfo
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Date

class TaskCategoryImpl(
    private val dataStore: DataStore<TaskDataStore>
) : TaskCategory {
    override suspend fun insertTask(task: TaskInfo) {
        dataStore.updateData { datastore ->
            datastore.copy(tasks = datastore.tasks.add(task))
        }
    }

    override suspend fun updateTaskStatus(task: TaskInfo) {
        dataStore.updateData { datastore ->
            datastore.copy(tasks = datastore.tasks.mutate { taskList ->
                taskList[taskList.indexOfFirst { it.id == task.id }] = task
            })
        }
    }

    override suspend fun insertCategory(categoryInfo: CategoryInfo) {
        dataStore.updateData { datastore ->
            datastore.copy(categories = datastore.categories.add(categoryInfo))
        }
    }

    override suspend fun deleteTask(task: TaskInfo) {
        dataStore.updateData { datastore ->
            datastore.copy(tasks = datastore.tasks.removeAll { it.id == task.id })
        }
    }

    override suspend fun deleteCategory(categoryInfo: CategoryInfo) {
        dataStore.updateData { datastore ->
            datastore.copy(categories = datastore.categories.removeAll { it.categoryInformation == categoryInfo.categoryInformation })
        }
    }

    override fun getCompletedTask(): LiveData<List<TaskInfo>> {
        return dataStore.data.map {
            it.tasks.filter {
                it.status
            }
        }.asLiveData()
    }

    override fun getDateStore(): Flow<TaskDataStore> {
        return dataStore.data
    }

    override fun getUncompletedTaskOfCategory(category: String): LiveData<List<TaskInfo>> {
        return dataStore.data.map {
            it.tasks.filter {
                !it.status && it.category.categoryInformation == category
            }
        }.asLiveData()
    }

    override fun getCategories(): LiveData<Set<CategoryInfo>> {
        return dataStore.data.map {
            it.categories
        }.asLiveData()
    }

    override fun getCountOfCategory(category: String): Int {
        return runBlocking {
            dataStore.data.map {
                it.tasks.count {
                    it.category.categoryInformation == category
                }
            }.first()
        }
    }

    override fun getActiveAlarms(currentTime: Date): List<TaskInfo> {
        return runBlocking {
            dataStore.data.map {
                it.tasks.filter {
                    it.status && it.date > currentTime
                }
            }.first()
        }
    }
}