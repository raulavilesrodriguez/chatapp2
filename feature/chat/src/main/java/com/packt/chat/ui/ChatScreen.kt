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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.request.ImageRequest
import com.packt.chat.R
import com.packt.chat.ui.model.Chat
import com.packt.chat.ui.model.Message
import com.packt.chat.ui.model.MessageContent
import com.packt.ui.avatar.Avatar


@Composable
fun ChatScreen(
    chatId: String?,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val sendText by viewModel.sendText.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChatInformation(chatId.orEmpty())
    }

    ChatScreenContent(
        onBackClick = onBackClick,
        participant = uiState,
        sendText = sendText,
        updateSendText = viewModel::updateSendText,
        onSendMessage = viewModel::onSendMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    onBackClick: () -> Unit,
    participant: Chat,
    sendText: String,
    updateSendText: (String) -> Unit,
    onSendMessage: ()->Unit,
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
            messages = getFakeMessages(),
            paddingValues = paddingValues
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbar(
    @DrawableRes iconBack: Int,
    onBackClick: () -> Unit,
    participant: Chat
){
    val context = LocalContext.current

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
        val photoSource: Any = participant.avatar ?: ImageRequest.Builder(context)
            .data(R.drawable.profile0)
            .build()
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
                updateSendText("")
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
fun ListOfMessages(messages: List<Message>, paddingValues: PaddingValues) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages) { message ->
                    MessageItem(message = message)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatToolbarPreview(){
    MaterialTheme {
        val participant = Chat(
            id = "1we2",
            name = "Mayra",
            avatar = "https://i.pravatar.cc/300?img=10"
        )
        ChatToolbar(
            iconBack = R.drawable.arrow_back,
            onBackClick = {},
            participant = participant
        )
    }
}

fun getFakeMessages(): List<Message> {
    return listOf(
        Message(
            id = "1",
            senderName = "Alice",
            senderAvatar = "https://i.pravatar.cc/300?img=1",
            isMine = false,
            timestamp = "10:00",
            messageContent = MessageContent.TextMessage(
                message = "Hi, how are you?"
            )
        ),
        Message(
            id = "2",
            senderName = "Lucy",
            senderAvatar = "https://i.pravatar.cc/300?img=2",
            isMine = true,
            timestamp = "10:01",
            messageContent = MessageContent.TextMessage(
                message = "I'm good, thank you! And you?"
            )
        ),
        Message(
            id = "3",
            senderName = "Alice",
            senderAvatar = "https://i.pravatar.cc/300?img=1",
            isMine = false,
            timestamp = "10:02",
            messageContent = MessageContent.TextMessage(
                message = "Super fine!"
            )
        ),
        Message(
            id = "4",
            senderName = "Lucy",
            senderAvatar = "https://i.pravatar.cc/300?img=1",
            isMine = true,
            timestamp = "10:02",
            messageContent = MessageContent.TextMessage(
                message = "Are you going to the Kotlin conference?"
            )
        ),
        Message(
            id = "5",
            senderName = "Alice",
            senderAvatar = "https://i.pravatar.cc/300?img=1",
            isMine = false,
            timestamp = "10:03",
            messageContent = MessageContent.TextMessage(
                message = "Of course! I hope to see you there!"
            )
        ),
        Message(
            id = "5",
            senderName = "Alice",
            senderAvatar = "https://i.pravatar.cc/300?img=1",
            isMine = false,
            timestamp = "10:03",
            messageContent = MessageContent.TextMessage(
                message = "I'm going to arrive pretty early"
            )
        ),
        Message(
            id = "5",
            senderName = "Alice",
            senderAvatar = "https://i.pravatar.cc/300?img=1",
            isMine = false,
            timestamp = "10:03",
            messageContent = MessageContent.TextMessage(
                message = "So maybe we can have a coffee together"
            )
        ),
        Message(
            id = "5",
            senderName = "Alice",
            senderAvatar = "https://i.pravatar.cc/300?img=1",
            isMine = false,
            timestamp = "10:03",
            messageContent = MessageContent.TextMessage(
                message = "Wdyt?"
            )
        ),
    )
}
