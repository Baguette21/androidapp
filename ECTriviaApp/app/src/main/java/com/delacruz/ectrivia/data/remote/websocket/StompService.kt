package com.delacruz.ectrivia.data.remote.websocket

import com.delacruz.ectrivia.BuildConfig
import com.delacruz.ectrivia.data.remote.dto.GameStateDto
import com.delacruz.ectrivia.data.remote.dto.LeaderboardEntryDto
import com.delacruz.ectrivia.data.remote.dto.PlayerEventDto
import com.delacruz.ectrivia.data.remote.dto.ScoreUpdateDto
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _gameEvents = MutableSharedFlow<GameStateDto>(replay = 1, extraBufferCapacity = 1)
    val gameEvents: SharedFlow<GameStateDto> = _gameEvents

    private val _playerEvents = MutableSharedFlow<PlayerEventDto>()
    val playerEvents: SharedFlow<PlayerEventDto> = _playerEvents

    private val _leaderboardUpdates = MutableSharedFlow<List<LeaderboardEntryDto>>()
    val leaderboardUpdates: SharedFlow<List<LeaderboardEntryDto>> = _leaderboardUpdates

    private val _scoreUpdates = MutableSharedFlow<ScoreUpdateDto>()
    val scoreUpdates: SharedFlow<ScoreUpdateDto> = _scoreUpdates

    private var currentRoomCode: String? = null
    private var subscriptions = mutableSetOf<String>()

    fun connect(roomCode: String) {
        if (_connectionState.value == ConnectionState.CONNECTED && currentRoomCode == roomCode) {
            return
        }

        disconnect()
        currentRoomCode = roomCode
        _connectionState.value = ConnectionState.CONNECTING

        val request = Request.Builder()
            .url(BuildConfig.WS_URL)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                sendStompConnect()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleStompMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
                subscriptions.clear()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.ERROR
                subscriptions.clear()
            }
        })
    }

    fun disconnect() {
        subscriptions.clear()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        currentRoomCode = null
    }

    private fun sendStompConnect() {
        val frame = buildString {
            append("CONNECT\n")
            append("accept-version:1.2\n")
            append("heart-beat:10000,10000\n")
            append("\n")
            append("\u0000")
        }
        webSocket?.send(frame)
    }

    fun subscribeToRoom(roomCode: String) {
        subscribeToDestination("/topic/room/$roomCode/game")
        subscribeToDestination("/topic/room/$roomCode/players")
        subscribeToDestination("/topic/room/$roomCode/leaderboard")
        subscribeToDestination("/user/queue/score")
    }

    private fun subscribeToDestination(destination: String) {
        if (subscriptions.contains(destination)) return
        
        val subscribeId = "sub-${subscriptions.size}"
        val frame = buildString {
            append("SUBSCRIBE\n")
            append("id:$subscribeId\n")
            append("destination:$destination\n")
            append("\n")
            append("\u0000")
        }
        webSocket?.send(frame)
        subscriptions.add(destination)
    }

    private fun handleStompMessage(text: String) {
        if (text.startsWith("CONNECTED")) {
            currentRoomCode?.let { subscribeToRoom(it) }
            return
        }

        if (!text.startsWith("MESSAGE")) return

        val lines = text.split("\n")
        var destination = ""
        var bodyStartIndex = 0

        for (i in lines.indices) {
            if (lines[i].startsWith("destination:")) {
                destination = lines[i].substringAfter("destination:")
            }
            if (lines[i].isEmpty()) {
                bodyStartIndex = i + 1
                break
            }
        }

        val body = lines.drop(bodyStartIndex).joinToString("\n").trimEnd('\u0000')
        
        scope.launch {
            try {
                when {
                    destination.contains("/game") -> {
                        val event = gson.fromJson(body, GameStateDto::class.java)
                        _gameEvents.emit(event)
                    }
                    destination.contains("/players") -> {
                        val event = gson.fromJson(body, PlayerEventDto::class.java)
                        _playerEvents.emit(event)
                    }
                    destination.contains("/leaderboard") -> {
                        val entries = gson.fromJson(body, Array<LeaderboardEntryDto>::class.java).toList()
                        _leaderboardUpdates.emit(entries)
                    }
                    destination.contains("/score") -> {
                        val event = gson.fromJson(body, ScoreUpdateDto::class.java)
                        _scoreUpdates.emit(event)
                    }
                }
            } catch (e: Exception) {
                // Handle parsing errors
            }
        }
    }

    fun sendPing(roomCode: String) {
        val frame = buildString {
            append("SEND\n")
            append("destination:/app/room/$roomCode/ping\n")
            append("\n")
            append("\u0000")
        }
        webSocket?.send(frame)
    }
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}
