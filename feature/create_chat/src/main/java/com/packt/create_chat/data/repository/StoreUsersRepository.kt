package com.packt.create_chat.data.repository

import com.packt.create_chat.data.datasource.FirestoreUsersDataSource
import com.packt.create_chat.domain.IStoreUsersRepository
import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StoreUsersRepository @Inject constructor(
    private val dataSource: FirestoreUsersDataSource
) : IStoreUsersRepository {

    override val currentUserId: String
        get() = dataSource.currentUserId

    override suspend fun getUsers() = dataSource.getUsers()

    override suspend fun searchUsers(namePrefix: String): Flow<List<UserData>> {
        return dataSource.searchUsers(namePrefix)
    }

    override suspend fun createChat(participants: List<String>): String {
        return dataSource.createChat(participants)
    }
}