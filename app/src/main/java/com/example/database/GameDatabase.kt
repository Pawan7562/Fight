package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 500,
    val selectedCharacterId: String = "mom",
    val selectedSlipperId: String = "hawai",
    val selectedEmoteId: String = "bhangra",
    val lastDailyRewardTime: Long = 0,
    val powerLevel: Int = 1,
    val speedLevel: Int = 1,
    val healthLevel: Int = 1
)

@Entity(tableName = "character_unlock")
data class CharacterUnlock(
    @PrimaryKey val characterId: String,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "chappal_unlock")
data class ChappalUnlock(
    @PrimaryKey val chappalId: String,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "emote_unlock")
data class EmoteUnlock(
    @PrimaryKey val emoteId: String,
    val isUnlocked: Boolean = false
)

@Dao
interface GameDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProfile(profile: UserProfile)

    @Query("SELECT * FROM character_unlock")
    fun getCharacterUnlocks(): Flow<List<CharacterUnlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterUnlock(characterUnlock: CharacterUnlock)

    @Query("SELECT * FROM chappal_unlock")
    fun getChappalUnlocks(): Flow<List<ChappalUnlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChappalUnlock(chappalUnlock: ChappalUnlock)

    @Query("SELECT * FROM emote_unlock")
    fun getEmoteUnlocks(): Flow<List<EmoteUnlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmoteUnlock(emoteUnlock: EmoteUnlock)
}

@Database(
    entities = [
        UserProfile::class,
        CharacterUnlock::class,
        ChappalUnlock::class,
        EmoteUnlock::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
