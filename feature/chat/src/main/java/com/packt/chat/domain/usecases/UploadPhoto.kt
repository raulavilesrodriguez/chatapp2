package com.packt.chat.domain.usecases

import com.packt.chat.domain.IStorageRepository
import javax.inject.Inject

class UploadPhoto  @Inject constructor(
    private val repository: IStorageRepository
)  {
    suspend operator fun invoke(localPhoto: String, remotePath: String) {
        repository.uploadPhoto(localPhoto, remotePath)
    }
}