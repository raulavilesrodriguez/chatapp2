package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class UpdateChatInfo @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(
        chatId: String,
        groupName: String?,
        groupPhotoUrl: String?
    ){
        repository.updateChatInfo(chatId, groupName, groupPhotoUrl)
    }
}