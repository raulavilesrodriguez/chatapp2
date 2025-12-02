package com.packt.settings.domain

interface IInternalTokenRepository {
    suspend fun storeToken(uid: String, token: String)
    suspend fun clearFCMToken(uid: String)
}