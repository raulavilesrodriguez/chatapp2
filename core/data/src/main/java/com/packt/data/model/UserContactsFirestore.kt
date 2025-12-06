package com.packt.data.model

import com.packt.domain.user.UserContacts

data class UserContactsFirestore(
    val uid: String? = null
)

fun UserContactsFirestore.toUserContacts(): UserContacts {
    return UserContacts(
        uid = uid?:""
    )
}