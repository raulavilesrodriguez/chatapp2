package com.packt.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar
import kotlinx.coroutines.launch

@Composable
fun RowParticipants(
    participants: Set<UserData>,
    removeClick: (UserData) -> Unit
){
    val listState = rememberLazyListState()
    LaunchedEffect(participants.size) {
        if(participants.isNotEmpty()){
            launch{
                listState.animateScrollToItem(participants.size - 1)
            }
        }
    }
    LazyRow(
        state = listState,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(participants.toList()) {
            val firstName = it.name?.split(" ")?.getOrNull(0) ?: ""
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            removeClick(it)
                        }
                ){
                    Avatar(
                        photoUri = it.photoUrl,
                        size = 42.dp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(3.dp)
                            .size(21.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            .padding(3.dp)
                    )
                }
                Text(
                    text = firstName,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}