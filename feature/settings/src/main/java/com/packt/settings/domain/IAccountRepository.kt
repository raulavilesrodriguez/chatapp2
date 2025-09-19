package com.packt.settings.domain

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow

interface IAccountRepository {

    val currentUserId: String
    val hasUser: Boolean
    fun startPhoneNumberVerification(number: String, activity: Activity) : Flow<PhoneVerificationResult>
    suspend fun signInWithVerificationId(verificationId: String, code: String) : Boolean
    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Boolean
    fun resendVerificationCode(number: String, activity: Activity, token: PhoneAuthProvider.ForceResendingToken) : Flow<PhoneVerificationResult>
    suspend fun deleteAccount()
    fun signOut()
    fun getPhoneNumber() : String?
}