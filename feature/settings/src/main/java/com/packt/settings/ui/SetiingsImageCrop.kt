package com.packt.settings.ui

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import com.packt.chat.feature.settings.R
import com.packt.ui.composables.BasicToolbar
import com.packt.ui.avatar.saveBitmapToCache
import com.packt.ui.composables.DialogCancelButton
import com.packt.ui.composables.DialogConfirmButton
import com.packt.ui.ext.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen0(
    dataUser: (Uri, String, String) -> Unit = { _, _, _ -> }
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BasicToolbar(R.string.settings_1)
        },
        content = { innerPadding ->
            SettingsScreenContent0(
                modifier = Modifier.padding(innerPadding),
                dataUser = dataUser
            )
        }
    )
}

@Composable
fun SettingsScreenContent0(
    modifier: Modifier = Modifier,
    dataUser: (Uri, String, String) -> Unit
){
    var nameUser by remember { mutableStateOf("") }
    var numberUser by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showWarningDialog by remember { mutableStateOf(false) }

    var imageToCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageCropInstance by remember { mutableStateOf<ImageCrop?>(null) } // Para mantener la instancia de ImageCrop

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = context.uriToBitmap(it)
            if(bitmap != null){
                imageToCropBitmap = bitmap
            } else {
                Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (imageToCropBitmap != null) {
        LaunchedEffect(imageToCropBitmap) {
            imageToCropBitmap?.let { bitmap ->
                imageCropInstance = ImageCrop(bitmap)
            }
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp), // Añade padding alrededor del cropper
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Crop Your Image",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ImageCropView necesita una instancia de ImageCrop
            imageCropInstance?.let { cropper ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Black)
                    .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) { // Da espacio al cropper
                    cropper.ImageCropView(
                        modifier = Modifier, // El cropper llena el Box
                        guideLineColor = Color.LightGray,
                        guideLineWidth = 2.dp,
                        edgeCircleSize = 10.dp, // Un poco más grande para mejor tacto
                        showGuideLines = true,
                        cropType = CropType.FREE_STYLE,
                        edgeType = EdgeType.CIRCULAR
                    )
                }
            } ?: Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) // Placeholder si imageCropInstance aún no está listo
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                OutlinedButton(onClick = {
                    imageToCropBitmap = null
                    imageCropInstance = null
                }) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        imageCropInstance?.let { cropper ->
                            val croppedBitmap = cropper.onCrop()
                            scope.launch(Dispatchers.IO) {
                                val newUri = saveBitmapToCache(context, croppedBitmap)
                                scope.launch(Dispatchers.Main) {
                                    if(newUri != null) {
                                        photoUri = newUri
                                    } else {
                                        Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                                    }
                                    imageToCropBitmap = null
                                    imageCropInstance = null
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                stringResource(R.string.app_name),
                color = Color.Green,
                fontSize = 48.sp,
                modifier = Modifier.padding(12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center // Center the content
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ){
                    // Show image
                    AsyncImage(
                        model = photoUri ?: ImageRequest.Builder(context)
                            .data(R.drawable.profile0)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Button to select image
                    Button(
                        onClick = {
                            pickImageLauncher.launch("image/*")
                        }
                    ) {
                        Text(stringResource(R.string.select_photo))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nameUser,
                onValueChange = {nameUser = it},
                label = {Text(stringResource(R.string.user_name))},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.Green,
                    unfocusedLabelColor = Color.Green
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = numberUser,
                onValueChange = {numberUser = it},
                label = {Text(stringResource(R.string.user_number))},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.Green,
                    unfocusedLabelColor = Color.Green
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {dataUser(photoUri!!, nameUser, numberUser)}
                ) {
                    Text(stringResource(R.string.send))
                }
                OutlinedButton(
                    onClick = {showWarningDialog = true},
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }

            if(showWarningDialog){
                val context = LocalContext.current
                val activity = context as? Activity

                AlertDialog(
                    title = {Text(stringResource(R.string.warning))},
                    text = {Text(stringResource(R.string.text_warning))},
                    dismissButton = { DialogCancelButton(R.string.keep){showWarningDialog = false} },
                    confirmButton = { DialogConfirmButton(R.string.left){
                        showWarningDialog = false
                        activity?.finishAffinity() // cerrar app
                    } },
                    onDismissRequest = { showWarningDialog = false}
                )
            }
        }
    }
}






