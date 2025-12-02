package com.packt.conversations.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.packt.chat.feature.conversations.R
import com.packt.conversations.ui.model.Conversation
import com.packt.domain.model.ContentType
import com.packt.ui.avatar.Avatar
import com.packt.ui.time.formatTimestamp

@Composable
fun ConversationItem(
    conversation: Conversation
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ){
        Avatar(
            photoUri = conversation.displayPhotoUrl,
            size = 50.dp,
            contentDescription = "${conversation.displayName}'s avatar"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            val displayName = if (conversation.isMine) {
                "${conversation.displayName} (${stringResource(R.string.you)})"
            } else {
                conversation.displayName
            }
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                //modifier = Modifier.fillMaxWidth(0.7f)
            )
            when(conversation.messageType){
                ContentType.TEXT -> {
                    Text(
                        text = conversation.lastMessage,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        //modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
                ContentType.IMAGE -> {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            painter = painterResource(R.drawable.photo),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.photo),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            //modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
                ContentType.VIDEO -> {
                    Row(modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                        ){
                        Icon(
                            painter = painterResource(R.drawable.video),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.video),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            //modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(start = 8.dp)
        ) {
            val formattedTimestamp = formatTimestamp(conversation.timestamp)
            Text(
                text = formattedTimestamp,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if(conversation.unreadCount > 0){
                val colorBackground = MaterialTheme.colorScheme.error
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.unreadCount.toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.width(IntrinsicSize.Min)
                        .drawBehind{
                            drawCircle(
                                color = colorBackground,
                                radius = this.size.minDimension
                            )
                        },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationItemPreview(){
    MaterialTheme {
        val conversation = Conversation(
            chatId = "1",
            displayName = "Carla Becerra ❤\uFE0F\u200D\uD83E\uDE79",
            displayPhotoUrl = "https://i.pravatar.cc/150?u=1",
            lastMessage = "hola que tal, que haces tan de maniana jejeje ji, vamos al cine, " +
                    "que dices, por que no respondes, estas ahi, me estoy poniendo agresiva jeje ",
            messageType = ContentType.VIDEO,
            timestamp = 0L,
            unreadCount = 2,
            isGroupChat = false,
            participants = listOf()
        )
        ConversationItem(
            conversation = conversation
        )

    }
}