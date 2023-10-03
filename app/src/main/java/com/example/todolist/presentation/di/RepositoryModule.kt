package com.example.todolist.presentation.di

import com.example.todolist.data.db.TaskCategory
import com.example.todolist.data.repository.TaskCategoryRepositoryImpl
import com.example.todolist.domain.TaskCategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTaskCategoryRepository(taskCategory: TaskCategory): TaskCategoryRepository {
        return TaskCategoryRepositoryImpl(taskCategory)
    }
}