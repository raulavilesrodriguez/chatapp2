package com.packt.conversations.ui.model

import com.packt.domain.model.ChatMetadata
import com.packt.domain.model.ContentType
import com.packt.domain.user.UserData
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
    val isGroup = participants.size > 2

    var displayName = ""
    var displayPhotoUrl = ""
    var isMine: Boolean = false

    if(isGroup){
        displayName = this.groupName ?: participants.mapNotNull { it.name }.joinToString(" ")
        displayPhotoUrl = this.groupPhotoUrl ?: ""
    } else {
        // chats 1 to 1
        val otherUser = participants.find { it.uid != currentUserId }
        if (otherUser != null){
            displayName = otherUser.name ?: ""
            displayPhotoUrl = otherUser.photoUrl
        } else {
            // chats with me
            val currentUser = participants.find { it.uid == currentUserId }
            displayName = currentUser?.name ?: ""
            displayPhotoUrl = currentUser?.photoUrl ?: ""
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
