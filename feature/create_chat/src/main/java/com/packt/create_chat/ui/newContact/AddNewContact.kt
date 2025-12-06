package com.packt.create_chat.ui.newContact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.feature.create_chat.R
import com.packt.create_chat.ui.CreateConversationViewModel
import com.packt.ui.ext.isValidNumber
import com.packt.ui.snackbar.SnackbarManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewContact(
    openAndPopUp: (String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: CreateConversationViewModel = hiltViewModel()
){
    val numberContact by viewModel.numberContact.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_new_contact)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            NewContactContent(
                numberContact = numberContact,
                updateNumberContact = viewModel::updateNumberContact,
                addNewContact = { viewModel.addNewContact(it, openAndPopUp) },
                onBackClick = onBackClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}


@Composable
fun NewContactContent(
    numberContact: String,
    updateNumberContact: (String) -> Unit,
    addNewContact: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.available_only_ecuador),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = stringResource(R.string.number_contact),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(4.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = numberContact,
                onValueChange = {
                    updateNumberContact(it)
                },
                shape = RoundedCornerShape(16.dp),
                placeholder = {Text(text = stringResource(R.string.add_number_contact))},
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                trailingIcon = {Icon(painter = painterResource(R.drawable.twotone_phone), contentDescription = null)},
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Button(
                onClick = {
                    if(numberContact.isValidNumber()){
                        addNewContact(numberContact)
                    } else {
                        SnackbarManager.showMessage(R.string.error_adding_contact)
                    }
                }
            ) {
                Text(text = stringResource(R.string.register))
            }
            OutlinedButton(
                onClick = { onBackClick()}
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
        /**
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ) */
    }
}

@Preview(showBackground = true)
@Composable
fun AddNewContactPreview(){
    MaterialTheme {
        NewContactContent(
            numberContact = "",
            updateNumberContact = {},
            addNewContact = {},
            onBackClick = {}
        )
    }
}