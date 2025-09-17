package com.packt.settings.domain.usecases

import com.packt.settings.domain.IAccountRepository
import javax.inject.Inject

class SignInWithVerificationId @Inject constructor(
    private val repository: IAccountRepository
) {
    suspend operator fun invoke(verificationId: String, code: String) : Boolean {
        return repository.signInWithVerificationId(verificationId, code)
    }
}