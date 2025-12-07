package com.packt.create_chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.packt.chat.feature.create_chat.R
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar

@Composable
fun UserItem(user: UserData, currentUserUid: String, onDeleteContact: (uid:String) -> Unit){
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
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if(currentUserUid != user.uid){
            IconButton(
                onClick = { onDeleteContact(user.uid) }
            ) {
                Icon(painter = painterResource(R.drawable.twotone_cancel), contentDescription = "delete")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserItemPreview(){
    MaterialTheme {
        UserItem(
            user = UserData(
                name = "Carla Becerra ❤\uFE0F\u200D\uD83E\uDE79",
                photoUrl = "https://i.pravatar.cc/150?u=1",
                uid = "1y"
            ),
            currentUserUid = "1y2",
            onDeleteContact = {}
        )
    }
}