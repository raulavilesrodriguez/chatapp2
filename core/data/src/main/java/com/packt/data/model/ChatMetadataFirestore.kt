package com.packt.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.packt.domain.model.ChatMetadata
import com.packt.domain.model.ContentType

data class ChatMetadataFirestore(
    val chatId: String? = null,
    val participants: List<String>? = null,
    val lastMessage: String? = null,
    val updatedAt: Timestamp? = null, // Use Timestamp of Firestore, time the last message was sent
    val lastMessageSenderId: String? = null,
    val lastMessageType: String? = null,
    val unreadCount: Map<String, Int>? = null,
    val createdAt: Timestamp? = null,//time to create chat
    // to groups
    @get:PropertyName("isGroup")
    val isGroup: Boolean? = null,
    val groupName: String? = null,
    val groupPhotoUrl: String? = null
) {
    fun toChatMetadata(): ChatMetadata {
        if (chatId == null) return ChatMetadata()

        // Convert String to ContentType
        val domainLastMessageType = when (lastMessageType?.uppercase()) { //insensible
            ContentType.TEXT.name -> ContentType.TEXT
            ContentType.IMAGE.name -> ContentType.IMAGE
            ContentType.VIDEO.name -> ContentType.VIDEO
            else -> ContentType.TEXT
        }
        return ChatMetadata(
            chatId = chatId,
            participants = participants ?: emptyList(),
            lastMessage = lastMessage,
            updatedAt = updatedAt?.toDate()?.time,
            lastMessageSenderId = lastMessageSenderId,
            lastMessageType = domainLastMessageType,
            unreadCount = unreadCount ?: emptyMap(),
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
            isGroup = isGroup ?: false,
            groupName = groupName,
            groupPhotoUrl = groupPhotoUrl
        )
    }
}
