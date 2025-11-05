package com.packt.create_chat.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.create_chat.R
import com.packt.domain.user.UserData
import com.packt.ui.composables.SearchField

@Composable
fun CreateConversationScreen(
    openScreen: (String) -> Unit,
    popUp: () -> Unit,
    viewModel: CreateConversationViewModel = hiltViewModel()
){
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = viewModel.currentUserId

    CreateConversationScreenContent(
        searchText = searchText,
        updateSearchText = viewModel::updateSearchText,
        searchResults = searchResults,
        isLoading = isLoading,
        onUserClick = { uid -> viewModel.createChatRoom(uid, openScreen) },
        backClick = popUp,
        currentUserUid = currentUserId
    )
}

@Composable
fun CreateConversationScreenContent(
    searchText: String,
    updateSearchText: (String) -> Unit,
    searchResults: List<UserData>,
    isLoading: Boolean,
    onUserClick: (uid: String) -> Unit,
    backClick: () -> Unit,
    currentUserUid: String
){
    var showSearchField by remember { mutableStateOf(false) }
    var showToolBar by remember { mutableStateOf(true) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            when{
                showSearchField -> {
                    SearchField(
                        value = searchText,
                        onValueChange = updateSearchText,
                        placeholder = R.string.search,
                        trailingIcon = R.drawable.arrow_back,
                        onTrailingIconClick = {
                            showSearchField = false
                            showToolBar = true
                        }
                    )
                }
                showToolBar -> {
                    SearchToolBar(
                        title = R.string.select_contact,
                        iconBack = R.drawable.arrow_back,
                        iconBackClick = backClick,
                        iconSearch = R.drawable.baseline_search_24,
                        iconSearchClick = {
                            showSearchField = true
                            showToolBar = false
                        }
                    )
                }
            }
        },
        content = { innerPadding ->
            if(!isLoading){
                UserList(
                    modifier = Modifier
                        .padding(innerPadding)
                        .navigationBarsPadding(), // evita superponerse con la parte inferior
                    users = searchResults,
                    onUserClick = onUserClick,
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
fun SearchToolBar(
    @StringRes title: Int,
    @DrawableRes iconBack: Int,
    iconBackClick: () -> Unit,
    @DrawableRes iconSearch: Int,
    iconSearchClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize()
            .padding(top = 16.dp, start = 0.dp, end = 0.dp, bottom = 16.dp)
    ){
        IconButton(
            onClick = {iconBackClick()},
            modifier = Modifier
                .padding(start= 0.dp, end = 12.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(id = iconBack),
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.80f)
        ){
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
                )
        }
        IconButton(
            onClick = iconSearchClick,
            modifier = Modifier
                .padding(end = 4.dp)
                .align(Alignment.CenterEnd)
        ){
            Icon(
                painter = painterResource(id = iconSearch),
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchToolBarPreview(){
    MaterialTheme {
        SearchToolBar(
            title = R.string.select_contact,
            iconBack = R.drawable.arrow_back,
            iconBackClick = {},
            iconSearch = R.drawable.baseline_search_24,
            iconSearchClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchFieldPreview(){
    MaterialTheme {
        SearchField(
            value = "",
            onValueChange = {},
            placeholder = R.string.search,
            trailingIcon = R.drawable.arrow_back,
            onTrailingIconClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateConversationScreenContentPreview(){
    MaterialTheme {
        CreateConversationScreenContent(
            searchText = "",
            updateSearchText = {},
            searchResults = generateFakeUsers(),
            isLoading = false,
            onUserClick = {},
            backClick = {},
            currentUserUid = ""
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
    )
)