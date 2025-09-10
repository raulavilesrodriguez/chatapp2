package com.packt.settings.ui

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.packt.ui.composables.BasicToolbar
import com.packt.settings.R
import com.packt.settings.ui.model.SetUserData
import com.packt.ui.avatar.Avatar
import com.packt.ui.composables.BasicBottomBar
import com.packt.ui.composables.DialogCancelButton
import com.packt.ui.composables.DialogConfirmButton
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun SettingsScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BasicToolbar(R.string.settings)
        },
        bottomBar = {
            BasicBottomBar(R.string.made_by)
        },
        content = { innerPadding ->
            SettingsScreenContent(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                updatePhotoUri = viewModel::updatePhotoUri,
                updateName = viewModel::updateName,
                updateNumber = viewModel::updateNumber,
                onSettingClick = {viewModel.onSettingClick(openAndPopUp)}
            )
        }
    )
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    uiState: SetUserData,
    updatePhotoUri: (String) -> Unit,
    updateName: (String) -> Unit,
    updateNumber: (String) -> Unit,
    onSettingClick: () -> Unit
){
    var showWarningDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            if (resultUri != null) {
                updatePhotoUri(resultUri.toString())
            } else {
                Toast.makeText(context, "Error al recortar", Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(context, "Error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
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
            fontSize = 40.sp,
            modifier = Modifier.padding(8.dp)
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
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable {
                            pickImageLauncher.launch("image/*")
                        }
                ) {
                    Avatar(
                        photoUri = uiState.photoUri,
                        size = 200.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = {updateName(it)},
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
            value = uiState.number,
            onValueChange = {updateNumber(it)},
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
                onClick = {onSettingClick()}
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


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview(){
    MaterialTheme {
        SettingsScreenContent(
            uiState = SetUserData(),
            updatePhotoUri = {},
            updateName = {},
            updateNumber = {},
            onSettingClick = {}
        )
    }
}