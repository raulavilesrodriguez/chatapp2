package com.packt.create_chat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.packt.domain.user.UserData

@Composable
fun UserList(
    modifier: Modifier = Modifier,
    users: List<UserData>,
    onUserClick: (uid: String) -> Unit,
    currentUserUid: String
){
    LazyColumn(modifier = modifier) {
        items(users) { user ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onUserClick(user.uid)}
            ) {
                UserItem(user = user, currentUserUid = currentUserUid)
            }
        }
    }
}