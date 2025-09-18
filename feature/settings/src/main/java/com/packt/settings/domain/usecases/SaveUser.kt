package com.packt.settings.domain.usecases

import com.packt.domain.user.UserData
import com.packt.settings.domain.IStoreRepository
import jakarta.inject.Inject

class SaveUser @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(user: UserData) {
        repository.save(user)
    }
}