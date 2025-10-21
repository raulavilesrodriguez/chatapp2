package com.packt.create_chat.ui

import com.packt.create_chat.domain.usecases.CreateChat
import com.packt.create_chat.domain.usecases.GetCurrentUserId
import com.packt.create_chat.domain.usecases.GetUsers
import com.packt.create_chat.domain.usecases.SearchUsers
import com.packt.domain.user.UserData
import com.packt.ui.navigation.NavRoutes
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
    private val createChat: CreateChat,
    private val currentUserIdUseCase: GetCurrentUserId,
): BaseViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserData>>(emptyList())
    val searchResults: StateFlow<List<UserData>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

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
            getUsers()
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
            searchUsers(query)
                .collect { users ->
                    _searchResults.value = users
                    _isLoading.value = false
                }
        }
        searchJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    fun createChatRoom(uid: String, openScreen: (String) -> Unit){
        val participants = setOf(currentUserId, uid).toList()
        launchCatching {
            val chatId = createChat(participants)
            openScreen(NavRoutes.Chat.replace("{chatId}", chatId))
        }
    }

}