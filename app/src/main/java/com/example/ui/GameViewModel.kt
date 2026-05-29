package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.DatabaseProvider
import com.example.database.GameRepository
import com.example.database.UserProfile
import com.example.engine.AudioSynthesizer
import com.example.engine.GameData
import com.example.engine.GameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val repository = GameRepository(database.gameDao())

    // 1. Database state flows
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val characterUnlocks = repository.characterUnlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chappalUnlocks = repository.chappalUnlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emoteUnlocks = repository.emoteUnlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Active Screen State
    var currentScreen by mutableStateOf("SPLASH")
    var selectedBattleMode by mutableStateOf("ffa") // ffa, team, mom_kids
    var selectedMapId by mutableStateOf("wedding") // wedding, rooftop, etc

    // 3. Match Instance State
    val gameEngine = GameEngine()
    var joystickX by mutableStateOf(0f)
    var actionIntent by mutableStateOf<String?>(null)

    var currentSpecialAnnouncement by mutableStateOf("")
    var specialAnnouncementTimer by mutableStateOf(0f)

    var activeScore by mutableStateOf(0)
    var isMatchFinished by mutableStateOf(false)
    var matchWinnerName by mutableStateOf("")
    var matchWinnerEmoji by mutableStateOf("")
    var matchCoinsReward by mutableStateOf(0)

    // Daily reward scratching visual state
    var dailyRewardFeedback by mutableStateOf("")
    var dailyRewardCooldownActive by mutableStateOf(false)

    // Game-loop scheduler Job
    private var gameLoopJob: Job? = null

    init {
        viewModelScope.launch {
            repository.checkAndInitializeDefaults()
            delay(1500) // Splash delay
            currentScreen = "MAIN_MENU"
        }

        // Bind auditory highlights
        gameEngine.onSlipperHit = {
            AudioSynthesizer.playHitSound()
        }
        gameEngine.onSpecialMoveTriggered = { characterName, powerName ->
            currentSpecialAnnouncement = "🔥 $characterName triggered: $powerName! 🔥"
            specialAnnouncementTimer = 3.5f
            AudioSynthesizer.playScreamSound()
        }
    }

    fun startMatch() {
        val currentProfile = userProfile.value ?: UserProfile()
        gameEngine.setupMatch(
            selectedCharacterId = currentProfile.selectedCharacterId,
            selectedWeaponId = currentProfile.selectedSlipperId,
            mode = selectedBattleMode,
            mapId = selectedMapId
        )

        isMatchFinished = false
        matchWinnerName = ""
        matchWinnerEmoji = ""
        matchCoinsReward = 0
        activeScore = 0
        joystickX = 0f
        actionIntent = null

        currentScreen = "GAME_PLAY"

        // Cancel previous match thread if any
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (currentScreen == "GAME_PLAY") {
                // Game tick calculations
                gameEngine.tick(joystickX, actionIntent)
                actionIntent = null // single frame consumption

                // Monitor special notifications
                if (specialAnnouncementTimer > 0f) {
                    specialAnnouncementTimer -= 0.025f
                    if (specialAnnouncementTimer <= 0f) {
                        currentSpecialAnnouncement = ""
                    }
                }

                // Update scores
                val mainPlayer = gameEngine.players.firstOrNull { it.id == "player" }
                activeScore = mainPlayer?.score ?: 0

                // Check end game states
                val alivePlayers = gameEngine.players.filter { !it.isDead }
                val isPlayerDead = mainPlayer?.isDead ?: true

                if (alivePlayers.size <= 1 || isPlayerDead) {
                    isMatchFinished = true
                    val winner = alivePlayers.firstOrNull()

                    if (winner != null && winner.id == "player") {
                        matchWinnerName = "VICTORY! YOU SLAPPED THEM ALL!"
                        matchWinnerEmoji = winner.emoji
                        // Base bonus + score scale bonus
                        val bonus = 120 + (activeScore / 3).coerceAtLeast(0)
                        matchCoinsReward = bonus
                        repository.updateCoins((userProfile.value?.coins ?: 0) + bonus)
                    } else if (winner != null) {
                        matchWinnerName = "${winner.name} won the Arena!"
                        matchWinnerEmoji = winner.emoji
                        val consolation = 30 + (activeScore / 5).coerceAtLeast(0)
                        matchCoinsReward = consolation
                        repository.updateCoins((userProfile.value?.coins ?: 0) + consolation)
                    } else {
                        matchWinnerName = "DOUBLED OUT! Chaos wins!"
                        matchWinnerEmoji = "🩴"
                        val consolation = 25
                        matchCoinsReward = consolation
                        repository.updateCoins((userProfile.value?.coins ?: 0) + consolation)
                    }

                    AudioSynthesizer.playDholBeats()
                    delay(1500)
                    currentScreen = "MATCH_SUMMARY"
                    break
                }

                // 40 FPS target delay
                delay(25)
            }
        }
    }

    fun exitToMenu() {
        gameLoopJob?.cancel()
        currentScreen = "MAIN_MENU"
    }

    // Actions
    fun handleAction(action: String) {
        actionIntent = action
    }

    // Store Purchases/Unlocks
    fun attemptUnlockCharacter(charId: String, cost: Int) {
        viewModelScope.launch {
            val success = repository.unlockCharacter(charId, cost)
            if (success) {
                AudioSynthesizer.playPowerActivatedSound()
                repository.selectCharacter(charId)
            }
        }
    }

    fun attemptUnlockSlipper(slipperId: String, cost: Int) {
        viewModelScope.launch {
            val success = repository.unlockChappal(slipperId, cost)
            if (success) {
                AudioSynthesizer.playPowerActivatedSound()
                repository.selectSlipper(slipperId)
            }
        }
    }

    fun selectCharacter(charId: String) {
        viewModelScope.launch {
            repository.selectCharacter(charId)
        }
    }

    fun selectSlipper(slipperId: String) {
        viewModelScope.launch {
            repository.selectSlipper(slipperId)
        }
    }

    // Upgrades
    fun upgradePower() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val cost = repository.getUpgradeCost(profile.powerLevel)
            if (profile.coins >= cost) {
                repository.upgradePower()
                AudioSynthesizer.playPowerActivatedSound()
            }
        }
    }

    fun upgradeSpeed() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val cost = repository.getUpgradeCost(profile.speedLevel)
            if (profile.coins >= cost) {
                repository.upgradeSpeed()
                AudioSynthesizer.playPowerActivatedSound()
            }
        }
    }

    fun upgradeHealth() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val cost = repository.getUpgradeCost(profile.healthLevel)
            if (profile.coins >= cost) {
                repository.upgradeHealth()
                AudioSynthesizer.playPowerActivatedSound()
            }
        }
    }

    // Claim Fun Daily Reward
    fun claimDailyReward() {
        viewModelScope.launch {
            dailyRewardCooldownActive = true
            val randomReward = listOf(150, 420, 250, 690, 500).random()
            val success = repository.claimDailyReward(randomReward)
            if (success) {
                dailyRewardFeedback = "🎉 Mummy blessed you with $randomReward Coins!"
                AudioSynthesizer.playPowerActivatedSound()
            } else {
                dailyRewardFeedback = "❌ Mummy is angry! Wait 10 seconds before asking again!"
                AudioSynthesizer.playScreamSound()
            }
            delay(3500)
            dailyRewardFeedback = ""
            dailyRewardCooldownActive = false
        }
    }

    fun getUpgradeCost(level: Int): Int {
        return repository.getUpgradeCost(level)
    }
}
