package com.packt.settings.domain.usecases

import com.packt.settings.domain.IAccountRepository
import com.packt.settings.domain.IInternalTokenRepository
import javax.inject.Inject

class SignOut @Inject constructor(
    private val repository: IAccountRepository
) {
    operator fun invoke() {
        repository.signOut()
    }
}