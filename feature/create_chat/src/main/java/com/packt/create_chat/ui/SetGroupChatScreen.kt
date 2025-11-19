package com.packt.create_chat.ui

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import com.packt.ui.avatar.Avatar
import com.packt.ui.composables.ProfileToolBar
import com.packt.ui.photo.pickImageLauncher
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import com.packt.chat.feature.create_chat.R
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import com.packt.ui.ext.truncate

@Composable
fun SetGroupChatScreen(
    openAndPopUp: (String, String) -> Unit,
    popUp: () -> Unit,
    viewModel: CreateConversationViewModel = hiltViewModel()
){
    val isSaving by viewModel.isSaving.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val chatMetadata by viewModel.uiStateChatMetadata.collectAsState()
    val currentUserId = viewModel.currentUserId

    Log.d("SetGroupChatScreen", "Participants loco: $participants")

    val imagePicker = pickImageLauncher(
        context = LocalContext.current,
        updatePhotoUri = { newUri ->
            viewModel.updateGroupPhotoUrl(newUri)
        },
        errorCropping = R.string.error_cropping ,
        errorSaving = R.string.error_saving
    )
    SetGroupChatContent(
        updatePhoto = {
            imagePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        backClick = {
            popUp()
        },
        isSaving = isSaving,
        updateNameGroup = viewModel::updateGroupName,
        participants = participants,
        chatMetadata = chatMetadata,
        createGroup = {
            viewModel.createGroup(openAndPopUp)
        },
        currentUserId = currentUserId
    )
}

@Composable
fun SetGroupChatContent(
    updatePhoto: () -> Unit,
    backClick: () -> Unit,
    isSaving: Boolean,
    updateNameGroup: (String) -> Unit,
    participants: List<UserData>,
    chatMetadata: ChatMetadata,
    createGroup: () -> Unit,
    currentUserId: String
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.new_group,
                backClick = backClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = createGroup
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward),
                    contentDescription = "create group"
                )
            }
        },
        content = { innerPadding ->
            SettingsGroup(
                updatePhoto = updatePhoto,
                isSaving = isSaving,
                updateNameGroup = updateNameGroup,
                participants = participants,
                chatMetadata = chatMetadata,
                currentUserId = currentUserId,
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
    )
}

@Composable
fun SettingsGroup(
    updatePhoto: () -> Unit,
    isSaving: Boolean,
    updateNameGroup: (String) -> Unit,
    participants: List<UserData>,
    chatMetadata: ChatMetadata,
    currentUserId: String,
    modifier: Modifier = Modifier,
){
    val listState = rememberLazyGridState()
    LaunchedEffect(participants.size) {
        if(participants.isNotEmpty()){
            launch{
                listState.animateScrollToItem(0)
            }
        }
    }
    if(!isSaving){
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Left
            ){
                IconButton(
                    onClick = {updatePhoto()}
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(color = Color(0xFF44444E)),
                        contentAlignment = Alignment.Center
                    ){
                        Avatar(
                            photoUri = chatMetadata.groupPhotoUrl?: R.drawable.camera_white,
                            size = if(chatMetadata.groupPhotoUrl == null) 24.dp else 42.dp,
                            contentDescription = "group photo"
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                NameContentGroup(chatMetadata = chatMetadata) { updateNameGroup(it) }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Text(
                text = stringResource(R.string.members_group, participants.size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 90.dp),
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(participants) { user ->
                    val firstName = if(user.uid == currentUserId){
                        "(${stringResource(R.string.you)})"
                    } else{
                        user.name?.truncate(10)?:user.number
                    }

                    Column(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Avatar(
                            photoUri = user.photoUrl,
                            size = 50.dp,
                            contentDescription = "${user.name}'s avatar"
                        )
                        Text(
                            text = firstName,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    } else {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun NameContentGroup(
    chatMetadata: ChatMetadata,
    updateNameGroup: (String) -> Unit,
){
    val maxLength = 25
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    val currentName = chatMetadata.groupName ?: ""
    var textFieldValue by remember(currentName) {
        mutableStateOf(
            TextFieldValue(
                text = currentName,
                selection = TextRange(currentName.length)
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if(newValue.text.length <= maxLength){
                    textFieldValue = newValue
                    updateNameGroup(newValue.text)
                }
            },
            label = {Text(text = stringResource(R.string.label_name_group))},
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(text = stringResource(R.string.name_group)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        Text(
            text = "${textFieldValue.text.length}/$maxLength",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            textAlign = TextAlign.End
        )
    }
}