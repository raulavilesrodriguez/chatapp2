package com.packt.settings.domain.usecases

import com.packt.settings.domain.IAccountRepository
import javax.inject.Inject

class DeleteAccount @Inject constructor(
    private val repository: IAccountRepository
) {
    suspend operator fun invoke() {
        repository.deleteAccount()
    }
}