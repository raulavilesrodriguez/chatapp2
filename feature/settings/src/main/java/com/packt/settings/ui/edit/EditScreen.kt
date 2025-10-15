package com.packt.settings.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.settings.ui.SettingsViewModel
import com.packt.settings.ui.photo.pickImageLauncher

@Composable
fun EditScreen(
    openScreen: (String) -> Unit,
    popUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    EditScreenContent(
        updatePhotoUri = viewModel::updatePhotoUri,
        updateName = viewModel::updateName,
        isSavingProfile = viewModel.isSavingProfile.value
    )
}

@Composable
fun EditScreenContent(
    updatePhotoUri: (String) -> Unit,
    updateName: (String) -> Unit,
    isSavingProfile: Boolean = false
){
    val context = LocalContext.current

    val pickImageLauncher = pickImageLauncher(context) { updatePhotoUri }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(text = "Edit Screen")
    }

}