package com.packt.chat.ui.model

import com.packt.chat.domain.models.ChatRoom

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
