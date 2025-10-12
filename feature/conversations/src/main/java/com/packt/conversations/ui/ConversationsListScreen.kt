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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.conversations.ui.model.Conversation
import com.packt.ui.R
import com.packt.ui.composables.DropdownContextMenu
import kotlinx.coroutines.launch
import kotlin.Int

@Composable
fun ConversationsListScreen(
    openScreen: (String) -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel()
){
    val conversations by viewModel.conversations.collectAsState()
    val options = ActionOptions.getOptions()

    ConversationsListScreenContent(
        onNewConversationClick = { viewModel.onNewConversationClick(openScreen)},
        onConversationClick = { chatId -> viewModel.onConversationClick(openScreen, chatId)},
        conversations = conversations,
        options = options,
        onActionClick = { action -> viewModel.onActionClick(openScreen, action)}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreenContent(
    onNewConversationClick: () -> Unit,
    onConversationClick: (chatId: String) -> Unit,
    conversations: List<Conversation>,
    options: List<Int>,
    onActionClick: (Int) -> Unit
){
    val tabs = generateTabs()
    val pagerState = rememberPagerState(1){tabs.size}

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.conversations_list_title))
                },
                actions = {
                    DropdownContextMenu(
                        options = options,
                        modifier = Modifier,
                        onActionClick = onActionClick
                    )
                }
            )
        },
        bottomBar = {
            val coroutineScope = rememberCoroutineScope()

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.navigationBarsPadding()  //para que no se ponga encima de la parte inferior del movil
            ) {
                tabs.forEachIndexed { index, _ ->
                    Tab(
                        text = {Text(stringResource(tabs[index].title))},
                        selected = index == pagerState.currentPage,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = onNewConversationClick
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
                            conversations = conversations,
                            onConversationClick = onConversationClick
                        )
                    }
                    2 -> {
                        //Status
                    }
                }
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

