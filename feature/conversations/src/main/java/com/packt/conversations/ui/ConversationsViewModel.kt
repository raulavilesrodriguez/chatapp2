package com.packt.conversations.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.packt.conversations.domain.usecases.GetConversations
import com.packt.conversations.domain.usecases.GetCurrentUserId
import com.packt.conversations.domain.usecases.GetUser
import com.packt.conversations.ui.model.Conversation
import com.packt.conversations.ui.model.toConversation
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import com.packt.ui.navigation.NavRoutes
import com.packt.ui.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val getConversations: GetConversations,
    private val getUser: GetUser,
    private val currentUserIdUseCase: GetCurrentUserId,
) : BaseViewModel() {

    // La clave es el ID del usuario, el valor son sus datos.
    private val usersCache = mutableMapOf<String, UserData>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversations: StateFlow<List<Conversation>> = flow {
        // 1. Llama a la suspend function DENTRO de un constructor de Flow.
        //    Este bloque es un CoroutineScope.
        emit(getConversations())
    }
        .flatMapLatest { conversationsFlow ->
            // nos da el Flow<List<ChatMetadata>>, ahora lo aplanamos
            conversationsFlow
        }
        .flatMapLatest { metadataList ->
            // Ahora que tenemos la List<ChatMetadata>, la enriquecemos
            enrichConversations(metadataList)
        }
        .flowOn(Dispatchers.IO) // ejecuta toda la cadena de arriba
        .stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val currentUserId
        get() = currentUserIdUseCase()

    private fun enrichConversations(metadataList: List<ChatMetadata>): Flow<List<Conversation>> = flow {
        val enrichedConversations = metadataList.map { metadata ->
            val otherUsers = metadata.participants
                .mapNotNull { getOrFetchUser(it) }

            metadata.toConversation(otherUsers, currentUserId)
        }
        emit(enrichedConversations)
    }

    private suspend fun getOrFetchUser(userId: String): UserData? {
        if (usersCache.containsKey(userId)) {
            return usersCache[userId]
        }
        return try {
            getUser(userId)?.also { usersCache[userId] = it }
        } catch (e: Exception) {
            Log.e("ConversationsViewModel OJO", "OJO Error fetching user: $userId", e)
            null
        }
    }

    fun onNewConversationClick(openScreen: (String) -> Unit) = openScreen(NavRoutes.NewConversation)

    fun onConversationClick(openScreen: (String) -> Unit, chatId: String){
        openScreen(NavRoutes.Chat.replace("{chatId}", chatId))
    }

    fun onActionClick(openScreen: (String) -> Unit, action: Int){
        when(ActionOptions.getById(action)){
            ActionOptions.GROUP -> openScreen(NavRoutes.GroupChat)
            ActionOptions.SETTINGS -> openScreen(NavRoutes.EditUser)
        }
    }

}