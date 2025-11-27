package com.packt.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.core.ui.R
import com.packt.domain.user.UserData
import com.packt.ui.composables.SearchField
import com.packt.ui.group.ParticipantsGroup
import com.packt.ui.group.RowParticipants

@Composable
fun NewParticipantsGroup(
    openAndPopUp: (String, String) -> Unit,
    onPopUp: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
){
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = viewModel.currentUserId
    val selectedParticipants by viewModel.selectedParticipants.collectAsState()

    NewParticipantsContent(
        searchText = searchText,
        updateSearchText = viewModel::updateSearchText,
        searchResults = searchResults,
        isLoading = isLoading,
        onCreateClick = {viewModel.addNewParticipants(openAndPopUp)},
        currentUserUid = currentUserId,
        selectedParticipants = selectedParticipants,
        onAddParticipant = viewModel::addParticipant,
        onRemoveParticipant = viewModel::removeParticipant,
        onBackClick = {
            viewModel.clearParticipants()
            onPopUp()
        }
    )
}

@Composable
fun NewParticipantsContent(
    searchText: String,
    updateSearchText: (String) -> Unit,
    searchResults: List<UserData>,
    isLoading: Boolean,
    onCreateClick: () -> Unit,
    currentUserUid: String,
    selectedParticipants: Set<UserData>,
    onAddParticipant: (UserData) -> Unit,
    onRemoveParticipant: (UserData) -> Unit,
    onBackClick: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            SearchField(
                value = searchText,
                onValueChange = updateSearchText,
                placeholder = R.string.search,
                trailingIcon = R.drawable.arrow_back,
                onTrailingIconClick = onBackClick
            )
        },
        floatingActionButton = {
            if(!isLoading){
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onCreateClick()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        content = {innerPadding ->
            if(!isLoading){
                ParticipantsList(
                    modifier = Modifier
                        .padding(innerPadding)
                        .navigationBarsPadding(), // evita superponerse con la parte inferior
                    users = searchResults,
                    selectedParticipants = selectedParticipants,
                    onAddParticipant = onAddParticipant,
                    onRemoveParticipant = onRemoveParticipant,
                    currentUserUid = currentUserUid
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    )
}

@Composable
fun ParticipantsList(
    modifier: Modifier = Modifier,
    users: List<UserData>,
    selectedParticipants: Set<UserData>,
    onAddParticipant: (UserData) -> Unit,
    onRemoveParticipant: (UserData) -> Unit,
    currentUserUid: String
){
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        RowParticipants(selectedParticipants) {
            onRemoveParticipant(it)
        }
        if(selectedParticipants.isNotEmpty()){
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
        Text(
            text = stringResource(R.string.contacts),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
        ParticipantsGroup(
            users = users.filter { it.uid != currentUserUid },
            onUserClick = {
                onAddParticipant(it)
            },
            selectedParticipants = selectedParticipants,
            iconChoose = R.drawable.circle
        )
    }
}

