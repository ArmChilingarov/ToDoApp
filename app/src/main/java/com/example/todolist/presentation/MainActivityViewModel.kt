package com.example.todolist.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.NoOfTaskForEachCategory
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.domain.TaskCategoryRepository
import com.example.todolist.presentation.MainActivityViewModel.Sort.CATEGORY
import com.example.todolist.presentation.MainActivityViewModel.Sort.DATE
import com.example.todolist.presentation.MainActivityViewModel.Sort.NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: TaskCategoryRepository
) : ViewModel() {

    private val _tasksOfCategories = MutableLiveData<List<NoOfTaskForEachCategory>>()
    val tasksOfCategories: LiveData<List<NoOfTaskForEachCategory>> = _tasksOfCategories

    private val _tasks = MutableLiveData<List<TaskInfo>>()

    private val _filteredTasks = MediatorLiveData<List<TaskInfo>>()
    val filteredTasks: LiveData<List<TaskInfo>> = _filteredTasks

    private val searchText = MutableLiveData<String>()

    init {
        _filteredTasks.addSource(searchText) {
            processTasks(_tasks.value.orEmpty(), it.orEmpty())
        }
    }

    fun updateTaskStatus(task: TaskInfo) {
        viewModelScope.launch(IO) {
            repository.updateTaskStatus(task)
        }
    }

    fun deleteTask(task: TaskInfo) {
        viewModelScope.launch(IO) {
            repository.deleteTask(task)
        }
    }

    fun insertTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        viewModelScope.launch(IO) {
            repository.insertTaskAndCategory(taskInfo, categoryInfo)
        }
    }

    fun updateTaskAndAddCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        viewModelScope.launch(IO) {
            repository.updateTaskAndAddCategory(taskInfo, categoryInfo)
        }
    }

    fun listenDataStoreUpdates() {
        viewModelScope.launch(IO) {
            repository.getDateStore().collect {
                // receives every update related to datastore
                withContext(Main) {
                    _tasks.value = it.tasks
                }
                processTasks(it.tasks, searchText.value)
                processCategoryTasks(it.tasks)
            }
        }
    }

    private fun processCategoryTasks(tasks: PersistentList<TaskInfo>) {
        val newTaskList = mutableListOf<NoOfTaskForEachCategory>()
        tasks.filter {
            !it.status
        }.groupingBy {
            it.category
        }.eachCount().map {
            newTaskList.add(
                NoOfTaskForEachCategory(
                    it.key.categoryInformation,
                    it.key.color,
                    it.value
                )
            )
        }
        return _tasksOfCategories.postValue(newTaskList)
    }

    private fun processTasks(tasks: List<TaskInfo>, searchText: String?) {
        return _filteredTasks.postValue(
            tasks.filter { task ->
                !task.status && task.description.contains(searchText.orEmpty()).takeIf {
                    !searchText.isNullOrEmpty()
                } ?: true
            }
        )
    }

    fun updateTaskAndAddDeleteCategory(
        taskInfo: TaskInfo,
        categoryInfoAdd: CategoryInfo,
        categoryInfoDelete: CategoryInfo
    ) {
        viewModelScope.launch(IO) {
            repository.updateTaskAndAddDeleteCategory(taskInfo, categoryInfoAdd, categoryInfoDelete)
        }
    }

    fun deleteTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        viewModelScope.launch(IO) {
            repository.deleteTaskAndCategory(taskInfo, categoryInfo)
        }
    }

    fun getCompletedTask(): LiveData<List<TaskInfo>> {
        return repository.getCompletedTask()
    }

    fun getUncompletedTaskOfCategory(category: String): LiveData<List<TaskInfo>> {
        return repository.getUncompletedTaskOfCategory(category)
    }


    fun getCategories(): LiveData<Set<CategoryInfo>> {
        return repository.getCategories()
    }

    suspend fun getCountOfCategory(category: String): Int {
        var count: Int
        coroutineScope() {
            count = withContext(IO) { repository.getCountOfCategory(category) }
        }
        return count
    }

    fun performSearch(text: CharSequence?) {
        searchText.value = text.toString()
    }

    fun sortTasks(sort: Sort) {
        return _filteredTasks.postValue(
            _tasks.value?.filter { task ->
                !task.status && task.description.contains(searchText.value.orEmpty()).takeIf {
                    !searchText.value.isNullOrEmpty()
                } ?: true
            }?.sortedBy {
                when (sort) {
                    DATE -> it.date.toString()
                    NAME -> it.title
                    CATEGORY -> it.category.categoryInformation
                }
            }
        )


    }

    enum class Sort {
        DATE, NAME, CATEGORY
    }
}