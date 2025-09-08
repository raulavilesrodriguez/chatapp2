package com.packt.ui.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri

fun Context.uriToBitmap(uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(this.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true // Importante para algunas operaciones de bitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}