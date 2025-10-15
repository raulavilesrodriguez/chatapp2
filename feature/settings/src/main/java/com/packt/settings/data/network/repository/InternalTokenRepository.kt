package com.packt.settings.data.network.repository

import com.packt.settings.data.network.datasource.FirestoreUserDataSource
import com.packt.settings.domain.IInternalTokenRepository
import javax.inject.Inject

class InternalTokenRepository @Inject constructor(
    private val dataSource: FirestoreUserDataSource
) : IInternalTokenRepository {
    override suspend fun storeToken(uid: String, token: String) {
        dataSource.storeFCMToken(uid, token)
    }
}