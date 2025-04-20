@file:Suppress("DEPRECATION")

package com.durranitech.realtimechatapp.ui.auth

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.durranitech.realtimechatapp.viewModel.AuthUiState
import com.durranitech.realtimechatapp.viewModel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(viewModel: AuthViewModel, navController: NavHostController) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val coroutinesScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.observeAsState()
    val isLoading = rememberSaveable { mutableStateOf(false) }
    var snackBarHost by remember { mutableStateOf(SnackbarHostState()) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ){
            focusManager.clearFocus()
        }){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(32.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Text(
            text = "Sign In",
            fontSize = (18.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            shape = RoundedCornerShape(18.dp),
            label = { Text(text = "email", fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "email") },
            modifier = Modifier.fillMaxWidth().focusRequester(emailFocusRequester)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            label = { Text(text = "password", fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done), keyboardActions = KeyboardActions(
                onNext = {passwordFocusRequester.requestFocus()},
                onDone = {
                    focusManager.clearFocus()
                    coroutinesScope.launch {
                        if (!isNetworkAvailable(context)) {
                            snackBarHost.showSnackbar(
                                message = "No Internet Connection",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        if (email.isEmpty()) {
                            snackBarHost.showSnackbar(
                                message = "Email cannot be empty",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            snackBarHost.showSnackbar(
                                message = "Invalid Email",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        if (password.isEmpty()) {
                            snackBarHost.showSnackbar(
                                message = "Password cannot be empty",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        if (password.length < 6 || password.isEmpty()) {
                            snackBarHost.showSnackbar(
                                message = "Password should be at least 6 characters",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        viewModel.authenticateUser(email, password, context)
                    }
                }
            ),

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "email icon",
                    tint = Color.DarkGray
                )
            },
            modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot Password.",
                color = Color.DarkGray,
                textAlign = TextAlign.End,
                modifier = Modifier.clickable {},
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                coroutinesScope.launch {
                    focusManager.clearFocus()
                }

                coroutinesScope.launch {
                    if (!isNetworkAvailable(context)) {
                        snackBarHost.showSnackbar(
                            message = "No Internet Connection",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                        return@launch
                    }
                    if (email.isEmpty()) {
                        snackBarHost.showSnackbar(
                            message = "Email cannot be empty",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                        return@launch
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        snackBarHost.showSnackbar(
                            message = "Invalid Email",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                        return@launch
                    }
                    if (password.isEmpty()) {
                        snackBarHost.showSnackbar(
                            message = "Password cannot be empty",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                        return@launch
                    }
                    if (password.length < 6 || password.isEmpty()) {
                        snackBarHost.showSnackbar(
                            message = "Password should be at least 6 characters",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                        return@launch
                    }
                    viewModel.authenticateUser(email, password, context)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text(
                text = "Log In or Sign Up", fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(32.dp))


    }}
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(hostState = snackBarHost, modifier = Modifier.align(Alignment.BottomCenter))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                color = Color.Gray
            )
        }


    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Loading -> isLoading.value = true

            is AuthUiState.Success -> {
                coroutinesScope.launch {
                    snackBarHost.showSnackbar(
                        message = (uiState as AuthUiState.Success).message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                }
                coroutinesScope.launch {
                    delay(500)
                    navController.navigate("home"){
                        popUpTo(0){
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                    isLoading.value = false
                }
            }

            is AuthUiState.Error -> {
                coroutinesScope.launch {
                    snackBarHost.showSnackbar(
                        message = (uiState as AuthUiState.Error).message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                }
                isLoading.value = false
            }

            else -> {}
        }
    }



}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo?.isConnectedOrConnecting == true
}