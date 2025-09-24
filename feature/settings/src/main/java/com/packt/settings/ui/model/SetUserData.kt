package com.packt.settings.ui.model

import com.packt.domain.user.UserData
import com.packt.ui.ext.normalizeName

data class SetUserData(
    val photoUri: String = DEFAULT_AVATAR_URL,
    val name: String = "",
    val number: String = ""
)

fun UserData.toSetUserData(): SetUserData {
    return SetUserData(
        photoUri = photoUrl,
        name = name ?: "",
        number = number
    )
}

fun SetUserData.toUserData(uid: String): UserData {
    return UserData(
        uid = uid,
        photoUrl = photoUri,
        name = name.ifBlank { null },
        nameLowercase = name.normalizeName().ifBlank { null },
        number = number
    )
}

