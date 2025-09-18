package com.packt.settings.data.network.repository

import com.packt.domain.user.UserData
import com.packt.settings.data.network.datasource.FirestoreUserDataSource
import com.packt.settings.domain.IStoreRepository
import jakarta.inject.Inject

class StoreRepository @Inject constructor(
    private val dataSource: FirestoreUserDataSource
) : IStoreRepository {

    override suspend fun save(user: UserData) {
        dataSource.save(user)
    }

    override suspend fun getUser(uid: String): UserData? {
        return dataSource.getUser(uid)
    }

    override suspend fun delete(uid: String) {
        dataSource.delete(uid)
    }

    override suspend fun update(user: UserData) {
        dataSource.update(user)
    }
}