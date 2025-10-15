package com.packt.settings.domain.usecases

import com.packt.settings.domain.IFCMTokenRepository
import com.packt.settings.domain.IInternalTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAndStoreFCMToken @Inject constructor(
    private val fcmRepository: IFCMTokenRepository,
    private val internalTokenRepository: IInternalTokenRepository
) {
    suspend operator fun invoke(uid: String) = withContext(Dispatchers.IO){
        val token = fcmRepository.getFCMToken()
        internalTokenRepository.storeToken(uid, token)
    }
}