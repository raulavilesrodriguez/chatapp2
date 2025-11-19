package com.packt.chat.ui.navigation

import android.Manifest
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.packt.ui.composables.PermissionDialog
import com.packt.ui.composables.RationaleDialog
import com.packt.ui.navigation.AppState
import com.packt.ui.snackbar.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.packt.chat.ui.ChatScreen
import com.packt.chat.ui.theme.ChatTheme
import com.packt.conversations.ui.ConversationsListScreen
import com.packt.create_chat.ui.CreateConversationScreen
import com.packt.create_chat.ui.CreateGroup
import com.packt.create_chat.ui.SetGroupChatScreen
import com.packt.settings.ui.LoginScreen
import com.packt.settings.ui.SettingsScreen
import com.packt.settings.ui.SplashScreen
import com.packt.settings.ui.edit.EditNameScreen
import com.packt.settings.ui.edit.EditScreen
import com.packt.ui.navigation.DeepLinks
import com.packt.ui.navigation.NavRoutes
import androidx.navigation.navigation
import com.packt.create_chat.ui.CreateConversationViewModel

@Composable
fun MainNavigation(){
    ChatTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val appState = rememberAppState()

            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = appState.snackbarHostState,
                        modifier = Modifier.padding(32.dp),
                        snackbar = { snackbarData ->
                            Snackbar(
                                snackbarData,
                                contentColor = MaterialTheme.colorScheme.onSurface, //text color
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh //background color
                            )
                        }
                    )
                },
            ) { innerPadding ->
                NavHost(
                    navController = appState.navController,
                    startDestination = NavRoutes.Splash,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    addSplash(appState)
                    addLogin(appState)
                    addSettings(appState)
                    addConversationsList(appState)
                    addNewConversation(appState)
                    addChat(appState)
                    addCreateGroupGraph(appState)
                    editUser(appState)
                    editNameUser(appState)
                }
            }

        }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionDialog() {
    val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (!permissionState.status.isGranted) {
        if (permissionState.status.shouldShowRationale) RationaleDialog()
        else PermissionDialog { permissionState.launchPermissionRequest() }
    }

}

@Composable
fun rememberAppState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(snackbarHostState, navController, snackbarManager, resources,coroutineScope){
        AppState(snackbarHostState, navController, snackbarManager, resources, coroutineScope)
    }


@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalResources.current
}

private fun NavGraphBuilder.addSplash(appState: AppState){
    composable(NavRoutes.Splash) {
        SplashScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addLogin(appState: AppState){
    composable(NavRoutes.Login) {
        LoginScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addSettings(appState: AppState){
    composable(NavRoutes.Settings) {
        SettingsScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addConversationsList(appState: AppState){
    composable(NavRoutes.ConversationsList) {
        ConversationsListScreen(
            openScreen = {route -> appState.navigate(route) }
        )
    }
}

private fun NavGraphBuilder.addNewConversation(appState: AppState){
    composable(NavRoutes.NewConversation) {
        CreateConversationScreen(
            openScreen = {route -> appState.navigate(route) },
            popUp = { appState.popUp() }
        )
    }
}

private fun NavGraphBuilder.addChat(appState: AppState){
    composable(
        route = NavRoutes.Chat,
        arguments = listOf(navArgument(NavRoutes.ChatArgs.ChatId) { type = NavType.StringType }),
        deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.chatRoute })
    ) { backStackEntry ->
        val chatId = backStackEntry.arguments?.getString(NavRoutes.ChatArgs.ChatId)
        ChatScreen(chatId = chatId, onBackClick = { appState.popUp() })
    }
}

private fun NavGraphBuilder.editUser(appState: AppState){
    composable(NavRoutes.EditUser) {
        EditScreen(
            openScreen = {route -> appState.navigate(route) },
            popUp = { appState.popUp() }
        )
    }
}

private fun NavGraphBuilder.editNameUser(appState: AppState){
    composable(NavRoutes.EditName) {
        EditNameScreen(
            openScreen = {route, popUp -> appState.navigateAndPopUp(route, popUp) },
            popUp = { appState.popUp() }
        )
    }
}

private fun NavGraphBuilder.addCreateGroupGraph(appState: AppState){
    navigation(
        startDestination = NavRoutes.GroupChat,
        route = NavRoutes.CreateGroupGraph
    ){
        composable(NavRoutes.GroupChat) { backStackEntry ->
            val viewModel: CreateConversationViewModel = hiltViewModel(
                backStackEntry.findStartDestination(appState.navController)
            )
            CreateGroup(
                openScreen = { route -> appState.navigate(route) },
                viewModel = viewModel
            )
        }
        composable(NavRoutes.SetGroupChat) { backStackEntry ->
            val viewModel: CreateConversationViewModel = hiltViewModel(
                backStackEntry.findStartDestination(appState.navController)
            )
            SetGroupChatScreen(
                openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)},
                popUp = { appState.popUp() },
                viewModel = viewModel
            )
        }
    }
}

private fun NavBackStackEntry.findStartDestination(navController: NavController): NavBackStackEntry {
    return this.destination.parent?.let { parent ->
        navController.getBackStackEntry(parent.route!!)
    } ?: this
}