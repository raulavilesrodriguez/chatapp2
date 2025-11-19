package com.packt.chat.ui

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.packt.chat.domain.usecases.ClearUserActiveStatus
import com.packt.chat.domain.usecases.GetCurrentUserId
import com.packt.chat.domain.usecases.GetInitialChatRoomInfo
import com.packt.chat.domain.usecases.GetMessages
import com.packt.chat.domain.usecases.GetMessagesPaged
import com.packt.chat.domain.usecases.GetUser
import com.packt.chat.domain.usecases.ObserveUser
import com.packt.chat.domain.usecases.ResetUnreadCount
import com.packt.chat.domain.usecases.SendMessage
import com.packt.chat.domain.usecases.SetUserActiveInChat
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
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import com.packt.chat.domain.models.Message as DomainMessage

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessages: GetMessages,
    private val getMessagesPaged: GetMessagesPaged,
    private val sendMessage: SendMessage,
    private val currentUserIdUseCase: GetCurrentUserId,
    private val getInitialChatRoomInfo: GetInitialChatRoomInfo,
    val getUser: GetUser,
    private val observeUser: ObserveUser,
    private val resetUnreadCount: ResetUnreadCount,
    private val setUserActiveInChat: SetUserActiveInChat,
    private val clearUserActiveStatus: ClearUserActiveStatus
    ) : BaseViewModel() {

    private val _sendText = MutableStateFlow("")
    val sendText: StateFlow<String> = _sendText.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _uiState = MutableStateFlow<List<UserData>>(emptyList())
    val uiState: StateFlow<List<UserData>> = _uiState

    private var chatInfoJob: Job? = null

    private val _chatMetadata = MutableStateFlow<ChatMetadata?>(null)
    val chatMetadata: StateFlow<ChatMetadata?> = _chatMetadata.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null  // referencia al último doc para paginación
    private var allMessagesLoaded = false
    private var paginationJob: Job? = null
    private var realTimeJob: Job? = null
    private var currentChatId: String? = null
    private var userObservationJobs = mutableListOf<Job>()

    val currentUserId
        get() = currentUserIdUseCase()

    fun updateSendText(newText: String) {
        _sendText.value = newText
    }

    var user: UserData? = null

    init {
        reloadCurrentUser()
    }

    // para que siempre este actualizado los datos del usuario
    fun reloadCurrentUser() {
        launchCatching {
            user = getUser(currentUserId)
        }
    }

    fun loadChatInformation(chatId: String){
        chatInfoJob?.cancel()
        paginationJob?.cancel()
        realTimeJob?.cancel()
        userObservationJobs.forEach { it.cancel() }
        userObservationJobs.clear()
        currentChatId = chatId

        // clean el estado
        _uiState.value = emptyList()

        chatInfoJob = launchCatching {
            setUserActiveInChat(chatId)
            try {
                resetUnreadCount(chatId)
                _chatMetadata.value = getInitialChatRoomInfo(chatId) ?: return@launchCatching
                // inicia la carga de mensajes en una corrutina hija, para que sea mas rapido
                launchCatching {
                    loadInitialMessages(chatId)
                }
                val participants: List<String> = _chatMetadata.value?.participants ?: emptyList()
                 val otherParticipantsIds = participants.filter { it != currentUserId }

                if(otherParticipantsIds.isNotEmpty()){
                    // un mapa to have update the user data
                    val participantMap = mutableMapOf<String, UserData>()
                    otherParticipantsIds.forEach{ participantId ->
                        val job = launchCatching {
                            observeUser(participantId)
                                .flowOn(Dispatchers.IO) //la escucha de firestore ocurre en hilo de fondo observeUser()
                                .collect { user ->
                                    if(user != null){
                                        // synchronized para evitar condiciones de carrera
                                        synchronized(participantMap){
                                            participantMap[participantId] = user
                                            _uiState.value = participantMap.values.toList()
                                        }
                                    }
                                }
                        }
                        userObservationJobs.add(job)
                    }
                } else {
                    _uiState.value = emptyList()
                }
            } catch (ie: Throwable){
                Log.e("DEBUG LOAD MESSAGES", "Error in charge chat: ${ie.message}", ie)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentChatId?.let { chatId ->
            launchCatching {
                clearUserActiveStatus(chatId)
            }
        }
    }

    fun setChatActive(){
        currentChatId?.let { chatId ->
            launchCatching {
                setUserActiveInChat(chatId)
            }
        }
    }

    fun setChatInactive(){
        currentChatId?.let { chatId ->
            launchCatching {
                clearUserActiveStatus(chatId)
            }
        }
    }

    /**
     * private fun updateMessages(chatId: String, since: Timestamp){
     *         realTimeJob?.cancel()
     *         realTimeJob = launchCatching {
     *             getMessages(chatId, currentUserId, since)
     *                 .flowOn(Dispatchers.IO)
     *                 .map { list -> list.map { it.toUI() }}
     *                 .collect { messagesList ->
     *                     _messages.value = messagesList
     *                 }
     *         }
     *     }
     *
     */

    private fun loadInitialMessages(chatId: String, pageSize: Long=10){
        paginationJob = launchCatching {
            val (messages, lastDoc)= getMessagesPaged(chatId, currentUserId, pageSize, null)
            _messages.value = messages.map { it.toUI() }
            lastDocument = lastDoc
            allMessagesLoaded = messages.size < pageSize

            val mostRecentTimestamp = messages.firstOrNull()?.firestoreTimestamp ?: Timestamp.now()
            listenForNewMessages(chatId, mostRecentTimestamp)
        }
    }

    private fun listenForNewMessages(chatId: String, since: Timestamp) {
        realTimeJob = launchCatching {
            getMessages(chatId, currentUserId, since) // este es un Flow con snapshotListener
                .flowOn(Dispatchers.IO)
                .map { list -> list.map { it.toUI() } }
                .collect { newMessages ->
                    if(newMessages.isNotEmpty()){
                        // Mezclar historial + mensajes nuevos
                        val currentMessages = _messages.value
                        _messages.value = (newMessages.reversed() + currentMessages).distinctBy { it.id }
                    }
                }
        }
    }

    fun loadMoreMessages(chatId: String, pageSize: Long=2){
        if(allMessagesLoaded || paginationJob?.isActive == true) return  // there are no more messages
        paginationJob = launchCatching {
            if (lastDocument == null) {
                allMessagesLoaded = true
                return@launchCatching
            }
             val (olderMessages, lastDoc)= getMessagesPaged(chatId, currentUserId, pageSize, lastDocument)
            if(olderMessages.isNotEmpty()){
                val currentMessages = _messages.value
                _messages.value = (currentMessages + olderMessages.map { it.toUI() }).distinctBy { it.id }
            }
             lastDocument = lastDoc
            allMessagesLoaded = olderMessages.size < pageSize
        }
    }

    private fun DomainMessage.toUI(): Message {
        return Message(
            id = id ?: "",
            senderId = senderId,
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
        if(_chatMetadata.value == null){
            Log.e("DEBUG SEND MESSAGE", "Chat metadata not initialized")
            return
        }
        launchCatching {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTimestampForUI = sdf.format(java.util.Date(System.currentTimeMillis()))
            val message = DomainMessage(
                id = "",
                chatId = _chatMetadata.value?.chatId ?: return@launchCatching,
                senderId = user?.uid?:currentUserId,
                senderName = user?.name?:"Yo",
                senderAvatar = user?.photoUrl ?: "",
                timestamp = formattedTimestampForUI,
                isMine = true,
                contentType = ContentType.TEXT,
                content = currentMessageText,
                contentDescription = currentMessageText
            )
            _sendText.value = ""
            sendMessage(_chatMetadata.value!!.chatId, message, _chatMetadata.value!!.participants)
        }
    }

    fun onActionChatClick(action: Int){
        when(ActionsChat.getById(action)){
            ActionsChat.DELETE -> {}
        }
    }

    fun onActionGroupClick(action: Int){
        when(ActionsGroup.getById(action)){
            ActionsGroup.ADD -> {}
            ActionsGroup.LEFT -> {}
        }
    }
}