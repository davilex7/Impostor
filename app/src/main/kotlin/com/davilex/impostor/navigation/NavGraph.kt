package com.davilex.impostor.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davilex.impostor.ui.categories.CategorySelectScreen
import com.davilex.impostor.ui.game.DebateScreen
import com.davilex.impostor.ui.game.GameStartScreen
import com.davilex.impostor.ui.home.HomeScreen
import com.davilex.impostor.ui.result.ResultScreen
import com.davilex.impostor.ui.reveal.RoleRevealScreen
import com.davilex.impostor.ui.setup.SetupScreen
import com.davilex.impostor.ui.voting.VotingScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(280),
                initialOffsetX = { it / 6 }
            ) + fadeIn(animationSpec = tween(280))
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { -it / 6 }
            ) + fadeOut(animationSpec = tween(220))
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(280),
                initialOffsetX = { -it / 6 }
            ) + fadeIn(animationSpec = tween(280))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { it / 6 }
            ) + fadeOut(animationSpec = tween(220))
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlay = {
                    navController.navigate(Routes.SETUP)
                }
            )
        }
        composable(Routes.SETUP) {
            SetupScreen(
                onBack = { navController.popBackStack() },
                onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                onStartGame = {
                    navController.navigate(Routes.ROLE_REVEAL) {
                        popUpTo(Routes.SETUP) { inclusive = false }
                    }
                }
            )
        }
        composable(Routes.CATEGORIES) {
            CategorySelectScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ROLE_REVEAL) {
            RoleRevealScreen(
                onAbort = {
                    navController.popBackStack(Routes.SETUP, inclusive = false)
                },
                onAllRolesRevealed = {
                    navController.navigate(Routes.GAME_START) {
                        popUpTo(Routes.ROLE_REVEAL) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.GAME_START) {
            GameStartScreen(
                onContinue = {
                    navController.navigate(Routes.DEBATE) {
                        popUpTo(Routes.GAME_START) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DEBATE) {
            DebateScreen(
                onVote = { navController.navigate(Routes.VOTING) }
            )
        }
        composable(Routes.VOTING) {
            VotingScreen(
                onBackToDebate = { navController.popBackStack() },
                onConfirm = {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.DEBATE) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                onNewGame = {
                    navController.navigate(Routes.ROLE_REVEAL) {
                        popUpTo(Routes.SETUP) { inclusive = false }
                    }
                },
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
