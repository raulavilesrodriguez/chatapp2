package com.packt.chat.domain.usecases

import com.packt.chat.domain.IStorageRepository
import javax.inject.Inject

class DownloadUrlPhoto @Inject constructor(
    private val repository: IStorageRepository
) {
    suspend operator fun invoke(remotePath: String): String {
        return repository.downloadUrlPhoto(remotePath)
    }
}