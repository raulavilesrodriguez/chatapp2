package com.packt.settings.domain.usecases

import com.packt.settings.domain.IAccountRepository
import jakarta.inject.Inject

class GetPhoneNumber @Inject constructor(
    private val repository: IAccountRepository
) {
    operator fun invoke(): String? {
        return repository.getPhoneNumber()
    }
}