package com.packt.ui.avatar

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri?{
    val fileNamePrefix = "cropped_imagen"
    val cacheDir = context.cacheDir
    val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.jpg"
    val imageFile = File(cacheDir, fileName)

    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.flush()
        return Uri.fromFile(imageFile)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    } finally {
        try {
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}