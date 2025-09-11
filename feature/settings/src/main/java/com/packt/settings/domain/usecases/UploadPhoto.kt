package com.packt.settings.domain.usecases

import com.packt.settings.domain.IStorageRepository
import javax.inject.Inject

class UploadPhoto  @Inject constructor(
    private val repository: IStorageRepository
)  {
    suspend operator fun invoke(localPhoto: String, remotePath: String) {
        repository.uploadPhoto(localPhoto, remotePath)
    }
}