package com.packt.settings.domain.usecases

import android.app.Activity
import com.packt.settings.domain.IAccountRepository
import com.packt.settings.domain.PhoneVerificationResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartPhoneNumberVerification @Inject constructor(
    private val repository: IAccountRepository
) {
    operator fun invoke(number: String, activity: Activity) : Flow<PhoneVerificationResult> {
        return repository.startPhoneNumberVerification(number, activity)
    }
}