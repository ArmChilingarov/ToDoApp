@file:UseSerializers(
    DateSerializer::class,
    CategorySetSerializer::class,
    TaskListSerializer::class
)

package com.example.todolist.data.model

import com.example.todolist.data.db.DateSerializer
import com.example.todolist.data.util.CategorySetSerializer
import com.example.todolist.data.util.TaskListSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date

@Serializable
data class TaskDataStore(
    val tasks: PersistentList<TaskInfo> = persistentListOf(),
    val categories: PersistentSet<CategoryInfo> = persistentSetOf()
)

@Serializable
data class TaskInfo(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: Date = Date(),
    val priority: Int = 0,
    val status: Boolean = false,
    val category: CategoryInfo = CategoryInfo()
)

fun TaskInfo.toJson(): String {
    return Json.encodeToString(this)
}

fun String?.toTaskInfo(): TaskInfo? {
    return runCatching {
        Json.decodeFromString<TaskInfo>(this.orEmpty())
    }.getOrNull()
}
