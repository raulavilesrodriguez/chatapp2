package com.packt.chat.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.packt.chat.R
import com.packt.chat.ui.model.Message
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@Composable
fun ChatScreen(
    chatId: String?,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val sendText by viewModel.sendText.collectAsState()
    val messages by viewModel.messages.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.reloadCurrentUser()
    }

    LaunchedEffect(Unit) {
        viewModel.loadChatInformation(chatId.orEmpty())
    }

    // to update unReadCount in firestore of UserData model
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setChatActive()
                Lifecycle.Event.ON_PAUSE -> viewModel.setChatInactive()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ChatScreenContent(
        onBackClick = onBackClick,
        participant = uiState,
        sendText = sendText,
        updateSendText = viewModel::updateSendText,
        onSendMessage = viewModel::onSendMessage,
        messages = messages,
        onLoadMoreMessages = { viewModel.loadMoreMessages(chatId.orEmpty())}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    onBackClick: () -> Unit,
    participant: UserData,
    sendText: String,
    updateSendText: (String) -> Unit,
    onSendMessage: ()->Unit,
    messages: List<Message>,
    onLoadMoreMessages: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            ChatToolbar(
                iconBack = R.drawable.arrow_back,
                onBackClick = onBackClick,
                participant = participant
            )
        },
        bottomBar = {
            SendMessageBox(
                onSendMessage = onSendMessage,
                sendText = sendText,
                updateSendText = updateSendText
            )
        }
    ) { paddingValues ->
        ListOfMessages(
            messages = messages,
            paddingValues = paddingValues,
            onLoadMoreMessages = onLoadMoreMessages,
            participant = participant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbar(
    @DrawableRes iconBack: Int,
    onBackClick: () -> Unit,
    participant: UserData
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBackClick() }){
            Icon(
                painter = painterResource(id = iconBack),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        val photoSource: Any = participant.photoUrl
        Avatar(
            photoUri = photoSource,
            size = 40.dp,
            contentDescription = "${participant.name}'s avatar"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = participant.name?:"",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
fun SendMessageBox(
    onSendMessage: ()->Unit,
    sendText: String,
    updateSendText: (String) -> Unit
) {
    Box(modifier = Modifier
        .navigationBarsPadding()
        .defaultMinSize()
        .padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = sendText,
            onValueChange = { updateSendText(it) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterStart)
                .height(56.dp),
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(56.dp),
            enabled = sendText.isNotBlank(),
            onClick = {
                onSendMessage()
                //updateSendText("")
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.send),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Send message"
            )
        }
    }
}

@Composable
fun ListOfMessages(
    messages: List<Message>,
    paddingValues: PaddingValues,
    onLoadMoreMessages: () -> Unit,
    participant: UserData
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // detecta cuando llega al tope de la lista
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo } // observe changes in the layout
            .distinctUntilChanged { old, new ->
                old.visibleItemsInfo.lastOrNull()?.index == new.visibleItemsInfo.lastOrNull()?.index
            }
            .collect { layoutInfo ->
                // Obtenemos el índice del último item visible en la pantalla
                // que en reverseLayout=true es el que está MÁS ARRIBA.
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                // Si no hay items o no hay índice, no hacemos nada.
                if (lastVisibleItemIndex == null) return@collect
                // El número total de items que tenemos cargados actualmente.
                val totalItemsCount = layoutInfo.totalItemsCount
                // Condición: Si el índice del último item visible (el de arriba)
                // está cerca del final de nuestra lista de datos, es hora de cargar más.
                if (lastVisibleItemIndex >= totalItemsCount - 5) {
                    onLoadMoreMessages()
                }
            }
    }

    // Se dispara cada vez que la lista de mensajes cambia.
    LaunchedEffect(messages.firstOrNull()?.id) {
        // Si el primer item de la lista (el más nuevo) no está completamente visible,
        // o si hemos añadido un nuevo mensaje y el scroll no está en la posición 0,
        // hacemos scroll al principio.
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                // AnimateScrollToItem es más suave que scrollToItem
                listState.animateScrollToItem(index = 0)
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            //verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            reverseLayout = true // ojo
        ) {
            items(
                messages,
                key = {message -> message.id}
            ) { message ->
                MessageItem(message = message, participant = participant)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatToolbarPreview(){
    MaterialTheme {
        val participant = UserData(
            uid = "1we2",
            name = "Mayra",
            photoUrl = "https://i.pravatar.cc/300?img=10"
        )
        ChatToolbar(
            iconBack = R.drawable.arrow_back,
            onBackClick = {},
            participant = participant
        )
    }
}
