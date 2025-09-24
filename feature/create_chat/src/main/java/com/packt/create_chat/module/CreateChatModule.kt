package com.packt.create_chat.module

import com.packt.create_chat.data.repository.StoreUsersRepository
import com.packt.create_chat.domain.IStoreUsersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class CreateChatModule {
    @Binds
    abstract fun provideCreateChatRepository(impl: StoreUsersRepository): IStoreUsersRepository
}