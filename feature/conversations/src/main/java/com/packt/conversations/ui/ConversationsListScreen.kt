package com.packt.conversations.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.packt.conversations.ui.model.Conversation
import com.packt.ui.R

@Composable
fun ConversationsListScreen(
    openScreen: (String) -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel()
){
    ConversationsListScreenContent(
        onNewConversationClick = viewModel::onNewConversationClick,
        onConversationClick = viewModel::onConversationClick,
        openScreen = openScreen
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreenContent(
    onNewConversationClick: ((String) -> Unit) -> Unit,
    onConversationClick: ((String)-> Unit, chatId: String) -> Unit,
    openScreen: (String) -> Unit
){
    val tabs = generateTabs()
    val selectedIndex = remember { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(1){tabs.size}

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.conversations_list_title))
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            TabRow(
                selectedTabIndex = selectedIndex.value,
                modifier = Modifier.navigationBarsPadding()  //para que no se ponga encima de la parte inferior del movil
            ) {
                tabs.forEachIndexed { index, _ ->
                    Tab(
                        text = {Text(stringResource(tabs[index].title))},
                        selected = index == selectedIndex.value,
                        onClick = {
                            selectedIndex.value = index
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { onNewConversationClick(openScreen)}
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        },
        content = { innerPadding ->
            HorizontalPager(
                modifier = Modifier.padding(innerPadding),
                state = pagerState
            ) { index ->
                when (index) {
                    0 -> {
                        //Status
                    }
                    1 -> {
                        ConversationList(
                            conversations = generateFakeConversations(),
                            onConversationClick = { chatId -> onConversationClick(openScreen, chatId)}
                        )
                    }
                    2 -> {
                        //Status
                    }
                }
            }
            LaunchedEffect(selectedIndex.value) {
                pagerState.animateScrollToPage(selectedIndex.value)
            }
        },
    )
}

data class ConversationsListTab(
    @param:StringRes val title: Int,
)

fun generateTabs(): List<ConversationsListTab> {
    return listOf(
        ConversationsListTab(
            title = R.string.conversations_tab_status_title,
        ),
        ConversationsListTab(
            title = R.string.conversations_tab_chats_title,
        ),
        ConversationsListTab(
            title = R.string.conversations_tab_calls_title,
        ),
    )
}

fun generateFakeConversations(): List<Conversation> {
    return listOf(
        Conversation(
            id = "1",
            name = "John Doe",
            message = "Hey, how are you?",
            timestamp = "10:30",
            avatar = "https://i.pravatar.cc/150?u=1",
            unreadCount = 2
        ),
        Conversation(
            id = "2",
            name = "Jane Smith",
            message = "Looking forward to the party!",
            timestamp = "11:15",
            avatar = "https://i.pravatar.cc/150?u=2"
        ),
        // Add more conversations here
        Conversation(
            id = "3",
            name = "Michael Johnson",
            message = "Did you finish the project?",
            timestamp = "9:45",
            avatar = "https://i.pravatar.cc/150?u=3",
            unreadCount = 3
        ),
        Conversation(
            id = "4",
            name = "Emma Brown",
            message = "Great job on the presentation!",
            timestamp = "12:20",
            avatar = "https://i.pravatar.cc/150?u=4"
        ),
        Conversation(
            id = "5",
            name = "Lucas Smith",
            message = "See you at the game later.",
            timestamp = "14:10",
            avatar = "https://i.pravatar.cc/150?u=5",
            unreadCount = 1
        ),
        Conversation(
            id = "6",
            name = "Sophia Johnson",
            message = "Let's meet for lunch tomorrow.",
            timestamp = "16:00",
            avatar = "https://i.pravatar.cc/150?u=6"
        ),
        Conversation(
            id = "7",
            name = "Olivia Brown",
            message = "Can you help me with the assignment?",
            timestamp = "18:30",
            avatar = "https://i.pravatar.cc/150?u=7",
            unreadCount = 5
        ),
        Conversation(
            id = "8",
            name = "Liam Williams",
            message = "I'll call you later.",
            timestamp = "19:15",
            avatar = "https://i.pravatar.cc/150?u=8"
        ),
        Conversation(
            id = "9",
            name = "Charlotte Johnson",
            message = "Don't forget the meeting tomorrow.",
            timestamp = "21:45",
            avatar = "https://i.pravatar.cc/150?u=9",
            unreadCount = 1
        ),
        Conversation(
            id = "10",
            name = "James Brown",
            message = "The movie was awesome!",
            timestamp = "23:00",
            avatar = "https://i.pravatar.cc/150?u=10"
        ),
        Conversation(
            id = "11",
            name = "Jake Smith",
            message = "Did you get the tickets?",
            timestamp = "23:50",
            avatar = "https://i.pravatar.cc/150?u=11"
        )
    )
}