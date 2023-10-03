package com.example.todolist.data.util

import com.example.todolist.data.model.TaskInfo
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class TaskListSerializer(private val serializer: KSerializer<TaskInfo>) :
    KSerializer<PersistentList<TaskInfo>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<TaskInfo>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<TaskInfo>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<TaskInfo> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }

}