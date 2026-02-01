package com.delacruz.ectrivia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.delacruz.ectrivia.ui.screens.createroom.CreateRoomScreen
import com.delacruz.ectrivia.ui.screens.createroom.QuestionEditorScreen
import com.delacruz.ectrivia.ui.screens.createroom.QuestionListScreen
import com.delacruz.ectrivia.ui.screens.createroom.ThemeSelectorScreen
import com.delacruz.ectrivia.ui.screens.game.GamePlayScreen
import com.delacruz.ectrivia.ui.screens.home.HomeScreen
import com.delacruz.ectrivia.ui.screens.hostview.HostSpectatorScreen
import com.delacruz.ectrivia.ui.screens.joinroom.JoinRoomScreen
import com.delacruz.ectrivia.ui.screens.joinroom.NicknameScreen
import com.delacruz.ectrivia.ui.screens.leaderboard.FinalLeaderboardScreen
import com.delacruz.ectrivia.ui.screens.lobby.LobbyScreen
import com.delacruz.ectrivia.ui.screens.results.QuestionResultScreen

@Composable
fun ECTriviaNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onCreateRoom = { navController.navigate(Screen.CreateRoom.route) },
                onJoinRoom = { navController.navigate(Screen.JoinRoom.route) }
            )
        }

        composable(Screen.CreateRoom.route) {
            CreateRoomScreen(
                onNavigateBack = { navController.popBackStack() },
                onThemeSelected = { nickname, timerSeconds ->
                    navController.navigate(Screen.ThemeSelector.createRoute(nickname, timerSeconds))
                },
                onCustomQuestions = { roomCode, playerId, isHost ->
                    navController.navigate(Screen.QuestionList.createRoute(roomCode, playerId, isHost))
                },
                onRoomCreated = { roomCode, playerId, isHost ->
                    navController.navigate(Screen.Lobby.createRoute(roomCode, playerId, isHost)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.ThemeSelector.route,
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("timerSeconds") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
            val timerSeconds = backStackEntry.arguments?.getInt("timerSeconds") ?: 15
            
            ThemeSelectorScreen(
                nickname = nickname,
                timerSeconds = timerSeconds,
                onNavigateBack = { navController.popBackStack() },
                onCategorySelected = { roomCode, playerId, isHost ->
                    navController.navigate(Screen.Lobby.createRoute(roomCode, playerId, isHost)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.QuestionEditor.route,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.LongType },
                navArgument("isHost") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val playerId = backStackEntry.arguments?.getLong("playerId") ?: 0L
            val isHost = backStackEntry.arguments?.getBoolean("isHost") ?: false
            QuestionEditorScreen(
                roomCode = roomCode,
                onNavigateBack = { navController.popBackStack() },
                onQuestionAdded = {
                    navController.navigate(Screen.QuestionList.createRoute(roomCode, playerId, isHost))
                }
            )
        }

        composable(
            route = Screen.QuestionList.route,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.LongType },
                navArgument("isHost") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val playerId = backStackEntry.arguments?.getLong("playerId") ?: 0L
            val isHost = backStackEntry.arguments?.getBoolean("isHost") ?: false
            QuestionListScreen(
                roomCode = roomCode,
                playerId = playerId,
                isHost = isHost,
                onNavigateBack = { navController.popBackStack() },
                onAddQuestion = {
                    navController.navigate(Screen.QuestionEditor.createRoute(roomCode, playerId, isHost))
                },
                onStartGame = { startPlayerId, startIsHost ->
                    navController.navigate(Screen.Lobby.createRoute(roomCode, startPlayerId, startIsHost)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.JoinRoom.route) {
            JoinRoomScreen(
                onNavigateBack = { navController.popBackStack() },
                onRoomFound = { roomCode ->
                    navController.navigate(Screen.Nickname.createRoute(roomCode))
                }
            )
        }

        composable(
            route = Screen.Nickname.route,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            NicknameScreen(
                roomCode = roomCode,
                onNavigateBack = { navController.popBackStack() },
                onJoined = { playerId, isHost ->
                    navController.navigate(Screen.Lobby.createRoute(roomCode, playerId, isHost)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.Lobby.route,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.LongType },
                navArgument("isHost") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val playerId = backStackEntry.arguments?.getLong("playerId") ?: 0L
            val isHost = backStackEntry.arguments?.getBoolean("isHost") ?: false
            LobbyScreen(
                roomCode = roomCode,
                playerId = playerId,
                isHost = isHost,
                onNavigateBack = { navController.popBackStack() },
                onGameStart = { isThemeBased, resolvedPlayerId ->
                    if (isHost && !isThemeBased) {
                        navController.navigate(Screen.HostSpectator.createRoute(roomCode)) {
                            popUpTo(Screen.Home.route)
                        }
                    } else {
                        navController.navigate(Screen.GamePlay.createRoute(roomCode, resolvedPlayerId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onLeaveRoom = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.GamePlay.route,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val playerId = backStackEntry.arguments?.getLong("playerId") ?: 0L
            GamePlayScreen(
                roomCode = roomCode,
                playerId = playerId,
                onQuestionEnd = { navController.navigate(Screen.QuestionResult.createRoute(roomCode)) },
                onGameEnd = { navController.navigate(Screen.FinalLeaderboard.createRoute(roomCode)) {
                    popUpTo(Screen.Home.route)
                }}
            )
        }

        composable(
            route = Screen.HostSpectator.route,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            HostSpectatorScreen(
                roomCode = roomCode,
                onQuestionEnd = { navController.navigate(Screen.QuestionResult.createRoute(roomCode)) },
                onGameEnd = { navController.navigate(Screen.FinalLeaderboard.createRoute(roomCode)) {
                    popUpTo(Screen.Home.route)
                }}
            )
        }

        composable(
            route = Screen.QuestionResult.route,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            QuestionResultScreen(
                roomCode = roomCode,
                onNextQuestion = { navController.popBackStack() },
                onGameEnd = { navController.navigate(Screen.FinalLeaderboard.createRoute(roomCode)) {
                    popUpTo(Screen.Home.route)
                }}
            )
        }

        composable(
            route = Screen.FinalLeaderboard.route,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            FinalLeaderboardScreen(
                roomCode = roomCode,
                onPlayAgain = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onExit = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
