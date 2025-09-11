package com.packt.settings.domain


interface IStorageRepository {

    suspend fun uploadPhoto(localPhoto: String, remotePath: String)
    suspend fun downloadUrlPhoto(remotePath: String) : String
}