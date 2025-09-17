package com.packt.settings.domain

import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

sealed class PhoneVerificationResult {
    data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : PhoneVerificationResult()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneVerificationResult()
    data class VerificationFailed(val exception: FirebaseException) : PhoneVerificationResult()
}