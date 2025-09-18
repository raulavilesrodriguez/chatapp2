package com.packt.settings.ui.model

import com.packt.domain.user.UserData

const val DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/chatapp-3c587.firebasestorage.app/o/profile0.jpg?alt=media&token=f01e11ca-9f52-48c8-ae8b-dfa6d37dc12a"

data class SetUserData(
    val photoUri: String = DEFAULT_AVATAR_URL,
    val name: String = "",
    val number: String = ""
)

fun UserData.toSetUserData(): SetUserData {
    return SetUserData(
        photoUri = photoUrl,
        name = name,
        number = number
    )
}

fun SetUserData.toUserData(uid: String): UserData {
    return UserData(
        uid = uid,
        photoUrl = photoUri,
        name = name,
        number = number
    )
}

