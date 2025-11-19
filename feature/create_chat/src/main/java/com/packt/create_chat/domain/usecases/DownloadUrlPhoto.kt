package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class DownloadUrlPhoto @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(remotePath: String): String {
        return repository.downloadUrlPhoto(remotePath)
    }
}