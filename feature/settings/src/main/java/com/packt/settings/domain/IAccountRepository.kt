package com.packt.settings.domain

interface IAccountRepository {

    suspend fun authenticate(number: String)

}