package com.packt.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.packt.chat.feature.settings.R
import com.packt.domain.user.UserData


@Composable
fun SplashScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.alreadyLoggedIn(openAndPopUp)
        viewModel.getUserData()
    }

    val userData by viewModel.userDataStore.collectAsState()
    SplashScreenContent(
        userData = userData
    )
}

@Composable
fun SplashScreenContent(
    modifier: Modifier = Modifier,
    userData: UserData?
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5EFE6)),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = if (userData?.name != null)
                    stringResource(R.string.welcome_name, userData.name!!)
                else stringResource(R.string.welcome),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
            )
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )
            //Image(painter = painterResource(id = R.drawable.logo), contentDescription = "App Logo")
        }
    }
}