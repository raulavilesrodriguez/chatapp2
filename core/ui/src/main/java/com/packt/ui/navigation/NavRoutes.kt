package com.packt.ui.navigation

object NavRoutes {
    const val Splash = "splash"

    const val Login = "login"

    const val Settings = "settings"

    const val ConversationsList = "conversations_list"

    const val NewConversation = "create_conversation"

    const val Chat = "chat/{chatId}"
    object ChatArgs {
        const val ChatId = "chatId"
    }
    const val CreateChatGroup = "create_chat_group"
    const val NewParticipantsGroup = "new_participants_group"

    const val CreateGroupGraph = "create_group_graph"
    const val GroupChat = "group_chat"
    const val SetGroupChat = "set_group_chat"

    const val EditUser = "edit_user"
    const val EditName = "edit_name"
}