package com.example.todolist.data.util

import com.example.todolist.data.model.CategoryInfo
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class CategorySetSerializer(private val serializer: KSerializer<CategoryInfo>) :
    KSerializer<PersistentSet<CategoryInfo>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<Set<CategoryInfo>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentSet"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentSet<CategoryInfo>) {
        return SetSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentSet<CategoryInfo> {
        return SetSerializer(serializer).deserialize(decoder).toPersistentSet()
    }

}