package com.packt.create_chat.ui.newContact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.feature.create_chat.R
import com.packt.create_chat.ui.CreateConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewContact(
    openAndPopUp: (String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: CreateConversationViewModel = hiltViewModel()
){
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
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}


@Composable
fun NewContactContent(
    modifier: Modifier = Modifier
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("holaaa jijiji")
    }
}