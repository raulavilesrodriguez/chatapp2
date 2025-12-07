package com.packt.create_chat.ui

import android.util.Log
import com.packt.chat.feature.create_chat.R
import com.packt.create_chat.domain.usecases.AddContact
import com.packt.create_chat.domain.usecases.ChatExists
import com.packt.create_chat.domain.usecases.CreateChat
import com.packt.create_chat.domain.usecases.CreateChatId
import com.packt.create_chat.domain.usecases.DeleteContact
import com.packt.create_chat.domain.usecases.DownloadUrlPhoto
import com.packt.create_chat.domain.usecases.GetContacts
import com.packt.create_chat.domain.usecases.GetCurrentUserId
import com.packt.create_chat.domain.usecases.GetUser
import com.packt.create_chat.domain.usecases.GetUsers
import com.packt.create_chat.domain.usecases.SearchContacts
import com.packt.create_chat.domain.usecases.SearchUsers
import com.packt.create_chat.domain.usecases.UploadPhoto
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import com.packt.ui.avatar.DEFAULT_AVATAR_GROUP
import com.packt.ui.ext.numberFirebaseEcu
import com.packt.ui.navigation.NavRoutes
import com.packt.ui.snackbar.SnackbarManager
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateConversationViewModel @Inject constructor(
    private val getUsers: GetUsers,
    private val searchUsers: SearchUsers,
    private val createChatId: CreateChatId,
    private val createChat: CreateChat,
    private val currentUserIdUseCase: GetCurrentUserId,
    private val chatExists: ChatExists,
    private val getUser: GetUser,
    private val uploadPhoto: UploadPhoto,
    private val downloadUrlPhoto: DownloadUrlPhoto,
    private val addContact: AddContact,
    private val getContacts: GetContacts,
    private val searchContacts: SearchContacts,
    private val deleteContact: DeleteContact
    ): BaseViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserData>>(emptyList())
    val searchResults: StateFlow<List<UserData>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    private var participantsGroupIds = mutableListOf<String>()
    private val _participants = MutableStateFlow<List<UserData>>(emptyList())
    val participants: StateFlow<List<UserData>> = _participants.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _uiStateChatMetadata = MutableStateFlow(ChatMetadata())
    val uiStateChatMetadata: StateFlow<ChatMetadata> = _uiStateChatMetadata.asStateFlow()

    private val _selectedParticipants = MutableStateFlow<Set<UserData>>(emptySet())
    val selectedParticipants: StateFlow<Set<UserData>> = _selectedParticipants.asStateFlow()

    private val _numberContact = MutableStateFlow("")
    val numberContact: StateFlow<String> = _numberContact.asStateFlow()

    private var user: UserData? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            _searchText
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    searchJob?.cancel() // cancel previous search
                    if (query.isBlank()) {
                        fetchAllUsers()
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
    }

    private fun fetchAllUsers() {
        _isLoading.value = true
        searchJob = launchCatching {
            getContacts()
                .collect { users ->
                    _searchResults.value = users
                    _isLoading.value = false
                }
        }
        searchJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    private fun performSearch(query: String) {
        _isLoading.value = true
        searchJob = launchCatching {
            searchContacts(query)
                .collect { users ->
                    _searchResults.value = users
                    _isLoading.value = false
                }
        }
        searchJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    fun createChatRoom(uid: String, openAndPopUp: (String, String) -> Unit){
        val participants = setOf(currentUserId, uid).toList()
        launchCatching {
            val chatId = createChatId(participants, isGroup = false)
            createChat(participants, chatId, isGroup = false)
            openAndPopUp(NavRoutes.Chat.replace("{chatId}", chatId), NavRoutes.NewConversation)
        }
    }

    fun updateGroupName(newName: String){
        _uiStateChatMetadata.value = _uiStateChatMetadata.value.copy(groupName = newName)
    }

    fun updateGroupPhotoUrl(newPhoto: String){
        _uiStateChatMetadata.value = _uiStateChatMetadata.value.copy(groupPhotoUrl = newPhoto)
    }

    fun addParticipant(user: UserData) {
        _selectedParticipants.value += user
    }

    fun removeParticipant(user: UserData) {
        _selectedParticipants.value -= user
    }

    fun clearParticipants() {
        _selectedParticipants.value = emptySet()
    }

    fun participantsGroup(openScreen: (String) -> Unit){
        Log.d("participantsGroup", "participantsGroup VIEWMODEL: ${_selectedParticipants.value.size}")
        if(_selectedParticipants.value.isEmpty()){
            SnackbarManager.showMessage(R.string.warning_participants)
            return
        }
        launchCatching {
            _participants.value = emptyList()
            val selectedUsers = _selectedParticipants.value.toMutableList()
            val currentUser = getUser(currentUserId)
            if(currentUser !=null){
                _participants.value = selectedUsers + currentUser
            } else {
                return@launchCatching
            }
            participantsGroupIds = (_participants.value.map{it.uid}).toMutableList()
            Log.d("participantsGroup", "IDs finales a buscar: ${participantsGroupIds.joinToString()}")

            Log.d("participantsGroup", "¡PARTICIPANTS! Usuarios encontrados: ${_participants.value.size}")
            delay(50)
            openScreen(NavRoutes.SetGroupChat)
        }
    }

    fun createGroup(openAndPopUp: (String, String) -> Unit){
        launchCatching {
            val groupNameFromInput = _uiStateChatMetadata.value.groupName
            val finalGroupName = if(groupNameFromInput.isNullOrBlank()){
                user = _participants.value.find { it.uid == currentUserId }
                val firstName = user?.name?.split(" ")?.getOrNull(0)
                if(!firstName.isNullOrBlank()){
                    "Chat Grupo $firstName"
                } else {
                    val randomNumber = (1..9999).random()
                    "Grupo $randomNumber"
                }
            } else {
                groupNameFromInput
            }

            val chatYaExists = chatExists(participantsGroupIds, finalGroupName)
            if (chatYaExists) {
                SnackbarManager.showMessage(R.string.chat_already_exists)
                return@launchCatching
            }
            val chatId = createChatId(participantsGroupIds, isGroup = true)
            val finalPhotoUrl = onSavePhoto(chatId)
            createChat(participantsGroupIds, chatId, isGroup = true, finalGroupName, finalPhotoUrl)
            _uiStateChatMetadata.value = _uiStateChatMetadata.value.copy(
                chatId = chatId,
                participants = participantsGroupIds,
                isGroup = true,
                groupName = finalGroupName,
                groupPhotoUrl = finalPhotoUrl
            )
            // clean el estado de los participantes
            clearParticipants()
            _participants.value = emptyList()
            participantsGroupIds = mutableListOf()
            _uiStateChatMetadata.value = ChatMetadata()
            //Navigation to chat screen
            openAndPopUp(NavRoutes.Chat.replace("{chatId}", chatId), NavRoutes.GroupChat)
        }
    }

    private suspend fun onSavePhoto(chatId: String): String{
        val localPhotoUri = _uiStateChatMetadata.value.groupPhotoUrl
        if (localPhotoUri.isNullOrBlank()) {
            return DEFAULT_AVATAR_GROUP
        }
        _isSaving.value = true
        return try {
            val remotePath = "profile_images/${chatId}.jpg"
            // Upload photo to Firebase Storage
            uploadPhoto(localPhotoUri, remotePath)
            // Download URL of the uploaded photo
            downloadUrlPhoto(remotePath)
        } finally {
            _isSaving.value = false
        }
    }

    fun openScreenAddNewContact(openScreen: (String) -> Unit){
        openScreen(NavRoutes.NewContact)
    }

    fun updateNumberContact(newNumber: String){
        _numberContact.value = newNumber
    }

    fun addNewContact(number: String, openAndPopUp: (String, String) -> Unit){
        launchCatching {
            val contactAdded = addContact(number.numberFirebaseEcu())
            if(contactAdded){
                openAndPopUp(NavRoutes.NewConversation, NavRoutes.NewContact)
            } else {
                SnackbarManager.showMessage(R.string.error_adding_contact)
            }
        }
    }

    fun onDeleteContact(uid: String){
        launchCatching {
            deleteContact(uid)
        }
    }

}