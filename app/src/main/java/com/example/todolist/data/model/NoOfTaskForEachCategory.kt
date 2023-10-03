package com.example.todolist.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NoOfTaskForEachCategory(
    val category: String,
    val color: String,
    val count: Int,
) {
    val textFormatted = "$category (${count})"
}
