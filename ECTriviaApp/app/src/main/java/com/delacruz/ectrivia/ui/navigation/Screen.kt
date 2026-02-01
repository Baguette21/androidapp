package com.delacruz.ectrivia.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateRoom : Screen("create_room")
    data object ThemeSelector : Screen("theme_selector/{nickname}/{timerSeconds}") {
        fun createRoute(nickname: String, timerSeconds: Int) = "theme_selector/$nickname/$timerSeconds"
    }
    data object QuestionEditor : Screen("question_editor/{roomCode}/{playerId}/{isHost}") {
        fun createRoute(roomCode: String, playerId: Long, isHost: Boolean) =
            "question_editor/$roomCode/$playerId/$isHost"
    }
    data object QuestionList : Screen("question_list/{roomCode}/{playerId}/{isHost}") {
        fun createRoute(roomCode: String, playerId: Long, isHost: Boolean) =
            "question_list/$roomCode/$playerId/$isHost"
    }
    data object JoinRoom : Screen("join_room")
    data object Nickname : Screen("nickname/{roomCode}") {
        fun createRoute(roomCode: String) = "nickname/$roomCode"
    }
    data object Lobby : Screen("lobby/{roomCode}/{playerId}/{isHost}") {
        fun createRoute(roomCode: String, playerId: Long, isHost: Boolean) =
            "lobby/$roomCode/$playerId/$isHost"
    }
    data object GamePlay : Screen("game_play/{roomCode}/{playerId}") {
        fun createRoute(roomCode: String, playerId: Long) = "game_play/$roomCode/$playerId"
    }
    data object HostSpectator : Screen("host_spectator/{roomCode}") {
        fun createRoute(roomCode: String) = "host_spectator/$roomCode"
    }
    data object QuestionResult : Screen("question_result/{roomCode}") {
        fun createRoute(roomCode: String) = "question_result/$roomCode"
    }
    data object FinalLeaderboard : Screen("final_leaderboard/{roomCode}") {
        fun createRoute(roomCode: String) = "final_leaderboard/$roomCode"
    }
}
