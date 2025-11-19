package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class UploadPhoto @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(localPhoto: String, remotePath: String) {
        repository.uploadPhoto(localPhoto, remotePath)
    }
}