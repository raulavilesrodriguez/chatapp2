package com.packt.ui.group

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar

@Composable
fun ParticipantItem(
    user: UserData,
    isSelected: Boolean,
    @DrawableRes iconChoose: Int
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Left
    ){
        Avatar(
            photoUri = user.photoUrl,
            size = 50.dp,
            contentDescription = "${user.name}'s avatar"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = user.name ?: "",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (isSelected){
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else{
            Icon(
                painter = painterResource(iconChoose),
                contentDescription = "Selected",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}