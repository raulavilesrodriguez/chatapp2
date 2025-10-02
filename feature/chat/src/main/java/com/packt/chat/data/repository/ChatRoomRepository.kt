package com.packt.chat.data.repository

import com.packt.chat.data.datasource.FirestoreChatsDataSource
import com.packt.chat.domain.IChatRoomRepository
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val dataSource: FirestoreChatsDataSource
): IChatRoomRepository {
    override suspend fun getInitialChatRoomInfo(chatId: String): ChatMetadata? {
        return dataSource.getInitialChatRoomInfo(chatId)
    }
    override val currentUserId: String
        get() = dataSource.currentUserId

    override suspend fun getUser(uid: String): UserData? {
        return dataSource.getUser(uid)
    }
}