package com.packt.settings.data.network.repository

import com.packt.settings.data.network.datasource.StorageDataSource
import com.packt.settings.domain.IStorageRepository
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