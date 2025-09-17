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
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.packt.chat.ui.theme.ChatTheme
import com.packt.conversations.ui.ConversationsListScreen
import com.packt.create_chat.ui.CreateConversationScreen
import com.packt.settings.ui.LoginScreen
import com.packt.settings.ui.SettingsScreen
import com.packt.ui.navigation.NavRoutes

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
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer, //text color
                                containerColor = Color(0xFFE0F7FA) //background color
                            )
                        }
                    )
                },
            ) { innerPadding ->
                NavHost(
                    navController = appState.navController,
                    startDestination = NavRoutes.Login,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    addLogin(appState)
                    addSettings(appState)
                    addConversationsList(appState)
                    addNewConversation(appState)
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
            openScreen = {route -> appState.navigate(route) }
        )
    }
}