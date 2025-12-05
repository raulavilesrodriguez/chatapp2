package com.packt.chat.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.feature.chat.R
import com.packt.chat.ui.ChatViewModel
import com.packt.domain.model.ChatMetadata
import com.packt.ui.composables.ProfileToolBar

@Composable
fun EditNameGroup(
    openScreen: (String, String) -> Unit,
    popUp: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
){
    val chatMetadata by viewModel.chatMetadata.collectAsState()

    EditNameGroupContent(
        chatMetadata = chatMetadata,
        updateNameGroup = viewModel::updateNameGroup,
        onSaveNameClick = { viewModel.onSaveNameGroup(openScreen)},
        backClick = popUp
    )
}

@Composable
fun EditNameGroupContent(
    chatMetadata: ChatMetadata?,
    updateNameGroup: (String) -> Unit,
    onSaveNameClick: () -> Unit,
    backClick: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.group_name,
            ) { backClick() }
        },
        content = { innerPadding ->
            NameContentGroup(
                chatMetadata = chatMetadata,
                updateNameGroup = updateNameGroup,
                onSaveNameClick = onSaveNameClick,
                modifier = Modifier
                    .padding(innerPadding)
                    //.navigationBarsPadding()
            )
        }
    )
}

@Composable
private fun NameContentGroup(
    chatMetadata: ChatMetadata?,
    updateNameGroup: (String) -> Unit,
    onSaveNameClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val maxLength = 25
    //local configuration of mobile device
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    if (isLandscape){
        LandscapeNameContent(
            chatMetadata = chatMetadata,
            updateNameGroup = updateNameGroup,
            onSaveNameClick = onSaveNameClick,
            maxLength = maxLength,
            focusRequester = focusRequester,
            modifier = modifier
        )
    } else {
        PortraitNameContent(
            chatMetadata = chatMetadata,
            updateNameGroup = updateNameGroup,
            onSaveNameClick = onSaveNameClick,
            maxLength = maxLength,
            focusRequester = focusRequester,
            modifier = modifier
        )
    }
}

@Composable
private fun PortraitNameContent(
    chatMetadata: ChatMetadata?,
    updateNameGroup: (String) -> Unit,
    onSaveNameClick: () -> Unit,
    maxLength: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
){
    Box(modifier = modifier
        .fillMaxSize()
        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
    ){
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.End
        ) {
            NameTextFieldGroup(chatMetadata, updateNameGroup, maxLength, focusRequester)
            Text(
                text= stringResource(R.string.comment_save_group),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Start
            )
        }
        SaveButton(
            onClick = onSaveNameClick,
            isNameValid = chatMetadata?.groupName?.isNotBlank() == true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun LandscapeNameContent(
    chatMetadata: ChatMetadata?,
    updateNameGroup: (String) -> Unit,
    onSaveNameClick: () -> Unit,
    maxLength: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 0.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            NameTextFieldGroup(chatMetadata, updateNameGroup, maxLength, focusRequester, Modifier.weight(0.7f))
            Spacer(modifier = Modifier.width(16.dp))
            SaveButton(
                onClick = onSaveNameClick,
                isNameValid = chatMetadata?.groupName?.isNotBlank() == true,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Text(
            text= stringResource(R.string.comment_save_group),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun NameTextFieldGroup(
    chatMetadata: ChatMetadata?,
    updateNameGroup: (String) -> Unit,
    maxLength: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
){
    val currentNameGroup = chatMetadata?.groupName?: ""
    var textFieldValue by remember(currentNameGroup) {
        mutableStateOf(
            TextFieldValue(
                text = currentNameGroup,
                selection = TextRange(currentNameGroup.length)
            )
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {newValue ->
                if(newValue.text.length <= maxLength){
                    textFieldValue = newValue
                    updateNameGroup(newValue.text)
                }
            },
            label = {Text(text = stringResource(R.string.label_name_group))},
            shape = RoundedCornerShape(16.dp),
            placeholder = {Text(text = stringResource(R.string.placeholder_name_group))},
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


@Composable
private fun SaveButton(
    onClick: () -> Unit,
    isNameValid: Boolean,
    modifier: Modifier = Modifier
){
    Button(
        onClick = onClick,
        enabled = isNameValid,
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
    ) {
        Text(
            text = stringResource(R.string.save),
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}