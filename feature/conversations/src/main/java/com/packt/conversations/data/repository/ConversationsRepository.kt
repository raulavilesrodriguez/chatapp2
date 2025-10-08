package com.packt.conversations.data.repository

import com.packt.conversations.data.datasource.FirestoreConversationsDataSource
import com.packt.conversations.domain.IConversationsRepository
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConversationsRepository @Inject constructor(
    private val dataSource: FirestoreConversationsDataSource
) : IConversationsRepository {
    override suspend fun getConversations(): Flow<List<ChatMetadata>> {
        return dataSource.getConversations()
    }

    override suspend fun getUser(uid: String): UserData? = dataSource.getUser(uid)

    override val currentUserId: String
        get() = dataSource.currentUserId
}