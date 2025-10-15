package com.packt.settings.ui.photo

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.packt.settings.R
import com.packt.ui.snackbar.SnackbarManager
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun pickImageLauncher(
    context: Context,
    updatePhotoUri: (String) -> Unit,
) : ActivityResultLauncher<String> {
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            if (resultUri != null) {
                updatePhotoUri(resultUri.toString())
            } else {
                SnackbarManager.showMessage(R.string.error_cropping)
                //Toast.makeText(context, "Error al recortar", Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            val errorMessage = context.getString(
                R.string.error_saving, cropError?.message ?: "")
            SnackbarManager.showMessage(errorMessage)
            //Toast.makeText(context, "Error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Archivo de salida temporal
            val destinationUri = Uri.fromFile(
                File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
            )

            val options = UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(90)
                setToolbarTitle("Recortar Imagen")
                setToolbarColor(Color.Black.toArgb())
                setStatusBarLight(true)
                setToolbarWidgetColor(Color.White.toArgb()) // las letras del toolbar son blancas
                setActiveControlsWidgetColor(Color.Magenta.toArgb())
                setHideBottomControls(false) // 👈 muestra controles de rotación/escala
                setFreeStyleCropEnabled(true) // 👈 permite redimensionar libremente
                setRootViewBackgroundColor(Color.Black.toArgb()) // a veces no basta
                setDimmedLayerColor(Color(0x8C000000).toArgb())
            }

            val intent = UCrop.of(selectedUri, destinationUri)
                .withAspectRatio(1f, 1f) // 👈 cuadrado, puedes cambiarlo
                .withOptions(options)
                .getIntent(context)

            uCropLauncher.launch(intent)
        }
    }
    return pickImageLauncher
}