package com.packt.chat.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.packt.chat.feature.chat.R
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar
import com.packt.ui.avatar.DEFAULT_AVATAR_GROUP
import com.packt.ui.profile.ItemEditProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsGroupScreenContent(
    onBackClick: () -> Unit,
    chatMetadata: ChatMetadata,
    currentUser: UserData?,
    otherParticipants: List<UserData>,
    onPhotoClick: () -> Unit,
    onNameClick: () -> Unit
){
    val participants: MutableList<UserData> = mutableListOf()
    participants.add(currentUser?:UserData())
    participants.addAll(otherParticipants)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(stringResource(R.string.details))},
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            ProfileGroupChat(
                chatMetadata = chatMetadata,
                currentUser = currentUser,
                participants = participants,
                onPhotoClick = onPhotoClick,
                onNameClick = onNameClick,
                modifier = Modifier
                    .padding(innerPadding)
                    .navigationBarsPadding()
            )
        }
    )
}


@Composable
fun ProfileGroupChat(
    chatMetadata: ChatMetadata,
    currentUser: UserData?,
    participants: MutableList<UserData>,
    onPhotoClick: () -> Unit,
    onNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserUid = currentUser?.uid ?: ""
    LazyColumn(modifier = modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            GeneralDataGroup(
                chatMetadata = chatMetadata,
                onPhotoClick = onPhotoClick,
                onNameClick = onNameClick
            )
        }
        item{
            Text(
                text = stringResource(R.string.group_participants),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }
        items(participants) { user ->
            ParticipantItem(user = user, currentUserUid = currentUserUid)
        }
    }
}

@Composable
private fun GeneralDataGroup(
    chatMetadata: ChatMetadata,
    onPhotoClick: () -> Unit,
    onNameClick: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayName = chatMetadata.groupName ?: stringResource(R.string.unknow_group)
        val displayPhotoUrl = chatMetadata.groupPhotoUrl ?: DEFAULT_AVATAR_GROUP
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable {onPhotoClick()},
            contentAlignment = Alignment.Center // Center the content
        ) {
            Avatar(
                photoUri = displayPhotoUrl,
                size = 140.dp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            ItemEditProfile(icon = R.drawable.name, title = R.string.group_name, data = displayName) { onNameClick() }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ParticipantItem(user: UserData, currentUserUid: String){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Left
    ) {
        Avatar(
            photoUri = user.photoUrl,
            size = 50.dp,
            contentDescription = "${user.name}'s avatar"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            val displayName = if (currentUserUid == user.uid) {
                "${user.name ?: ""} (${stringResource(R.string.you)})"
            } else {user.name ?: ""}
            Text(
                text = displayName,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}