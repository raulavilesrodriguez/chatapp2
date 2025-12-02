package com.packt.settings.domain.usecases

import com.packt.settings.domain.IInternalTokenRepository
import javax.inject.Inject

class ClearFCMToken @Inject constructor(
    private val internalTokenRepository: IInternalTokenRepository
) {
    suspend operator fun invoke(uid: String) {
        internalTokenRepository.clearFCMToken(uid)
    }
}