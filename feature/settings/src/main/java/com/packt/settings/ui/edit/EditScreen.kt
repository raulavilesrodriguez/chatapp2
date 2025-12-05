package com.packt.settings.ui.edit

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.feature.settings.R
import com.packt.domain.user.UserData
import com.packt.settings.ui.model.DEFAULT_AVATAR_URL
import com.packt.ui.photo.pickImageLauncher
import com.packt.ui.avatar.Avatar
import com.packt.ui.composables.ProfileToolBar
import com.packt.ui.profile.ItemEditProfile


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    openScreen: (String) -> Unit,
    popUp: () -> Unit,
    clearAndNavigate: (String) -> Unit,
    viewModel: EditViewModel = hiltViewModel()
){
    val userData by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDialogSignOut by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.reloadUserData()
    }

    val imagePicker = pickImageLauncher(
        context = LocalContext.current,
        updatePhotoUri = { newUri ->
            // Y ocultamos el sheet
            showBottomSheet = false
            // Cuando la imagen se recorta con éxito, actualizamos el ViewModel
            viewModel.onSavePhotoClick(newUri, openScreen)
        },
        errorCropping = R.string.error_cropping,
        errorSaving = R.string.error_saving
    )

    EditScreenContent(
        userData = userData,
        onNameClick = {viewModel.onName(openScreen)},
        onEditPhotoClick = { showBottomSheet = true },
        backClick = popUp,
        isSaving = isSaving,
        onSignOut = {showDialogSignOut = true}
    )

    if (showBottomSheet) {
        ProfilePhotoBottomSheet(
            onDismiss = { showBottomSheet = false },
            onGalleryClick = {
                // Lanzamos la galería desde el BottomSheet
                imagePicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
    if (showDialogSignOut){
        AlertDialog(
            onDismissRequest = { showDialogSignOut = false},
            title = {Text(stringResource(R.string.sign_out))},
            text = {Text(stringResource(R.string.sign_out_text))},
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialogSignOut = false
                        viewModel.signOut(clearAndNavigate)
                    }
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {showDialogSignOut = false}
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoBottomSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit = {}
){
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.profile_photo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
        // Options of the list
        Column(
            modifier = Modifier
                .navigationBarsPadding() //padding para la barra de navegation of the system
        ) {
            SheetItem(
                icon = Icons.Default.PhotoLibrary,
                title = R.string.profile_photo,
                onClick = onGalleryClick
            )
            // mas SheetItem() para Camara, Avatar
        }
    }
}

@Composable
private fun SheetItem(
    icon: ImageVector,
    @StringRes title: Int,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(text = stringResource(id = title), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun EditScreenContent(
    userData: UserData?,
    onNameClick: () -> Unit,
    onEditPhotoClick: () -> Unit,
    backClick: () -> Unit,
    isSaving: Boolean,
    onSignOut: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.settings_edit,
                backClick = backClick
            )
                 },
        content = { innerPadding ->
            Profile(
                userData = userData,
                isSaving = isSaving,
                onNameClick = onNameClick,
                onEditPhotoClick = onEditPhotoClick,
                onSignOut = onSignOut,
                modifier = Modifier
                    .padding(innerPadding)
                    .navigationBarsPadding() // evita superponerse con la parte inferior
            )
        }
    )
}

@Composable
fun Profile(
    userData: UserData?,
    isSaving: Boolean,
    onNameClick: () -> Unit,
    onEditPhotoClick: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable {},
            contentAlignment = Alignment.Center // Center the content
        ) {
            Avatar(
                photoUri = userData?.photoUrl?: DEFAULT_AVATAR_URL,
                size = 200.dp
            )
            if(isSaving){
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 4.dp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = stringResource(R.string.settings_edit_photo),
            color = Color.Blue,
            textAlign = TextAlign.Center,
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .padding(16.dp)
                .clickable { onEditPhotoClick() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ){
            ItemEditProfile(icon = R.drawable.name, title = R.string.user_name, data= userData?.name ?:"") {onNameClick() }
            ItemEditProfile(icon = R.drawable.phone, title = R.string.user_number, data= userData?.number ?:"" ){}
            ItemEditProfile(icon = R.drawable.sign_out, title = R.string.sign_out, data= stringResource(R.string.sign_out_data, userData?.name ?:"")) { onSignOut() }
            //ItemProfile(icon = R.drawable.info, title = R.string.info, data= "en el gym" ){}
            //ItemProfile(icon = R.drawable.link, title = R.string.links, data= "enlaces") { }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditScreenPreview(){
    MaterialTheme {
        val user1 = UserData(
            name = "Carla Becerra ❤\uFE0F\u200D\uD83E\uDE79",
            photoUrl = "https://i.pravatar.cc/150?u=1",
            number = "0968804849",
            uid = "1y"
        )
        EditScreenContent(
            userData = user1,
            onNameClick = {},
            onEditPhotoClick = {},
            backClick = {},
            isSaving = false,
            onSignOut = {}
        )
    }
}