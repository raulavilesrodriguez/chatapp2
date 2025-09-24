package com.packt.create_chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar

@Composable
fun UserItem(user: UserData){
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
            Text(
                text = user.name ?: "",
                modifier = Modifier.fillMaxWidth()
            )
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
                photoUrl = "https://i.pravatar.cc/150?u=1"
            )
        )
    }
}