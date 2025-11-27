package com.packt.domain.user

data class UserConversations(
    val chatId: String = "",
    val clearedTimestamp: Long? = null,
    val blocked: Boolean = false,
    val updatedAt: Long? = null
)
