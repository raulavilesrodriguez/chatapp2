package com.packt.conversations.ui.model

import com.packt.domain.model.ChatMetadata
import com.packt.domain.model.ContentType
import com.packt.domain.user.UserData
import com.packt.ui.avatar.DEFAULT_AVATAR
import com.packt.ui.avatar.DEFAULT_AVATAR_GROUP
import java.text.SimpleDateFormat
import java.util.Locale

data class Conversation(
    val chatId: String = "",
    // to show in the list
    val displayName: String = "",
    val displayPhotoUrl: String = "",
    // information of the last message
    val lastMessage: String = "",
    val messageType: ContentType = ContentType.TEXT,
    val timestamp: String = "",
    val unreadCount: Int = 0,
    // participants
    val isGroupChat: Boolean = false,
    val participants: List<UserData> = listOf(),
    val isMine: Boolean = false
)

fun ChatMetadata.toConversation(participants: List<UserData>, currentUserId: String): Conversation {
    val unread = this.unreadCount[currentUserId] ?: 0

    val displayName: String
    val displayPhotoUrl: String
    var isMine = false

    if(this.isGroup){
        displayName = this.groupName ?: "Grupo sin nombre"
        displayPhotoUrl = this.groupPhotoUrl ?: DEFAULT_AVATAR_GROUP
    } else {
        // chats 1 to 1
        val otherUser = participants.find { it.uid != currentUserId }
        if (otherUser != null){
            displayName = otherUser.name ?: "Usuario sin nombre"
            displayPhotoUrl = otherUser.photoUrl
            isMine = false
        } else {
            // chats with me
            val currentUser = participants.find { it.uid == currentUserId }
            displayName = currentUser?.name ?: "Tú"
            displayPhotoUrl = currentUser?.photoUrl ?: DEFAULT_AVATAR
            isMine = true
        }
    }

    return Conversation(
        chatId = this.chatId,
        displayName = displayName,
        displayPhotoUrl = displayPhotoUrl,
        lastMessage = this.lastMessage ?: "",
        timestamp = this.updatedAt?.let{ formatTimestamp(it)} ?: "",
        messageType = lastMessageType?: ContentType.TEXT,
        unreadCount = unread,
        isGroupChat = isGroup,
        participants = participants,
        isMine = isMine
    )
}

fun formatTimestamp(millis: Long) : String {
    // convert millis (Long) to date "10:30 AM"
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}
