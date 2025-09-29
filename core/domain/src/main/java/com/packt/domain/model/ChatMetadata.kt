package com.packt.domain.model

data class ChatMetadata(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    var lastMessage: String? = null,
    var updatedAt: Long? = null, //time the last message was sent
    var lastMessageSenderId: String? = null,
    var lastMessageType: ContentType? = ContentType.TEXT,
    var unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis() //time to create chat
)
