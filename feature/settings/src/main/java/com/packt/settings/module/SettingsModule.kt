package com.packt.settings.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.packt.settings.data.network.repository.StorageRepository
import com.packt.settings.domain.IStorageRepository


@InstallIn(SingletonComponent::class)
@Module
abstract class SettingsModule {
    @Binds
    abstract fun provideStorageRepository(impl: StorageRepository): IStorageRepository

}