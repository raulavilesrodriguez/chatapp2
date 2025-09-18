package com.packt.settings.domain.usecases

import com.packt.settings.domain.IStoreRepository
import jakarta.inject.Inject

class DeleteUser @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String) {
        repository.delete(uid)
    }
}