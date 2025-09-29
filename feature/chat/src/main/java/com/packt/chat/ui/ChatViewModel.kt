package com.packt.chat.ui

import com.packt.chat.domain.models.ChatRoom
import com.packt.chat.ui.model.Chat
import com.packt.chat.ui.model.Message
import com.packt.chat.ui.model.toUI
import com.packt.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ChatViewModel @Inject constructor(

) : BaseViewModel() {

    private val _sendText = MutableStateFlow("")
    val sendText: StateFlow<String> = _sendText.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _uiState = MutableStateFlow(Chat())
    val uiState: StateFlow<Chat> = _uiState

    private var messageCollectionJob: Job? = null

    private lateinit var chatRoom: ChatRoom

    fun updateSendText(newText: String) {
        _sendText.value = newText
    }

    fun loadChatInformation(id: String){

        chatRoom = ChatRoom(
            id = id,
            senderName = "Mayra",
            senderAvatar = "https://i.pravatar.cc/300?img=10",
            lastMessages = listOf()
        )
        _uiState.value = chatRoom.toUI()
    }

    fun onSendMessage(){

    }

}