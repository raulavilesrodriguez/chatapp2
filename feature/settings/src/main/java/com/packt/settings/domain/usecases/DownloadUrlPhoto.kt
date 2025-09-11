package com.packt.settings.domain.usecases

import com.packt.settings.domain.IStorageRepository
import javax.inject.Inject

class DownloadUrlPhoto @Inject constructor(
    private val repository: IStorageRepository
) {
    suspend operator fun invoke(remotePath: String): String {
        return repository.downloadUrlPhoto(remotePath)
    }
}