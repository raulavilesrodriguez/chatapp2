package com.packt.domain.user

data class UserData(
    val uid: String = "",
    val name: String? = null,
    val nameLowercase: String? = null,
    val number: String = "",
    val photoUrl: String = "",
)
