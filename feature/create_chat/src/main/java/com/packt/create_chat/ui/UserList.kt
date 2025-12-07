package com.packt.create_chat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.packt.chat.feature.create_chat.R
import com.packt.domain.user.UserData

@Composable
fun UserList(
    modifier: Modifier = Modifier,
    users: List<UserData>,
    onUserClick: (uid: String) -> Unit,
    currentUserUid: String,
    onDeleteContact: (uid:String) -> Unit
){
    Text(
        text = stringResource(R.string.contacts),
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
    )
    LazyColumn(modifier = modifier) {
        items(users) { user ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onUserClick(user.uid)}
            ) {
                UserItem(user = user, currentUserUid = currentUserUid, onDeleteContact = onDeleteContact)
            }
        }
    }
}