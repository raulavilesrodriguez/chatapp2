package com.packt.conversations.module

import com.packt.conversations.data.repository.ConversationsRepository
import com.packt.conversations.domain.IConversationsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class ConversationsModule {
    @Binds
    abstract fun providesConversationsRepository(
        impl: ConversationsRepository
    ) : IConversationsRepository
}