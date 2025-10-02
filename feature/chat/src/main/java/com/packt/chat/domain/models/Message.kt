package com.packt.chat.domain.models

import com.packt.domain.model.ContentType

data class Message(
    val id: String? = null,
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val timestamp: String? = null,
    val isMine: Boolean = false,
    val contentType: ContentType = ContentType.TEXT,
    val content: String = "",
    val contentDescription: String = ""
)
