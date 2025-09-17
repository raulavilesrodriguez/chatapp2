package com.packt.settings.data.network.repository

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.packt.settings.data.network.datasource.AccountDataSource
import com.packt.settings.domain.IAccountRepository
import com.packt.settings.domain.PhoneVerificationResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val data: AccountDataSource
): IAccountRepository {

    override val currentUserId: String
        get() = data.currentUserId

    override val hasUser: Boolean
        get() = data.hasUser

    override fun startPhoneNumberVerification(number: String, activity: Activity) : Flow<PhoneVerificationResult> {
        return data.startPhoneNumberVerification(number, activity)
    }

    override suspend fun signInWithVerificationId(verificationId: String, code: String) : Boolean {
        return data.signInWithVerificationId(verificationId, code)
    }

    override suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Boolean {
        return data.signInWithPhoneAuthCredential(credential)
    }

    override fun resendVerificationCode(number: String, activity: Activity, token: PhoneAuthProvider.ForceResendingToken) : Flow<PhoneVerificationResult> {
        return data.resendVerificationCode(number, activity, token)
    }

    override suspend fun deleteAccount() {
        data.deleteAccount()
    }

    override fun signOut() {
        data.signOut()
    }

}