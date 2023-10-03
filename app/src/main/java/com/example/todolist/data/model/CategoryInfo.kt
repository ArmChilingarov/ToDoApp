package com.example.todolist.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryInfo(
    val categoryInformation: String = "",
    val color: String = ""
)
