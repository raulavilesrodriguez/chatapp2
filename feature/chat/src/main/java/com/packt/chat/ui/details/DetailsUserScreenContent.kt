package com.packt.chat.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.packt.chat.feature.chat.R
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar
import com.packt.ui.avatar.DEFAULT_AVATAR
import com.packt.ui.profile.ItemProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsUserScreenContent(
    onBackClick: () -> Unit,
    currentUser: UserData?,
    otherParticipants: List<UserData>
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details)) },
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
            ProfileUserChat(
                currentUser = currentUser,
                otherParticipants = otherParticipants,
                modifier = Modifier
                    .padding(innerPadding)
                    .navigationBarsPadding()
            )
        }
    )
}

@Composable
fun ProfileUserChat(
    currentUser: UserData?,
    otherParticipants: List<UserData>,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayName: String = when(otherParticipants.size){
            0 -> "${currentUser?.name ?: ""} (${stringResource(R.string.you)})"
            else -> otherParticipants[0].name?: stringResource(R.string.unknow_user)
        }
        val displayPhotoUrl: String = when(otherParticipants.size){
            0 -> currentUser?.photoUrl?: DEFAULT_AVATAR
            else -> otherParticipants[0].photoUrl
        }
        val phoneNumber: String = when(otherParticipants.size){
            0 -> currentUser?.number?: ""
            else -> otherParticipants[0].number
        }

        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable {},
            contentAlignment = Alignment.Center // Center the content
        ) {
            Avatar(
                photoUri = displayPhotoUrl,
                size = 140.dp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            ItemProfile(icon = R.drawable.name, title = R.string.user_name, data = displayName)
            ItemProfile(icon = R.drawable.phone, title = R.string.user_number, data = phoneNumber)
        }
    }
}
