package com.packt.settings.domain.usecases

import com.packt.settings.domain.IAccountRepository
import javax.inject.Inject

class GetCurrentUserId @Inject constructor(
    private val repository: IAccountRepository
) {
    operator fun invoke() = repository.currentUserId
}