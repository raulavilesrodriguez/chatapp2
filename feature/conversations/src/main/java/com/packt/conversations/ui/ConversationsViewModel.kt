package com.packt.conversations.ui

import com.packt.ui.navigation.NavRoutes
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor() : BaseViewModel() {

    fun onNewConversationClick(openScreen: (String) -> Unit) = openScreen(NavRoutes.NewConversation)

    fun onConversationClick(openScreen: (String) -> Unit, chatId: String){
        //openScreen(NavRoutes.Chat.replace("{chatId}", chatId))
    }

}