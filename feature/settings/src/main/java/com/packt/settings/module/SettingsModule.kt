package com.packt.settings.module

import com.packt.settings.data.network.repository.AccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.packt.settings.data.network.repository.StorageRepository
import com.packt.settings.data.network.repository.StoreRepository
import com.packt.settings.domain.IAccountRepository
import com.packt.settings.domain.IStorageRepository
import com.packt.settings.domain.IStoreRepository


@InstallIn(SingletonComponent::class)
@Module
abstract class SettingsModule {
    @Binds
    abstract fun provideStorageRepository(impl: StorageRepository): IStorageRepository

    @Binds
    abstract fun provideAccountRepository(impl: AccountRepository): IAccountRepository

    @Binds
    abstract fun provideStoreRepository(impl: StoreRepository): IStoreRepository

}