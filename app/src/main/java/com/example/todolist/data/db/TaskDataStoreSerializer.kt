package com.example.todolist.data.db

import androidx.datastore.core.Serializer
import com.example.todolist.data.model.TaskDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object TaskDataStoreSerializer : Serializer<TaskDataStore> {
    override val defaultValue: TaskDataStore
        get() = TaskDataStore()

    override suspend fun readFrom(input: InputStream): TaskDataStore {
        return try {
            Json.decodeFromString(
                deserializer = TaskDataStore.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: TaskDataStore, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = TaskDataStore.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}