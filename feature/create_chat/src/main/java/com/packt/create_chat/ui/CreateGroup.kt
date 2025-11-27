package com.packt.create_chat.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.feature.create_chat.R
import com.packt.domain.user.UserData
import com.packt.ui.composables.SearchField
import com.packt.ui.group.ParticipantsGroup
import com.packt.ui.group.RowParticipants
import com.packt.ui.navigation.NavRoutes

@Composable
fun CreateGroup(
    openScreen: (String) -> Unit,
    viewModel: CreateConversationViewModel = hiltViewModel()
){
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = viewModel.currentUserId

    val selectedParticipants by viewModel.selectedParticipants.collectAsState()

    CreateGroupContent(
        searchText = searchText,
        updateSearchText = viewModel::updateSearchText,
        searchResults = searchResults,
        isLoading = isLoading,
        onCreateClick = {viewModel.participantsGroup(openScreen)},
        currentUserUid = currentUserId,
        selectedParticipants = selectedParticipants,
        onAddParticipant = viewModel::addParticipant,
        onRemoveParticipant = viewModel::removeParticipant,
        onBackClick = {
            viewModel.clearParticipants()
            openScreen(NavRoutes.ConversationsList)
        }
    )
}

@Composable
fun CreateGroupContent(
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

@Preview(showBackground = true)
@Composable
fun CreateGroupPreview(){
    MaterialTheme {
        CreateGroupContent(
            searchText = "",
            updateSearchText = {},
            searchResults = generateFakeUsers(),
            isLoading = false,
            onCreateClick = {},
            currentUserUid = "41",
            selectedParticipants = mutableSetOf(),
            onAddParticipant = {},
            onRemoveParticipant = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantsListPreview(){
    MaterialTheme {
        ParticipantsList(
            users = generateFakeUsers(),
            selectedParticipants = mutableSetOf(),
            onAddParticipant = {},
            onRemoveParticipant = {},
            currentUserUid = "41"
        )
    }
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
    ),
    UserData(
        uid = "41",
        name = "Raul Avi",
        nameLowercase = "raul avi",
        number = "0958890125",
        photoUrl = "https://i.pravatar.cc/150?u=41"
    ),
    UserData(
        uid = "5",
        name = "Segundo Ayovi",
        nameLowercase = "segundo ayovi",
        number = "0953090123",
        photoUrl = "https://i.pravatar.cc/150?u=5"
    ),
    UserData(
        uid = "6",
        name = "Katty Lizano",
        nameLowercase = "katty lizano",
        number = "0968080148",
        photoUrl = "https://i.pravatar.cc/150?u=6"
    ),
    UserData(
        uid = "7",
        name = "Katiska Live",
        nameLowercase = "katiska live",
        number = "0985030142",
        photoUrl = "https://i.pravatar.cc/150?u=7"
    ),
    UserData(
        uid = "8",
        name = "Jorgito Guayaco",
        nameLowercase = "jorgito guayaco",
        number = "0976621196",
        photoUrl = "https://i.pravatar.cc/150?u=8"
    ),
    UserData(
        uid = "9",
        name = "Payasin",
        nameLowercase = "payasin",
        number = "0986547185",
        photoUrl = "https://i.pravatar.cc/150?u=9"
    ),
    UserData(
        uid = "11",
        name = "Sofia Caiza",
        nameLowercase = "sofia caiza",
        number = "0947858239",
        photoUrl = "https://i.pravatar.cc/150?u=11"
    ),

)