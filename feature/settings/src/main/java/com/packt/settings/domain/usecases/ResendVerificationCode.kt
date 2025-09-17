package com.packt.settings.domain.usecases

import android.app.Activity
import com.google.firebase.auth.PhoneAuthProvider
import com.packt.settings.domain.IAccountRepository
import com.packt.settings.domain.PhoneVerificationResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResendVerificationCode @Inject constructor(
    private val repository: IAccountRepository
) {
    operator fun invoke (
        number: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken) : Flow<PhoneVerificationResult>{
        return repository.resendVerificationCode(number, activity, token)
    }
}