package com.packt.conversations.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.packt.conversations.ui.model.Conversation

@Composable
fun ConversationList(
    conversations: List<Conversation>,
    onConversationClick: (chatId: String) -> Unit
){
    LazyColumn {
        items(conversations) { conversation ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onConversationClick(conversation.id)}
            ) {
                ConversationItem(
                    conversation = conversation
                )
            }
        }
    }
}