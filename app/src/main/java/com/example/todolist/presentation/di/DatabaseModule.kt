package com.example.todolist.presentation.di
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.example.todolist.data.db.TaskCategory
import com.example.todolist.data.db.TaskCategoryImpl
import com.example.todolist.data.db.TaskDataStoreSerializer
import com.example.todolist.data.model.TaskDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "task_storage.pb"
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<TaskDataStore> {
        return DataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { TaskDataStore() }
            ),
            serializer = TaskDataStoreSerializer,
            produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }

    @Singleton
    @Provides
    fun provideLocalSource(dataStore: DataStore<TaskDataStore>): TaskCategory {
        return TaskCategoryImpl(dataStore)
    }
}