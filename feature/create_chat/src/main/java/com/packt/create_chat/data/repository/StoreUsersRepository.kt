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

    override suspend fun getUser(userId: String): UserData? {
        return dataSource.getUser(userId)
    }

    override suspend fun searchUsers(namePrefix: String): Flow<List<UserData>> {
        return dataSource.searchUsers(namePrefix)
    }

    override suspend fun createChatId(participants: List<String>, isGroup: Boolean): String {
        return dataSource.createChatId(participants, isGroup)
    }

    override suspend fun createChat(
        participants: List<String>,
        chatId: String,
        isGroup: Boolean,
        groupName: String?,
        groupPhotoUrl: String?) {
        return dataSource.createChat(participants, chatId, isGroup, groupName, groupPhotoUrl)
    }

    override suspend fun updateChatInfo(
        chatId: String,
        groupName: String?,
        groupPhotoUrl: String?
    ) {
        dataSource.updateChatInfo(chatId, groupName, groupPhotoUrl)
    }

    override suspend fun chatExists(participants: List<String>, groupName: String?): Boolean {
        return dataSource.chatExists(participants, groupName)
    }

    override suspend fun uploadPhoto(localPhoto: String, remotePath: String) {
        dataSource.uploadPhoto(localPhoto, remotePath)
    }

    override suspend fun downloadUrlPhoto(remotePath: String): String {
        return dataSource.downloadUrlPhoto(remotePath)
    }

    override suspend fun addContact(number: String): Boolean {
        return dataSource.addContact(number)
    }

    override suspend fun getContacts(): Flow<List<UserData>> {
        return dataSource.getContacts()
    }

    override suspend fun searchContacts(namePrefix: String): Flow<List<UserData>> {
        return dataSource.searchContacts(namePrefix)
    }
}