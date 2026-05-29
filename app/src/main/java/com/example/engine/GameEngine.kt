package com.example.engine

import kotlin.random.Random

data class BattleSkeletor(
    val id: String,
    val name: String,
    val emoji: String,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var health: Float = 100f,
    var maxHealth: Float = 100f,
    var speed: Float = 5f,
    var basePower: Float = 1f,
    var isBot: Boolean = true,
    var isDead: Boolean = false,
    var direction: Float = 1f, // 1 is right, -1 is left
    var headTilt: Float = 0f,
    var slapActive: Boolean = false,
    var slapProgress: Float = 0f,
    var hitStun: Int = 0,
    var specialRageTimer: Float = 0f,
    var slipTimer: Float = 0f,
    var score: Int = 0,
    val colorHex: String,
    val preferredWeaponId: String,
    var activeSpecialCooldown: Int = 0,
    var preferredRoast: String = "Aise kaise bachegi?!"
)

data class BattleProjectile(
    val id: Int,
    val typeId: String,
    val emoji: String,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float = 0f,
    val ownerId: String,
    val damage: Float,
    val knockback: Float,
    var bounces: Int = 3
)

data class BattleItem(
    val id: Int,
    val type: String, // "banana", "puddle", "pillow_bomb"
    val emoji: String,
    val x: Float,
    val y: Float
)

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val emoji: String,
    var life: Int,
    val maxLife: Int = 15
)

data class ComicPopup(
    val id: Int,
    val text: String,
    val x: Float,
    val y: Float,
    var life: Float, // 1.0 to 0.0
    val colorHex: String,
    val scale: Float = 1.0f
)

class GameEngine {
    val gameWidth = 1000f
    val gameHeight = 600f
    val gravity = 0.45f
    val friction = 0.82f

    var players = mutableListOf<BattleSkeletor>()
    var projectiles = mutableListOf<BattleProjectile>()
    var passiveItems = mutableListOf<BattleItem>()
    var particles = mutableListOf<GameParticle>()
    var popups = mutableListOf<ComicPopup>()

    private var nextProjectileId = 1
    private var nextItemId = 1
    private var nextPopupId = 1

    var onSlipperHit: (() -> Unit)? = null
    var onSpecialMoveTriggered: ((String, String) -> Unit)? = null // Character, special power name
    var isSloMoActive = false
    var sloMoTimer = 0f

    fun setupMatch(
        selectedCharacterId: String,
        selectedWeaponId: String,
        mode: String, // "ffa", "team", "mom_kids", "hostel", "wedding"
        mapId: String
    ) {
        players.clear()
        projectiles.clear()
        passiveItems.clear()
        particles.clear()
        popups.clear()

        isSloMoActive = false
        sloMoTimer = 0f

        // 1. Add Player
        val playerChar = GameData.characters.firstOrNull { it.id == selectedCharacterId } ?: GameData.characters[0]
        val activeWeapon = GameData.weapons.firstOrNull { it.id == selectedWeaponId } ?: GameData.weapons[0]

        val mainPlayer = BattleSkeletor(
            id = "player",
            name = playerChar.name,
            emoji = playerChar.emoji,
            x = 150f,
            y = gameHeight - 120f,
            isBot = false,
            health = playerChar.baseHealth,
            maxHealth = playerChar.baseHealth,
            speed = playerChar.baseSpeed * 6f,
            basePower = playerChar.basePower,
            colorHex = playerChar.colorHex,
            preferredWeaponId = activeWeapon.id,
            preferredRoast = playerChar.voiceLineRoasts.random()
        )
        players.add(mainPlayer)

        // 2. Add Bots according to selected mode
        val oppositionList = GameData.characters.filter { it.id != selectedCharacterId }.shuffled()

        when (mode) {
            "mom_kids" -> {
                // Special mode: Angry Mom vs naughty Kids.
                // Player as Mom or Kid. Let's make player the mom or kid.
                if (selectedCharacterId == "mom") {
                    // Player is Mom! Opponents are hostel boys & kids (3 bots)
                    for (i in 0..2) {
                        val botChar = GameData.characters.filter { it.id != "mom" }[i % 3]
                        players.add(
                            BattleSkeletor(
                                id = "bot_$i",
                                name = botChar.name,
                                emoji = botChar.emoji,
                                x = 400f + i * 200f,
                                y = gameHeight - 120f,
                                isBot = true,
                                health = botChar.baseHealth * 0.7f, // slightly easier to slap
                                maxHealth = botChar.baseHealth * 0.7f,
                                speed = botChar.baseSpeed * 4.5f,
                                basePower = botChar.basePower * 0.8f,
                                colorHex = botChar.colorHex,
                                preferredWeaponId = "hawai",
                                preferredRoast = botChar.voiceLineRoasts.random()
                            )
                        )
                    }
                } else {
                    // Player is Kid/Student. Opponent is Mom!
                    val momChar = GameData.characters.first { it.id == "mom" }
                    players.add(
                        BattleSkeletor(
                            id = "mom_boss",
                            name = momChar.name,
                            emoji = momChar.emoji,
                            x = 750f,
                            y = gameHeight - 120f,
                            isBot = true,
                            health = momChar.baseHealth * 1.5f, // Boss Level!
                            maxHealth = momChar.baseHealth * 1.5f,
                            speed = momChar.baseSpeed * 5.5f,
                            basePower = momChar.basePower * 1.3f,
                            colorHex = momChar.colorHex,
                            preferredWeaponId = "belan",
                            preferredRoast = momChar.voiceLineRoasts.random()
                        )
                    )
                }
            }
            "team" -> {
                // Team Battle: Player + 1 Bot (Blue Team) vs 2 Bots (Red Team)
                // Player is Blue Team
                val allyChar = oppositionList[0]
                players.add(
                    BattleSkeletor(
                        id = "ally_bot",
                        name = "${allyChar.name} (Friend)",
                        emoji = allyChar.emoji,
                        x = 300f,
                        y = gameHeight - 120f,
                        isBot = true,
                        health = allyChar.baseHealth,
                        maxHealth = allyChar.baseHealth,
                        speed = allyChar.baseSpeed * 5f,
                        colorHex = "#2196F3", // Make friendly blue
                        preferredWeaponId = "hawai",
                        preferredRoast = allyChar.voiceLineRoasts.random()
                    )
                )

                for (i in 1..2) {
                    val enemy = oppositionList[i]
                    players.add(
                        BattleSkeletor(
                            id = "enemy_bot_$i",
                            name = enemy.name,
                            emoji = enemy.emoji,
                            x = 650f + (i - 1) * 150f,
                            y = gameHeight - 120f,
                            isBot = true,
                            health = enemy.healthLevelFactor(),
                            maxHealth = enemy.healthLevelFactor(),
                            speed = enemy.baseSpeed * 5f,
                            colorHex = "#F44336", // Angry Red
                            preferredWeaponId = "rubber",
                            preferredRoast = enemy.voiceLineRoasts.random()
                        )
                    )
                }
            }
            else -> {
                // Free For All (Default "Hostel Chaos" or "Wedding Fight" or "ffa")
                // 3 competitive bots
                for (i in 0..2) {
                    val botChar = oppositionList[i % oppositionList.size]
                    players.add(
                        BattleSkeletor(
                            id = "bot_$i",
                            name = botChar.name,
                            emoji = botChar.emoji,
                            x = 450f + i * 180f,
                            y = gameHeight - 120f,
                            isBot = true,
                            health = botChar.baseHealth,
                            maxHealth = botChar.baseHealth,
                            speed = botChar.baseSpeed * 5f,
                            basePower = botChar.basePower,
                            colorHex = botChar.colorHex,
                            preferredWeaponId = botChar.preferredSlipperId,
                            preferredRoast = botChar.voiceLineRoasts.random()
                        )
                    )
                }
            }
        }

        // 3. Populate initial map hazards / passive items
        spawnPassiveItems(mapId)
        spawnComicPopup("COMBAT ARENA SHAKING!", gameWidth / 2f, 150f, "#FFEB3B", 1.4f)
    }

    private fun GameCharacter.healthLevelFactor() = baseHealth

    private fun spawnPassiveItems(mapId: String) {
        // Spawn banana peels, water puddles, exploding pillows based on maps
        passiveItems.clear()
        when (mapId) {
            "wedding" -> {
                passiveItems.add(BattleItem(nextItemId++, "puddle", "🍹", 300f, gameHeight - 75f)) // Fruit juice puddle
                passiveItems.add(BattleItem(nextItemId++, "banana", "🍌", 650f, gameHeight - 75f))
                passiveItems.add(BattleItem(nextItemId++, "couch", "🛋️", 500f, gameHeight - 95f))
            }
            "rooftop" -> {
                passiveItems.add(BattleItem(nextItemId++, "banana", "🍌", 250f, gameHeight - 75f))
                passiveItems.add(BattleItem(nextItemId++, "banana", "🍌", 700f, gameHeight - 75f))
                passiveItems.add(BattleItem(nextItemId++, "water_leak", "💦", 480f, gameHeight - 75f))
            }
            "classroom" -> {
                passiveItems.add(BattleItem(nextItemId++, "desk", "🪑", 350f, gameHeight - 95f))
                passiveItems.add(BattleItem(nextItemId++, "desk", "🪑", 650f, gameHeight - 95f))
                passiveItems.add(BattleItem(nextItemId++, "puddle", "🥛", 500f, gameHeight - 75f))
            }
            "colony" -> {
                passiveItems.add(BattleItem(nextItemId++, "scooter", "🛵", 200f, gameHeight - 110f))
                passiveItems.add(BattleItem(nextItemId++, "banana", "🍌", 550f, gameHeight - 75f))
                passiveItems.add(BattleItem(nextItemId++, "dog_trap", "💩", 800f, gameHeight - 75f))
            }
            "kitchen" -> {
                passiveItems.add(BattleItem(nextItemId++, "puddle", "🧴", 400f, gameHeight - 75f)) // Dishwashing soap
                passiveItems.add(BattleItem(nextItemId++, "stove", "🔥", 750f, gameHeight - 95f))
            }
        }
    }

    // Tick update (typically called 40 times a second for 40 FPS simulated mechanics)
    fun tick(joystickX: Float, actionIntent: String?) {
        // Check Slo-Mo timer
        var timeStep = 1.0f
        if (isSloMoActive) {
            timeStep = 0.35f
            sloMoTimer -= 0.025f
            if (sloMoTimer <= 0) {
                isSloMoActive = false
            }
        }

        // Update Popups
        val popupIt = popups.iterator()
        while (popupIt.hasNext()) {
            val p = popupIt.next()
            p.life -= 0.025f * timeStep
            if (p.life <= 0) {
                popupIt.remove()
            }
        }

        // Update Particles
        val partIt = particles.iterator()
        while (partIt.hasNext()) {
            val pt = partIt.next()
            pt.x += pt.vx * timeStep
            pt.y += pt.vy * timeStep
            pt.vy += 0.15f * timeStep // gravity on particles
            pt.life--
            if (pt.life <= 0) {
                partIt.remove()
            }
        }

        // Update Players
        players.forEach { p ->
            if (p.isDead) return@forEach

            // Cooldown ticks
            if (p.activeSpecialCooldown > 0) p.activeSpecialCooldown--
            if (p.hitStun > 0) p.hitStun--

            // Rage timers
            if (p.specialRageTimer > 0) {
                p.specialRageTimer -= 0.025f * timeStep
                if (Random.nextFloat() < 0.15f) {
                    spawnParticle(p.x, p.y - 40f, "🔥")
                }
            }

            // Slip timers (from banana)
            if (p.slipTimer > 0) {
                p.slipTimer -= 1.0f * timeStep
                p.headTilt += 25f // spin like crazy
                // Friction slide out of control
                p.x += p.vx * timeStep
                p.vx *= 0.96f
                applyLimits(p)
                return@forEach // Skip normal movement while slipping out of control!
            } else {
                p.headTilt = p.headTilt * 0.85f // return to normal
            }

            // Slap progress animation
            if (p.slapActive) {
                p.slapProgress += 0.18f * timeStep
                if (p.slapProgress >= 1f) {
                    p.slapActive = false
                    p.slapProgress = 0f
                }
            }

            // Apply friction & gravity
            p.vy += gravity * timeStep
            p.y += p.vy * timeStep
            p.x += p.vx * timeStep
            p.vx *= friction

            // Collide with Floor
            val groundLevel = gameHeight - 110f
            if (p.y >= groundLevel) {
                p.y = groundLevel
                p.vy = 0f
            }

            // Inputs
            if (!p.isBot) {
                // Human player control
                val moveSpeed = p.speed * if (p.specialRageTimer > 0) 1.6f else 1.0f
                // Detect water puddle reduction
                var activeReduction = 1.0f
                passiveItems.forEach { item ->
                    if (item.type == "puddle" && Math.abs(p.x - item.x) < 50f && p.y >= groundLevel - 10f) {
                        activeReduction = 0.5f // Super slow mud!
                    }
                }

                p.vx = joystickX * moveSpeed * activeReduction

                if (joystickX < -0.1f) p.direction = -1f
                else if (joystickX > 0.1f) p.direction = 1f

                // Execute instantaneous human intentions
                if (actionIntent != null && p.hitStun <= 0) {
                    handlePlayerAction(p, actionIntent)
                }
            } else {
                // Robot Intelligence (Bot AI)
                updateBotAI(p, timeStep)
            }

            // Hazard Slippings
            passiveItems.forEach { item ->
                if (item.type == "banana" && Math.abs(p.x - item.x) < 35f && p.y >= groundLevel - 15f) {
                    // SLIP!
                    p.slipTimer = 35f
                    p.vy = -5f
                    p.vx = p.direction * 12f
                    spawnComicPopup("SPINNING SLIP!", p.x, p.y - 60f, "#FF5722", 1.2f)
                    AudioSynthesizer.playScreamSound()
                    p.health = Math.max(0f, p.health - 5f)
                }
                if (item.type == "dog_trap" && Math.abs(p.x - item.x) < 35f && p.y >= groundLevel - 15f) {
                    p.hitStun = 40
                    p.vx = 0f
                    spawnComicPopup("STINKY POOP STUCK!", p.x, p.y - 60f, "#795548", 1.1f)
                    AudioSynthesizer.playHitSound()
                    p.health = Math.max(0f, p.health - 2f)
                }
            }

            applyLimits(p)
        }

        // Update Projectiles
        val projIt = projectiles.iterator()
        while (projIt.hasNext()) {
            val proj = projIt.next()
            proj.x += proj.vx * timeStep
            proj.y += proj.vy * timeStep
            proj.vy += (gravity * 0.7f) * timeStep // lighter gravity on slippers
            proj.rotation += 22f * timeStep

            // Boundary bounces
            if (proj.x < 10f || proj.x > gameWidth - 10f) {
                proj.vx = -proj.vx * 0.8f
                proj.bounces--
                AudioSynthesizer.playHitSound()
            }

            // Floor bounce
            val groundLevel = gameHeight - 95f
            if (proj.y >= groundLevel) {
                proj.y = groundLevel
                proj.vy = -proj.vy * 0.65f // bounce up
                proj.vx *= 0.85f // roll slowing
                proj.bounces--
                AudioSynthesizer.playHitSound()
            }

            // Check collision against players
            var currentHit = false
            players.forEach { p ->
                if (!p.isDead && p.id != proj.ownerId && Math.abs(proj.x - p.x) < 45f && Math.abs(proj.y - p.y) < 65f) {
                    // HIT!
                    applyProjectileImpact(proj, p)
                    currentHit = true
                }
            }

            if (proj.bounces <= 0 || currentHit) {
                // Spawn small impact particles
                for (a in 0..4) {
                    spawnParticle(proj.x, proj.y, listOf("💫", "💥", "⭐").random())
                }
                projIt.remove()
            }
        }

        // Match winning state validation
        val aliveCount = players.count { !it.isDead }
        if (aliveCount <= 1) {
            // End scenario handles externally through state updates of match outcomes
        }
    }

    private fun applyLimits(p: BattleSkeletor) {
        if (p.x < 30f) {
            p.x = 30f
            p.vx = 0f
        }
        if (p.x > gameWidth - 30f) {
            p.x = gameWidth - 30f
            p.vx = 0f
        }
    }

    private fun handlePlayerAction(p: BattleSkeletor, action: String) {
        when (action) {
            "attack" -> {
                p.slapActive = true
                p.slapProgress = 0f
                AudioSynthesizer.playSlapSound()

                // Melee trigger
                players.forEach { target ->
                    if (target.id != p.id && !target.isDead) { // hit calculation
                        val dist = Math.abs(p.x - target.x)
                        val vertDist = Math.abs(p.y - target.y)
                        val isFacing = (p.direction > 0 && target.x > p.x) || (p.direction < 0 && target.x < p.x)

                        if (dist < 80f && vertDist < 70f && isFacing) {
                            val activeWep = GameData.weapons.firstOrNull { it.id == p.preferredWeaponId } ?: GameData.weapons[0]
                            val calcDmg = activeWep.damage * p.basePower * (if (p.specialRageTimer > 0) 2.0f else 1.0f)
                            target.health = Math.max(0f, target.health - calcDmg)
                            target.hitStun = 12
                            target.vx = p.direction * activeWep.knockback * 1.5f
                            target.vy = -3f // small hop

                            p.score += (calcDmg * 1.5f).toInt()

                            // Play visuals
                            spawnComicPopup(
                                listOf("SLAP!", "CRITICAL SLAP!", "TATAA!", "CHATAAK!").random(),
                                target.x, target.y - 70f, "#E91E63", 1.2f
                            )
                            spawnParticle(target.x, target.y - 20f, "💥")
                            AudioSynthesizer.playHitSound()
                            onSlipperHit?.invoke()

                            if (target.health <= 0) {
                                target.isDead = true
                                p.score += 200
                                spawnComicPopup("OUT! 😂", target.x, target.y - 40f, "#FF1744", 1.5f)
                                spawnParticle(target.x, target.y, "💀")
                            }
                        }
                    }
                }
            }
            "jump" -> {
                val groundLevel = gameHeight - 110f
                if (p.y >= groundLevel - 5f) {
                    p.vy = -12.5f
                    spawnParticle(p.x, p.y + 35f, "💨")
                }
            }
            "throw" -> {
                AudioSynthesizer.playThrowSound()
                val activeWep = GameData.weapons.firstOrNull { it.id == p.preferredWeaponId } ?: GameData.weapons[0]
                val speedVal = 14f * activeWep.speedMultiplier

                projectiles.add(
                    BattleProjectile(
                        id = nextProjectileId++,
                        typeId = activeWep.id,
                        emoji = activeWep.emoji,
                        x = p.x + p.direction * 35f,
                        y = p.y - 30f,
                        vx = p.direction * speedVal,
                        vy = -5.5f,
                        ownerId = p.id,
                        damage = activeWep.damage * 0.8f,
                        knockback = activeWep.knockback
                    )
                )

                // Hand recoil
                p.vx = -p.direction * 2.5f
            }
            "special" -> {
                if (p.activeSpecialCooldown <= 0) {
                    p.activeSpecialCooldown = 280 // ticks
                    val selectPower = GameData.powers.random()

                    // Match unique rules
                    onSpecialMoveTriggered?.invoke(p.name, selectPower.name)
                    AudioSynthesizer.playPowerActivatedSound()

                    when (selectPower.id) {
                        "damage" -> {
                            // Emotional Damage on all alive enemies
                            players.forEach { target ->
                                if (target.id != p.id && !target.isDead) {
                                    target.health = Math.max(20f, target.health - 25f)
                                    target.hitStun = 25
                                    spawnComicPopup("EMOTIONAL DAMAGE!", target.x, target.y - 70f, "#E91E63", 1.4f)
                                    spawnParticle(target.x, target.y - 30f, "💔")
                                    spawnParticle(target.x, target.y - 35f, "😭")
                                }
                            }
                        }
                        "rain" -> {
                            // Rain down many projectiles
                            for (r in 0..6) {
                                projectiles.add(
                                    BattleProjectile(
                                        id = nextProjectileId++,
                                        typeId = "hawai",
                                        emoji = "🩴",
                                        x = 100f + r * 130f + Random.nextInt(-40, 40),
                                        y = 0f,
                                        vx = Random.nextFloat() * 4f - 2f,
                                        vy = 8f,
                                        ownerId = p.id,
                                        damage = 15f,
                                        knockback = 6f
                                    )
                                )
                            }
                            spawnComicPopup("CHAPAL STORM!", p.x, p.y - 80f, "#2196F3", 1.3f)
                        }
                        "rage" -> {
                            p.specialRageTimer = 8f // 8 seconds of mega speed
                            spawnComicPopup("FURY MODE UNLEASHED!", p.x, p.y - 80f, "#FF5722", 1.4f)
                        }
                        "slowmo" -> {
                            isSloMoActive = true
                            sloMoTimer = 4.0f // 4 seconds of slo-mo
                            spawnComicPopup("TIME SLOWED!", p.x, p.y - 80f, "#9C27B0", 1.3f)
                        }
                        "laugh" -> {
                            // Laugh gas stuns all others
                            players.forEach { target ->
                                if (target.id != p.id && !target.isDead) {
                                    target.hitStun = 60 // long laugh stun!
                                    spawnComicPopup("HAHAHA STUNNED!", target.x, target.y - 70f, "#4CAF50", 1.2f)
                                    spawnParticle(target.x, target.y - 30f, "😂")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateBotAI(p: BattleSkeletor, timeStep: Float) {
        if (p.isDead) return

        // Hit stun block
        if (p.hitStun > 0) return

        // Simple Bot Decisions: Find closest living target!
        val target = players.filter { !it.isDead && it.id != p.id }
            .minByOrNull { Math.abs(it.x - p.x) }

        if (target != null) {
            val dist = Math.abs(p.x - target.x)
            val isTargetAbove = target.y < p.y - 60f

            // Face target
            p.direction = if (target.x > p.x) 1f else -1f

            // Movement behavior
            val followPower = p.speed * 0.45f * if (p.specialRageTimer > 0) 1.5f else 1f
            p.vx = p.direction * followPower

            // Jumping to reach or dodge
            if (isTargetAbove && Random.nextFloat() < 0.03f && p.y >= gameHeight - 110f) {
                p.vy = -11.5f
            }

            // Decide combat action
            if (dist < 80f) {
                // Highly likely to melee slap!
                if (Random.nextFloat() < 0.07f * timeStep) {
                    handlePlayerAction(p, "attack")
                }
            } else if (dist > 200f && dist < 600f) {
                // Likely to throw chappal
                if (Random.nextFloat() < 0.015f * timeStep) {
                    handlePlayerAction(p, "throw")
                }
            }

            // Random triggers for Special Powers
            if (p.activeSpecialCooldown <= 0 && Random.nextFloat() < 0.004f * timeStep) {
                handlePlayerAction(p, "special")
            }
        } else {
            // Idle random patrolling
            if (Random.nextFloat() < 0.015f) {
                p.direction = -p.direction
            }
            p.vx = p.direction * p.speed * 0.3f
        }
    }

    private fun applyProjectileImpact(proj: BattleProjectile, p: BattleSkeletor) {
        val calcDmg = proj.damage * (if (p.specialRageTimer > 0) 0.5f else 1.0f) // less damage in rage
        p.health = Math.max(0f, p.health - calcDmg)
        p.hitStun = 15
        p.vx = signOf(proj.vx) * proj.knockback * 1.3f
        p.vy = -4f // pop up in air

        // Award score to owner if owner is player
        val ownerPlayer = players.firstOrNull { it.id == proj.ownerId }
        if (ownerPlayer != null && !ownerPlayer.isDead) {
            ownerPlayer.score += (calcDmg * 2).toInt()
        }

        spawnComicPopup(
            listOf("EMOTIONAL DAMAGE!", "BOOM CHAPPAL!", "CHATAACK!", "MAREEGA TUNE!").random(),
            p.x, p.y - 70f, "#FF9800", 1.2f
        )
        onSlipperHit?.invoke()
        AudioSynthesizer.playHitSound()

        if (p.health <= 0) {
            p.isDead = true
            ownerPlayer?.let { it.score += 200 }
            spawnComicPopup("OUT! 😂", p.x, p.y - 45f, "#FF1744", 1.4f)
            spawnParticle(p.x, p.y, "💀")
        }
    }

    private fun signOf(value: Float): Float {
        return if (value < 0) -1f else 1f
    }

    fun spawnParticle(x: Float, y: Float, emoji: String) {
        val vx = Random.nextFloat() * 6f - 3f
        val vy = Random.nextFloat() * -5f - 2f
        particles.add(GameParticle(x, y, vx, vy, emoji, 12 + Random.nextInt(8)))
    }

    fun spawnComicPopup(text: String, x: Float, y: Float, color: String, scale: Float) {
        popups.add(ComicPopup(nextPopupId++, text, x, y, 1.0f, color, scale))
    }
}
