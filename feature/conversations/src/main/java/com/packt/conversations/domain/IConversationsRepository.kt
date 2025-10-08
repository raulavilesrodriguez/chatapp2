package com.packt.conversations.domain

import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow

interface IConversationsRepository {
    suspend fun getConversations(): Flow<List<ChatMetadata>>
    suspend fun getUser(uid:String): UserData?
    val currentUserId: String
}