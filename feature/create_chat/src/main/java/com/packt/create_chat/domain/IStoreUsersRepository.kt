package com.packt.create_chat.domain

import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow

interface IStoreUsersRepository {
    val currentUserId: String
    suspend fun getUser(userId: String): UserData?
    suspend fun getUsers(): Flow<List<UserData>>
    suspend fun searchUsers(namePrefix: String): Flow<List<UserData>>
    suspend fun createChatId(participants: List<String>, isGroup: Boolean): String
    suspend fun createChat(participants: List<String>, chatId: String, isGroup: Boolean, groupName: String?, groupPhotoUrl: String?)
    suspend fun updateChatInfo(chatId: String, groupName: String?, groupPhotoUrl: String?)
    suspend fun chatExists(participants: List<String>, groupName: String?): Boolean
    suspend fun uploadPhoto(localPhoto: String, remotePath: String)
    suspend fun downloadUrlPhoto(remotePath: String) : String
    suspend fun addContact(number:String): Boolean
    suspend fun getContacts(): Flow<List<UserData>>
    suspend fun searchContacts(namePrefix: String): Flow<List<UserData>>
}