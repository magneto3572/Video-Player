package com.biz.eulermoters.domain.di

import com.biz.eulermoters.domain.util.StorageUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideStorageUtil() : StorageUtil {
        return StorageUtil()
    }
}