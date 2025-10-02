package com.packt.chat.module

import com.packt.chat.data.repository.ChatRoomRepository
import com.packt.chat.data.repository.MessagesRepository
import com.packt.chat.domain.IChatRoomRepository
import com.packt.chat.domain.IMessagesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class ChatModule {
    @Binds
    abstract fun providesMessagesRepository(
        messagesRepository: MessagesRepository
    ): IMessagesRepository

    @Binds
    abstract fun providesChatRoomRepository(
        chatRoomRepository: ChatRoomRepository
    ): IChatRoomRepository
}