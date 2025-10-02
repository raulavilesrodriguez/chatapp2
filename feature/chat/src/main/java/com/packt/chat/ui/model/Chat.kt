package com.packt.chat.ui.model

import com.packt.chat.domain.models.ChatRoom
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData

data class Chat(
    val id: String? = null,
    val name: String? = null,
    val avatar: String? = null
)

fun ChatRoom.toUI() = run {
    Chat(
        id = id,
        name = senderName,
        avatar = senderAvatar
    )
}
