package com.packt.settings.data.network.datasource

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.packt.settings.domain.PhoneVerificationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AccountDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    private val TAG = "AccountDataSource"

    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    val hasUser: Boolean
        get() = firebaseAuth.currentUser != null

    /**
     * Inicia el proceso de verificación del número de teléfono.
     * Esta función enviará un SMS al número proporcionado.
     *
     * @param number El número de teléfono completo (incluyendo código de país).
     * @param activity La Activity actual, necesaria para el proceso de verificación.
     * @return Un [Flow] que emite eventos de [PhoneVerificationResult].
     */
    fun startPhoneNumberVerification(
        number: String,
        activity: Activity
    ) : Flow<PhoneVerificationResult> = callbackFlow {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Autenticación instantánea o el usuario ya tiene la app verificada en este dispositivo.
                Log.d(TAG, "onVerificationCompleted: $credential")
                trySend(PhoneVerificationResult.VerificationCompleted(credential)).isSuccess
                channel.close() // Cierra el flow ya que la verificación está completa
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                trySend(PhoneVerificationResult.VerificationFailed(e)).isSuccess
                channel.close() // Cierra el flow en caso de error
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent: $verificationId")
                trySend(PhoneVerificationResult.CodeSent(verificationId, token)).isSuccess
                // No cierres el channel aquí, porque esperamos que el usuario ingrese el código.
                // El ViewModel manejará este estado.
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number) // Número de teléfono para verificar
            .setTimeout(60L, TimeUnit.SECONDS) // Tiempo de espera y reenvío automático
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        // Se llama cuando el Flow es cancelado
        awaitClose {
            Log.d(TAG, "Phone number verification flow cancelled/closed.")
            // Aquí podrías añadir lógica de limpieza si fuera necesario,
            // aunque para verifyPhoneNumber no suele haber mucho que limpiar activamente.
        }

    }

    /**
     * Autentica al usuario usando el ID de verificación y el código SMS.
     *
     * @param verificationId El ID recibido en onCodeSent.
     * @param code El código SMS ingresado por el usuario.
     * @return true si el inicio de sesión fue exitoso, false en caso contrario.
     */
    suspend fun signInWithVerificationId(verificationId: String, code: String) : Boolean {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            firebaseAuth.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "signInWithPhoneAuthCredential failed", e)
            false
        }
    }

    /**
     * Autentica al usuario directamente si onVerificationCompleted fue llamado.
     * @param credential La credencial recibida en onVerificationCompleted.
     * @return true si el inicio de sesión fue exitoso, false en caso contrario.
     */
    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Boolean {
        return try {
            firebaseAuth.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "signInWithPhoneAuthCredential (direct) failed", e)
            false
        }
    }

    /**
     * Reenvía el código de verificación.
     *
     * @param number El número de teléfono.
     * @param activity La Activity actual.
     * @param token El token recibido en onCodeSent.
     * @return Un [Flow] que emite eventos de [PhoneVerificationResult].
     */
    fun resendVerificationCode(
        number: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken
    ) : Flow<PhoneVerificationResult> = callbackFlow {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Resend: onVerificationCompleted: $credential")
                trySend(PhoneVerificationResult.VerificationCompleted(credential)).isSuccess
                channel.close()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "Resend: onVerificationFailed", e)
                trySend(PhoneVerificationResult.VerificationFailed(e)).isSuccess
                channel.close()
            }

            override fun onCodeSent(
                verificationId: String,
                newToken: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "Resend: onCodeSent: $verificationId")
                trySend(PhoneVerificationResult.CodeSent(verificationId, newToken)).isSuccess
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token) // Importante para reenviar
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose { Log.d(TAG, "Resend verification flow cancelled/closed.") }

    }


    suspend fun deleteAccount() {
        firebaseAuth.currentUser!!.delete().await()

    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getPhoneNumber() : String? = firebaseAuth.currentUser?.phoneNumber

}

