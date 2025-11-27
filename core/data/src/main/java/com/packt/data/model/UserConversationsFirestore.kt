package com.packt.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.packt.domain.user.UserConversations

data class UserConversationsFirestore(
    val chatId: String? = null,
    val clearedTimestamp: Timestamp? = null,
    @get:PropertyName("blocked")
    val blocked: Boolean? = null,
    val updatedAt: Timestamp? = null
)

fun UserConversationsFirestore.toUserConversations(): UserConversations {
    if(chatId == null) return UserConversations()
    return UserConversations(
        chatId = chatId,
        clearedTimestamp = clearedTimestamp?.toDate()?.time,
        blocked = blocked ?: false,
        updatedAt = updatedAt?.toDate()?.time
    )
}


