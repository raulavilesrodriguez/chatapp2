package com.packt.settings.domain

import com.packt.domain.user.UserData

interface IStoreRepository {

    suspend fun save(user: UserData)
    suspend fun getUser(uid: String): UserData?
    suspend fun delete(uid: String)
    suspend fun update(user: UserData)
}