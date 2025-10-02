package com.packt.chat.ui

import android.util.Log
import com.packt.chat.domain.usecases.GetCurrentUserId
import com.packt.chat.domain.usecases.GetInitialChatRoomInfo
import com.packt.chat.domain.usecases.GetMessages
import com.packt.chat.domain.usecases.GetUser
import com.packt.chat.domain.usecases.SendMessage
import com.packt.chat.ui.model.Message
import com.packt.chat.ui.model.MessageContent
import com.packt.domain.model.ChatMetadata
import com.packt.domain.model.ContentType
import com.packt.domain.user.UserData
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import com.packt.chat.domain.models.Message as DomainMessage

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessages: GetMessages,
    private val sendMessage: SendMessage,
    private val currentUserIdUseCase: GetCurrentUserId,
    private val getInitialChatRoomInfo: GetInitialChatRoomInfo,
    val getUser: GetUser,
) : BaseViewModel() {

    private val _sendText = MutableStateFlow("")
    val sendText: StateFlow<String> = _sendText.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _uiState = MutableStateFlow(UserData())
    val uiState: StateFlow<UserData> = _uiState

    private var messageCollectionJob: Job? = null

    private lateinit var chatMetadata: ChatMetadata

    val currentUserId
        get() = currentUserIdUseCase()

    fun updateSendText(newText: String) {
        _sendText.value = newText
    }

    private var user: UserData? = null

    init {
        launchCatching { user = getUser(currentUserId) }
    }

    fun loadChatInformation(chatId: String){
        messageCollectionJob = launchCatching {
            try {
                withContext(Dispatchers.IO){
                    chatMetadata = getInitialChatRoomInfo(chatId)!!
                    val participants: List<String> = chatMetadata.participants
                    val otherId: String? = if(participants.size == 1){
                        currentUserId
                    } else {
                        participants.find { it != currentUserId }
                    }
                    _uiState.value = getUser(otherId?: currentUserId)?: UserData()
                }
                updateMessages(chatId)
            } catch (ie: Throwable){
                Log.e("DEBUG LOAD MESSAGES", "Error in charge chat: ${ie.message}", ie)
            }
        }
    }

    fun updateMessages(chatId: String){
        messageCollectionJob = launchCatching {
            getMessages(chatId, currentUserId)
                .map { it.toUI() }
                .flowOn(Dispatchers.IO)
                .collect { message ->
                    _messages.value += message
                }
        }
    }

    private fun DomainMessage.toUI(): Message {
        return Message(
            id = id ?: "",
            senderName = senderName,
            senderAvatar = senderAvatar,
            timestamp = timestamp ?: "",
            isMine = isMine,
            messageContent = getMessageContent()
        )
    }

    private fun DomainMessage.getMessageContent(): MessageContent {
        return when (contentType) {
            ContentType.TEXT -> MessageContent.TextMessage(content)
            ContentType.IMAGE -> MessageContent.ImageMessage(content, contentDescription)
            ContentType.VIDEO -> MessageContent.VideoMessage(content, contentDescription)
        }
    }

    fun onSendMessage(){
        val currentMessageText = _sendText.value
        if(currentMessageText.isBlank()){
            return
        }
        if(!this::chatMetadata.isInitialized){
            Log.e("DEBUG SEND MESSAGE", "Chat metadata not initialized")
            return
        }
        launchCatching {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTimestampForUI = sdf.format(java.util.Date(System.currentTimeMillis()))
            val message = DomainMessage(
                id = "",
                chatId = chatMetadata.chatId,
                senderId = user?.uid?:currentUserId,
                senderName = user?.name?:"Yo",
                senderAvatar = user?.photoUrl ?: "",
                timestamp = formattedTimestampForUI,
                isMine = true,
                contentType = ContentType.TEXT,
                content = currentMessageText,
                contentDescription = currentMessageText
            )
            sendMessage(chatMetadata.chatId, message)
            _sendText.value = ""
        }
    }
}