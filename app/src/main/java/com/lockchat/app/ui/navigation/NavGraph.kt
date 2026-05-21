package com.lockchat.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lockchat.app.ui.screens.addcontact.AddContactScreen
import com.lockchat.app.ui.screens.chatdetail.ChatDetailScreen
import com.lockchat.app.ui.screens.chats.ChatsScreen
import com.lockchat.app.ui.screens.onboarding.OnboardingScreen
import com.lockchat.app.ui.screens.onboarding.OnboardingViewModel
import com.lockchat.app.ui.screens.ping.PingScreen
import com.lockchat.app.ui.screens.profile.ProfileScreen

// ─────────────────────────────────────────────────
// Rutas de navegación
// ─────────────────────────────────────────────────
object Routes {
    const val ONBOARDING   = "onboarding"
    const val CHATS        = "chats"
    const val CHAT_DETAIL  = "chat/{contactId}"
    const val PING         = "ping"
    const val PROFILE      = "profile"
    const val ADD_CONTACT  = "add_contact"

    fun chatDetail(contactId: String) = "chat/$contactId"
}

// ─────────────────────────────────────────────────
// NavGraph principal
// ─────────────────────────────────────────────────
@Composable
fun LockChatNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    // Flujo: si ya existe identidad → Chats, si no → Onboarding
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val startDestination = if (onboardingVm.hasIdentity()) Routes.CHATS else Routes.ONBOARDING

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {
        // Onboarding simplificado (solo handle, sin PIN)
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onIdentityCreated = {
                    navController.navigate(Routes.CHATS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // Lista de Chats
        composable(Routes.CHATS) {
            ChatsScreen(
                onChatClick         = { contactId -> navController.navigate(Routes.chatDetail(contactId)) },
                onNavigateToPing    = { navController.navigate(Routes.PING) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToAddContact = { navController.navigate(Routes.ADD_CONTACT) }
            )
        }

        // Detalle de Chat
        composable(
            route     = Routes.CHAT_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            ChatDetailScreen(
                contactId      = contactId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Ping
        composable(Routes.PING) {
            PingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Perfil
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToAddContact = { navController.navigate(Routes.ADD_CONTACT) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Agregar contacto via QR
        composable(Routes.ADD_CONTACT) {
            AddContactScreen(
                onContactAdded = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
