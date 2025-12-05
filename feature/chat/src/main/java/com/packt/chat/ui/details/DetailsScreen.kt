package com.packt.chat.ui.details

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.feature.chat.R
import com.packt.chat.ui.ChatViewModel
import com.packt.ui.photo.pickImageLauncher

@Composable
fun DetailsScreen(
    onNavigatePopup: (String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = viewModel.user
    val chatMetadata by viewModel.chatMetadata.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.reloadCurrentUser()
    }

    val imagePicker = pickImageLauncher(
        context = LocalContext.current,
        updatePhotoUri = { newUri ->
            viewModel.onSavePhotoGroup(newUri, onNavigatePopup)
        },
        errorCropping = R.string.error_cropping,
        errorSaving = R.string.error_saving
    )

    if(chatMetadata != null){
        if(chatMetadata!!.isGroup){
            DetailsGroupScreenContent(
                onBackClick = onBackClick,
                chatMetadata = chatMetadata!!,
                currentUser = currentUser,
                otherParticipants = uiState,
                onPhotoClick = {
                    // Lanzamos la galería desde el BottomSheet
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onNameClick = {
                    viewModel.onNameGroup(onNavigatePopup)
                }
            )
        } else {
            DetailsUserScreenContent(
                onBackClick = onBackClick,
                currentUser = currentUser,
                otherParticipants = uiState
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
    }
}