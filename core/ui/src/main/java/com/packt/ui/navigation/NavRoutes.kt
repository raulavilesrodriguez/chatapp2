package com.packt.ui.navigation

object NavRoutes {
    const val Splash = "splash"

    const val Login = "login"

    const val Settings = "settings"

    const val ConversationsList = "conversations_list"

    const val NewConversation = "create_conversation"

    const val CHAT_ROOM = "chat_room"
    const val USER_ID = "uid"
    const val USER_ARG = "?$USER_ID={$USER_ID}"


    const val Chat = "chat/{chatId}"
    object ChatArgs {
        const val ChatId = "chatId"
    }
}