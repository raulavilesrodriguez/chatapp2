package com.packt.chat.domain

import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData

interface IChatRoomRepository {
    suspend fun getInitialChatRoomInfo(chatId: String): ChatMetadata?
    val currentUserId: String
    suspend fun getUser(uid: String): UserData?
}