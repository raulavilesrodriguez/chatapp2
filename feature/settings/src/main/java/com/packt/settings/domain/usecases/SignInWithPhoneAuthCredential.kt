package com.packt.settings.domain.usecases

import com.google.firebase.auth.PhoneAuthCredential
import com.packt.settings.domain.IAccountRepository
import javax.inject.Inject

class SignInWithPhoneAuthCredential @Inject constructor(
    private val repository: IAccountRepository
) {
    suspend operator fun invoke(credential: PhoneAuthCredential): Boolean {
        return repository.signInWithPhoneAuthCredential(credential)
    }
}