package com.packt.ui.avatar

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.canhub.cropper.CropImageView
import com.packt.ui.R


@Composable
fun CropperScreen(
    imageUri: Uri,
    onCropSuccess: (Bitmap) -> Unit,
    onCancelled: () -> Unit
){
    var cropImageView by remember { mutableStateOf<CropImageView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ){
        ComposableCropper(
            modifier = Modifier.fillMaxSize(),
            imageUri = imageUri,
            cropImageView = { cropImageView = it }
        )

        TopControlsRow(
            onCancelled = onCancelled,
            onCropSuccess = onCropSuccess,
            cropImageView = cropImageView,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun ComposableCropper(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    cropImageView: (CropImageView) -> Unit
){
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Log.d("CropperDebug", "Factory: Creating CropImageView")
            CropImageView(ctx).apply {
                setImageUriAsync(imageUri) // Cargar la imagen desde la URI
                guidelines = CropImageView.Guidelines.ON
                setFixedAspectRatio(true)
                setAspectRatio(1, 1)
                cropShape = CropImageView.CropShape.OVAL
                cropImageView(this)
            }
        }
    )
}

@Composable
fun TopControlsRow(
    onCancelled: () -> Unit,
    onCropSuccess: (Bitmap) -> Unit,
    cropImageView: CropImageView?,
    modifier: Modifier = Modifier
){
    // action buttons
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(onClick = onCancelled) {
            Text(stringResource(R.string.cancel_crooper), color = Color.White)
        }
        Button(onClick = {
            val cropped: Bitmap? = cropImageView?.getCroppedImage()
            cropped?.let { onCropSuccess(it) }
        }) {
            Text(stringResource(R.string.confirm_crooper), color = Color.White)
        }
    }
}