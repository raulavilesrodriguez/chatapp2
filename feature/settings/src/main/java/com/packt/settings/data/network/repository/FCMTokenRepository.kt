package com.packt.settings.data.network.repository

import com.packt.settings.data.network.datasource.FCMTokenDataSource
import com.packt.settings.domain.IFCMTokenRepository
import javax.inject.Inject

class FCMTokenRepository @Inject constructor(
    private val dataSource: FCMTokenDataSource
) : IFCMTokenRepository {
    override suspend fun getFCMToken(): String {
        return dataSource.getFcmToken() ?:
        throw RuntimeException("FCM Token could not be retrieved")
    }
}