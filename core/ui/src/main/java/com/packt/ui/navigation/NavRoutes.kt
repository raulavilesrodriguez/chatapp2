package com.packt.ui.navigation

object NavRoutes {
    const val Login = "login"

    const val Settings = "settings"

    const val ConversationsList = "conversations_list"

    const val NewConversation = "create_conversation"

    const val Chat = "chat/{chatId}"

    object ChatArgs {
        const val ChatId = "chatId"
    }
}