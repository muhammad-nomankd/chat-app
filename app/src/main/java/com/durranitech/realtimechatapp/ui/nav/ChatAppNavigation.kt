package com.durranitech.realtimechatapp.ui.nav

import AuthRepository
import ChatListViewModel
import ChatRepository
import ChatScreen
import ChatViewModel
import HomeScreen
import UserSearchScreen
import UserSearchViewModel
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.durranitech.realtimechatapp.data.repository.UserRepository
import com.durranitech.realtimechatapp.ui.auth.AuthenticationScreen
import com.durranitech.realtimechatapp.ui.profile.ProfileScreen
import com.durranitech.realtimechatapp.ui.splash.AppSplashScreen
import com.durranitech.realtimechatapp.viewModel.AuthViewModel
import com.durranitech.realtimechatapp.viewModel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatAppNavigation() {
    val navController = rememberNavController()
    val authRepository =
        remember { AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) }
    val chatRepository = remember { ChatRepository() }
    val authViewModel = remember { AuthViewModel(authRepository, navController) }
    val chatListViewModel = remember { ChatListViewModel(chatRepository) }
    val chatViewModel = remember { ChatViewModel(chatRepository) }
    val userSearchViewModel = remember {
        UserSearchViewModel(
            UserRepository(), ChatRepository()
        )
    }


    NavHost(navController = navController, startDestination = "splash") {
        // Splash Screen
        composable("splash") {
            AppSplashScreen(
                navController = navController, authViewModel = authViewModel
            )
        }

        // Auth Screen
        composable("auth") {
            AuthenticationScreen(authViewModel, navController)
            BackHandler {
                navController.popBackStack()
            }
        }

        // Home Screen (Chat List)
        composable("home") {
            HomeScreen(
                chatListViewModel = chatListViewModel,
                onNavigateToChat = { conversationId, userId ->
                    navController.navigate("chat/$conversationId/$userId"){
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch = {
                    navController.navigate("search") {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile"){
                        launchSingleTop = true
                    }
                },
                onSignOut = {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                })
            BackHandler {
                navController.popBackStack()
            }
        }

        // Chat Screen
        composable(
            route = "chat/{conversationId}/{userId}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType })) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            ChatScreen(
                viewModel = chatViewModel,
                conversationId = conversationId,
                otherUserId = userId,
                onBackClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                        popUpTo("home") { inclusive = true }
                    }
                })
            BackHandler {
                navController.popBackStack()
            }
        }

        // User Search Screen composable
        composable("search") {
            UserSearchScreen(
                viewModel = userSearchViewModel,
                onUserSelected = { conversationId, userId ->
                    navController.navigate("chat/$conversationId/$userId")
                },
                onBackClick = {
                    navController.popBackStack()
                })

            // Handle system back button for Search screen
            BackHandler {
                navController.popBackStack()
            }
        }

        Log.d("Navigation", "About to navigate to profile screen")
        composable("profile") {
            Log.d("Navigation", "Inside profile composable")
            ProfileScreen(
                profileViewModel = ProfileViewModel(
                    UserRepository(),
                    AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                )

                ,
                onNavigateBack = {
                    Log.d("Navigation", "Back navigation called")
                    navController.popBackStack()
                },
                onSignOut = {
                    Log.d("Navigation", "Sign out called")
                    ProfileViewModel(UserRepository(),AuthRepository(FirebaseAuth.getInstance(),FirebaseFirestore.getInstance())).signOut()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )

            BackHandler {
                navController.popBackStack()
            }
        }


    }

}