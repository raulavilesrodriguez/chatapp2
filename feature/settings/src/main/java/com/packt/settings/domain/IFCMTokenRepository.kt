package com.packt.settings.domain

interface IFCMTokenRepository {
    suspend fun getFCMToken(): String
}