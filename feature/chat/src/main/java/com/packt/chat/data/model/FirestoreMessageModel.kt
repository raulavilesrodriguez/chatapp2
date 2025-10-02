package com.packt.chat.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.packt.chat.domain.models.Message
import com.packt.domain.model.ContentType
import java.text.SimpleDateFormat
import java.util.Locale

data class FirestoreMessageModel(
    @Transient
    val id: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var senderAvatar: String = "",
    var content: String = "",
    var contentType: String = ContentType.TEXT.name,
    @ServerTimestamp
    var timestamp: Timestamp? = null
) {
    companion object {
        fun fromDomain(message: Message): FirestoreMessageModel {
            return FirestoreMessageModel(
                id = "",
                senderId = message.senderId,
                senderName = message.senderName,
                senderAvatar = message.senderAvatar,
                content = message.content,
                contentType = message.contentType.name,
                timestamp = null
            )
        }
    }

    fun toDomain(userId: String, chatId: String): Message {
        val domainContentType = when (contentType.uppercase()) {
            ContentType.TEXT.name -> ContentType.TEXT
            ContentType.IMAGE.name -> ContentType.IMAGE
            ContentType.VIDEO.name -> ContentType.VIDEO
            else -> ContentType.TEXT
        }

        return Message(
            id = id,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderAvatar = senderAvatar,
            isMine = userId == senderId,
            contentType = domainContentType,
            content = content,
            contentDescription = "",
            timestamp = timestamp?.toDateString() ?:""
        )
    }

    private fun Timestamp.toDateString(): String {
        // Create a SimpleDateFormat instance with the desired format and the default Locale
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        // Convert the Timestamp to a Date object
        val date = toDate()

        // Format the Date object using the SimpleDateFormat instance
        return formatter.format(date)
    }
}