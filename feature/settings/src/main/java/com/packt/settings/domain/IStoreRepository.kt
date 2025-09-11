package com.packt.settings.domain

import com.packt.settings.ui.model.SetUserData

interface IStoreRepository {

    suspend fun save(user: SetUserData)
    suspend fun getUser(numberId: String): SetUserData?
    suspend fun delete(numberId: String)
    suspend fun update(user: SetUserData)
}