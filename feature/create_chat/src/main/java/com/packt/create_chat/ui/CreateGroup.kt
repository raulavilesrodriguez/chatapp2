package com.packt.create_chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.create_chat.R
import com.packt.domain.user.UserData
import com.packt.ui.composables.SearchField

@Composable
fun CreateGroup(
    openScreen: (String) -> Unit,
    popUp: () -> Unit,
    viewModel: CreateConversationViewModel = hiltViewModel()
){
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = viewModel.currentUserId

    CreateGroupContent(
        searchText = searchText,
        updateSearchText = viewModel::updateSearchText,
        searchResults = searchResults,
        isLoading = isLoading,
        onCreateClick = {participants -> viewModel.createGroup(participants, openScreen)},
        backClick = popUp,
        currentUserUid = currentUserId
    )
}

@Composable
fun CreateGroupContent(
    searchText: String,
    updateSearchText: (String) -> Unit,
    searchResults: List<UserData>,
    isLoading: Boolean,
    onCreateClick: (uid: List<String>) -> Unit,
    backClick: () -> Unit,
    currentUserUid: String
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            SearchField(
                value = searchText,
                onValueChange = updateSearchText,
                placeholder = R.string.search,
                trailingIcon = R.drawable.arrow_back,
                onTrailingIconClick = {
                    backClick()
                }
            )
        },
        content = {innerPadding ->
            if(!isLoading){
                ParticipantsList(
                    modifier = Modifier
                        .padding(innerPadding)
                        .navigationBarsPadding(), // evita superponerse con la parte inferior
                    users = searchResults,
                    onCreateClick = onCreateClick,
                    currentUserUid = currentUserUid
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.padding(innerPadding),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}

@Composable
fun ParticipantsList(
    modifier: Modifier = Modifier,
    users: List<UserData>,
    onCreateClick: (uid: List<String>) -> Unit,
    currentUserUid: String
){
    Column(

    ) {
        UserList(
            users = users,
            onUserClick = { },
            currentUserUid = currentUserUid
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateGroupPreview(){
    CreateGroupContent(
        searchText = "",
        updateSearchText = {},
        searchResults = generateFakeUsers(),
        isLoading = true,
        onCreateClick = {},
        backClick = {},
        currentUserUid = ""
    )
}

private fun generateFakeUsers(): List<UserData> = listOf(
    UserData(
        uid = "1",
        name = "Linda Pechugas",
        nameLowercase = "linda pechugas",
        number = "0962390120",
        photoUrl = "https://i.pravatar.cc/150?u=1"
    ),
    UserData(
        uid = "2",
        name = "John Muelas",
        nameLowercase = "john muelas",
        number = "0982390121",
        photoUrl = "https://i.pravatar.cc/150?u=2"
    ),
    UserData(
        uid = "3",
        name = "Patricia Alomoto",
        nameLowercase = "patricia alomoto",
        number = "0972390122",
        photoUrl = "https://i.pravatar.cc/150?u=3"
    ),
    UserData(
        uid = "4",
        name = "Josefa Mendoza",
        nameLowercase = "josefa mendoza",
        number = "0952390123",
        photoUrl = "https://i.pravatar.cc/150?u=4"
    )
)