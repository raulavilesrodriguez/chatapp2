package com.packt.create_chat.domain

import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow

interface IStoreUsersRepository {
    val currentUserId: String
    suspend fun getUsers(): Flow<List<UserData>>
    suspend fun searchUsers(namePrefix: String): Flow<List<UserData>>
    suspend fun createChat(participants: List<String>): String
}