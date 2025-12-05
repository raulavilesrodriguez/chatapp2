package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class CreateChat @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(
        participants: List<String>,
        chatId: String,
        isGroup: Boolean,
        groupName: String?=null,
        groupPhotoUrl: String?=null
    ) {
        repository.createChat(participants, chatId, isGroup, groupName, groupPhotoUrl)
    }
}