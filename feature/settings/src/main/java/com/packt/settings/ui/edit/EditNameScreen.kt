package com.packt.settings.ui.edit

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.domain.user.UserData
import com.packt.settings.R
import com.packt.ui.composables.ProfileToolBar
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun EditNameScreen(
    openScreen: (String, String) -> Unit,
    popUp: () -> Unit,
    viewModel: EditViewModel = hiltViewModel()
){
    val userData by viewModel.uiState.collectAsState()
    EditNameScreenContent(
        userData = userData,
        updateName = viewModel::updateName,
        onSaveNameClick = { viewModel.onSaveNameClick(openScreen)},
        backClick = popUp
    )
}

@Composable
fun EditNameScreenContent(
    userData: UserData?,
    updateName: (String) -> Unit,
    onSaveNameClick: () -> Unit,
    backClick: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.user_name,
                backClick = backClick
            )
        },
        content = { innerPadding ->
            NameContent(
                userData = userData,
                updateName = updateName,
                onSaveNameClick = onSaveNameClick,
                modifier = Modifier
                    .padding(innerPadding)
                    .navigationBarsPadding(),
            )
        }
    )
}

@Composable
fun NameContent(
    userData: UserData?,
    updateName: (String) -> Unit,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        if (isLandscape){
           LandscapeNameContent(
               userData = userData,
               updateName = updateName,
               onSaveNameClick = onSaveNameClick,
               maxLength = maxLength,
               focusRequester = focusRequester
           )
        } else {
            PortraitNameContent(
                userData = userData,
                updateName = updateName,
                onSaveNameClick = onSaveNameClick,
                maxLength = maxLength,
                focusRequester = focusRequester
            )
        }

    }
}

@Composable
private fun PortraitNameContent(
    userData: UserData?,
    updateName: (String) -> Unit,
    onSaveNameClick: () -> Unit,
    maxLength: Int,
    focusRequester: FocusRequester
){
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.End
        ) {
            NameTextField(userData, updateName, maxLength, focusRequester)
            Text(
                text= stringResource(R.string.comment_save),
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
            isNameValid = userData?.name?.isNotBlank() == true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun LandscapeNameContent(
    userData: UserData?,
    updateName: (String) -> Unit,
    onSaveNameClick: () -> Unit,
    maxLength: Int,
    focusRequester: FocusRequester
){
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                NameTextField(userData, updateName, maxLength, focusRequester, Modifier.weight(0.7f))
                Spacer(modifier = Modifier.width(16.dp))
                SaveButton(
                    onClick = onSaveNameClick,
                    isNameValid = userData?.name?.isNotBlank() == true,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Text(
                text= stringResource(R.string.comment_save),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun NameTextField(
    userData: UserData?,
    updateName: (String) -> Unit,
    maxLength: Int,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
){
    val currentName = userData?.name ?: ""
    var textFieldValue by remember(currentName) {
        mutableStateOf(
            TextFieldValue(
                text = currentName,
                selection = TextRange(currentName.length)
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
                    updateName(newValue.text)
                }
            },
            label = {Text(text = stringResource(R.string.label_name))},
            shape = RoundedCornerShape(16.dp),
            placeholder = {Text(text = stringResource(R.string.placeholder_name))},
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

@Preview(showBackground = true)
@Composable
fun EditNameScreenContentPreview(){
    MaterialTheme {
        val user1 = UserData(
            name = "Carla Becerra ❤\uFE0F\u200D\uD83E\uDE79",
            photoUrl = "https://i.pravatar.cc/150?u=1",
            number = "0968804849",
            uid = "1y"
        )
        EditNameScreenContent(
            userData = user1,
            updateName = {},
            onSaveNameClick = {},
            backClick = {}
        )
    }
}