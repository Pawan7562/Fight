package com.example.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {

    val userProfile: Flow<UserProfile?> = gameDao.getUserProfile()
    val characterUnlocks: Flow<List<CharacterUnlock>> = gameDao.getCharacterUnlocks()
    val chappalUnlocks: Flow<List<ChappalUnlock>> = gameDao.getChappalUnlocks()
    val emoteUnlocks: Flow<List<EmoteUnlock>> = gameDao.getEmoteUnlocks()

    suspend fun getProfileSync(): UserProfile {
        return gameDao.getUserProfileSync() ?: UserProfile().also {
            gameDao.updateProfile(it)
        }
    }

    suspend fun updateCoins(coins: Int) {
        val current = getProfileSync()
        gameDao.updateProfile(current.copy(coins = coins))
    }

    suspend fun selectCharacter(characterId: String) {
        val current = getProfileSync()
        gameDao.updateProfile(current.copy(selectedCharacterId = characterId))
    }

    suspend fun selectSlipper(slipperId: String) {
        val current = getProfileSync()
        gameDao.updateProfile(current.copy(selectedSlipperId = slipperId))
    }

    suspend fun selectEmote(emoteId: String) {
        val current = getProfileSync()
        gameDao.updateProfile(current.copy(selectedEmoteId = emoteId))
    }

    suspend fun upgradePower() {
        val current = getProfileSync()
        if (current.coins >= getUpgradeCost(current.powerLevel)) {
            gameDao.updateProfile(current.copy(
                coins = current.coins - getUpgradeCost(current.powerLevel),
                powerLevel = current.powerLevel + 1
            ))
        }
    }

    suspend fun upgradeSpeed() {
        val current = getProfileSync()
        if (current.coins >= getUpgradeCost(current.speedLevel)) {
            gameDao.updateProfile(current.copy(
                coins = current.coins - getUpgradeCost(current.speedLevel),
                speedLevel = current.speedLevel + 1
            ))
        }
    }

    suspend fun upgradeHealth() {
        val current = getProfileSync()
        if (current.coins >= getUpgradeCost(current.healthLevel)) {
            gameDao.updateProfile(current.copy(
                coins = current.coins - getUpgradeCost(current.healthLevel),
                healthLevel = current.healthLevel + 1
            ))
        }
    }

    suspend fun unlockCharacter(characterId: String, cost: Int): Boolean {
        val current = getProfileSync()
        if (current.coins >= cost) {
            gameDao.updateProfile(current.copy(coins = current.coins - cost))
            gameDao.insertCharacterUnlock(CharacterUnlock(characterId, true))
            return true
        }
        return false
    }

    suspend fun unlockChappal(chappalId: String, cost: Int): Boolean {
        val current = getProfileSync()
        if (current.coins >= cost) {
            gameDao.updateProfile(current.copy(coins = current.coins - cost))
            gameDao.insertChappalUnlock(ChappalUnlock(chappalId, true))
            return true
        }
        return false
    }

    suspend fun unlockEmote(emoteId: String, cost: Int): Boolean {
        val current = getProfileSync()
        if (current.coins >= cost) {
            gameDao.updateProfile(current.copy(coins = current.coins - cost))
            gameDao.insertEmoteUnlock(EmoteUnlock(emoteId, true))
            return true
        }
        return false
    }

    suspend fun claimDailyReward(rewardAmount: Int): Boolean {
        val current = getProfileSync()
        val now = System.currentTimeMillis()
        // Allow daily reward if 24 hours (86400000 ms) passed, or for prototype/gameplay convenience, any new day (or at least 30 seconds for testability)
        // Let's make it 10 seconds for fun/testability! But keep standard logic.
        if (now - current.lastDailyRewardTime > 10000) { 
            gameDao.updateProfile(current.copy(
                coins = current.coins + rewardAmount,
                lastDailyRewardTime = now
            ))
            return true
        }
        return false
    }

    fun getUpgradeCost(level: Int): Int {
        return level * 150
    }

    suspend fun checkAndInitializeDefaults() {
        // Ensure defaults are populated
        val defaults = listOf("mom")
        for (charId in defaults) {
            gameDao.insertCharacterUnlock(CharacterUnlock(charId, true))
        }
        val defaultSlippers = listOf("hawai")
        for (slipId in defaultSlippers) {
            gameDao.insertChappalUnlock(ChappalUnlock(slipId, true))
        }
        val defaultEmotes = listOf("bhangra")
        for (emoteId in defaultEmotes) {
            gameDao.insertEmoteUnlock(EmoteUnlock(emoteId, true))
        }
    }
}
