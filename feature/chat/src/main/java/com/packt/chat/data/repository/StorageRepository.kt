package com.packt.chat.data.repository

import com.packt.chat.data.datasource.StorageDataSource
import com.packt.chat.domain.IStorageRepository
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val data: StorageDataSource
) : IStorageRepository {

    override suspend fun uploadPhoto(localPhoto: String, remotePath: String) {
        data.uploadPhoto(localPhoto, remotePath)
    }

    override suspend fun downloadUrlPhoto(remotePath: String): String {
        return data.downloadUrlPhoto(remotePath)
    }
}