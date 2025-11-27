package com.packt.ui.group

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.packt.domain.user.UserData

@Composable
fun ParticipantsGroup(
    modifier: Modifier = Modifier,
    users: List<UserData>,
    onUserClick: (user: UserData) -> Unit,
    selectedParticipants: Set<UserData>,
    @DrawableRes iconChoose: Int
){
    LazyColumn(modifier = modifier) {
        items(users) { user ->
            val isSelected = selectedParticipants.contains(user)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(user)}
            ) {
                ParticipantItem(user = user, isSelected = isSelected, iconChoose)
            }
        }
    }
}