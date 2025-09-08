package com.packt.ui.ext

import java.util.regex.Pattern

private const val MIN_PASS_LENGTH = 10
private const val ECUADOR_MOBILE_PATTERN = "^09\\d{8}$"

fun String.isValidNumber(): Boolean {
    return this.isNotBlank() &&
            this.length == MIN_PASS_LENGTH &&
            Pattern.compile(ECUADOR_MOBILE_PATTERN).matcher(this).matches()
}

fun String.numberFirebaseEcu(): String {
    // Quitar espacios o guiones por si acaso
    val numberClean = this.replace("\\s+".toRegex(), "").replace("-", "")

    return "+593" + numberClean.substring(1)
}