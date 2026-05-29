package com.example.engine

data class GameCharacter(
    val id: String,
    val name: String,
    val role: String,
    val description: String,
    val emoji: String,
    val baseSpeed: Float,
    val baseHealth: Float,
    val basePower: Float,
    val voiceLineRoasts: List<String>,
    val specialMoveName: String,
    val specialMoveDesc: String,
    val preferredSlipperId: String,
    val coinCost: Int = 0,
    val colorHex: String
)

data class GameWeapon(
    val id: String,
    val name: String,
    val emoji: String,
    val damage: Float,
    val speedMultiplier: Float,
    val knockback: Float,
    val description: String,
    val coinCost: Int = 0
)

data class SpecialPower(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val statusBubble: String
)

data class ArenaMap(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val wallColorHex: String,
    val floorColorHex: String,
    val hazards: List<String>
)

object GameData {
    val characters = listOf(
        GameCharacter(
            id = "mom",
            name = "Angry Mom",
            role = "Household Commander",
            description = "Armed with unlimited range precision, she can hit targets around corners. Fear the Belan!",
            emoji = "👵🔥",
            baseSpeed = 1.0f,
            baseHealth = 120f,
            basePower = 1.2f,
            voiceLineRoasts = listOf(
                "Nalayak! Sharam Karo!",
                "Sharma ji ke bete ko dekho!",
                "Paddhna likhna chhod do bas!",
                "Abhi maza chakhata hu tereko!"
            ),
            specialMoveName = "Belan Cyclone",
            specialMoveDesc = "Spins a circle of spinning rolling-pins that knock out everyone nearby!",
            preferredSlipperId = "hawai",
            coinCost = 0,
            colorHex = "#E91E63" // Pink
        ),
        GameCharacter(
            id = "boy",
            name = "Hostel Boy",
            role = "Maggie Consumer",
            description = "Evasive, fast, survives purely on 2-minute instant noodles and raw desperation.",
            emoji = "🎽🙋‍♂️",
            baseSpeed = 1.4f,
            baseHealth = 90f,
            basePower = 0.9f,
            voiceLineRoasts = listOf(
                "Bro, WiFi password batana?",
                "Proxy laga lena kal!",
                "Maggie ready hone do pehle!",
                "Ghar ka khana yaad aa raha hai..."
            ),
            specialMoveName = "Maggie Bomb Slap",
            specialMoveDesc = "Throws instant hot noodle explosions slowing down any opponent!",
            preferredSlipperId = "towel",
            coinCost = 300,
            colorHex = "#2196F3" // Blue
        ),
        GameCharacter(
            id = "uncle",
            name = "Dance Uncle",
            role = "Baraat Sensation",
            description = "Performs unmatched Bhangra/Naggin steps. Immune to fatigue, loves loud music.",
            emoji = "🕺🍺",
            baseSpeed = 0.8f,
            baseHealth = 130f,
            basePower = 1.1f,
            voiceLineRoasts = listOf(
                "Oye hoye yaaro, dhol bajao!",
                "Aisi dance step dekhi hai?",
                "Aur ek patiala peg ho jaye!",
                "Swaag se karenge sabka swagat!"
            ),
            specialMoveName = "Baraat Spin",
            specialMoveDesc = "Triggers high-velocity spinning kicks throwing empty bottles randomly!",
            preferredSlipperId = "leather",
            coinCost = 500,
            colorHex = "#FFEB3B" // Yellow
        ),
        GameCharacter(
            id = "student",
            name = "Lazy Student",
            role = "Backbencher King",
            description = "Highly defensive, absorbs heavy damage by falling back asleep instantly.",
            emoji = "😪🥱",
            baseSpeed = 0.7f,
            baseHealth = 140f,
            basePower = 0.8f,
            voiceLineRoasts = listOf(
                "Attandence please!",
                "Exam mein sab option 'C' likhungi!",
                "Bas 5 minute aur dadi...",
                "Syllabus out of syllabus tha!"
            ),
            specialMoveName = "Exam Nap Barrier",
            specialMoveDesc = "Creates a dream bubble that absorbs all incoming slippers!",
            preferredSlipperId = "pillow",
            coinCost = 400,
            colorHex = "#9C27B0" // Purple
        ),
        GameCharacter(
            id = "gym",
            name = "Gym Guy",
            role = "Scoops & Reps",
            description = "Heavy impact hitter. Loves protein, can carry heavy chappals with ease.",
            emoji = "💪🏋️‍♂️",
            baseSpeed = 1.1f,
            baseHealth = 110f,
            basePower = 1.4f,
            voiceLineRoasts = listOf(
                "Bro, protein scoop kitna liya?",
                "Leg day skip kiya lagta h!",
                "Double biceps dekh!",
                "No pain no gain, samjhe?"
            ),
            specialMoveName = "Protein Splash",
            specialMoveDesc = "Slam shockwave that knocks back everyone on the floor within 5 meters!",
            preferredSlipperId = "rubber",
            coinCost = 600,
            colorHex = "#FF5722" // Red-Orange
        ),
        GameCharacter(
            id = "kid",
            name = "Crying Kid",
            role = "Toy Dropper",
            description = "An annoying tiny force. Difficult to hit, deafens opponents on rage hit.",
            emoji = "😭🍼",
            baseSpeed = 1.3f,
            baseHealth = 80f,
            basePower = 1.0f,
            voiceLineRoasts = listOf(
                "Papa ko bolunga main!",
                "Mujhe chocolate chahiye!",
                "Waaaaah! Cheating kiya tune!",
                "Toy toot gaya mera!"
            ),
            specialMoveName = "Tantrum Screamer",
            specialMoveDesc = "High-decibel crying wave that stunning-stops all movement around!",
            preferredSlipperId = "bottle",
            coinCost = 350,
            colorHex = "#4CAF50" // Green
        ),
        GameCharacter(
            id = "villain",
            name = "Funny Villain",
            role = "Sholay Enforcer",
            description = "Classic retro cinema antagonist. Always makes monologue mistakes.",
            emoji = "🦹🏽🕶️",
            baseSpeed = 0.9f,
            baseHealth = 115f,
            basePower = 1.1f,
            voiceLineRoasts = listOf(
                "Kitne aadmi the?!",
                "Jo darr gaya samjho marr gaya!",
                "Mugambo khush hua!",
                "Yeh haath mujhe dede thakur!"
            ),
            specialMoveName = "Villainous Monologue",
            specialMoveDesc = "Throws high-knockback spinning rolling pins and points finger with laughter!",
            preferredSlipperId = "rolling_pin",
            coinCost = 450,
            colorHex = "#795548" // Brown
        ),
        GameCharacter(
            id = "grandpa",
            name = "Meme Grandpa",
            role = "Colony Legend",
            description = "Tells old-time war stories that stun, then throws legendary wooden sandals.",
            emoji = "👴🏽🪵",
            baseSpeed = 0.6f,
            baseHealth = 150f,
            basePower = 1.3f,
            voiceLineRoasts = listOf(
                "Humare zamane mein...",
                "1 paise me 10 chappal aati thi!",
                "Sharam karo sanskar sikho!",
                "Mobile phek ke marunga!"
            ),
            specialMoveName = "Dandaji Hammer",
            specialMoveDesc = "Smashes walking cane down, summoning vertical chappal rain onto targets!",
            preferredSlipperId = "wooden",
            coinCost = 700,
            colorHex = "#607D8B" // Slate
        )
    )

    val weapons = listOf(
        GameWeapon(
            id = "hawai",
            name = "Hawai Slipper",
            emoji = "🩴",
            damage = 15f,
            speedMultiplier = 1.0f,
            knockback = 5.0f,
            description = "The classic blue and white rubber icon of discipline.",
            coinCost = 0
        ),
        GameWeapon(
            id = "belan",
            name = "Wooden Belan",
            emoji = "🥖",
            damage = 25f,
            speedMultiplier = 0.8f,
            knockback = 9.0f,
            description = "High density rolling pin with a 100% chance of emotional trauma.",
            coinCost = 250
        ),
        GameWeapon(
            id = "towel",
            name = "Wet Towel",
            emoji = "🧣",
            damage = 12f,
            speedMultiplier = 1.3f,
            knockback = 4.0f,
            description = "Whip crack action. Super-fast reloads, causes moderate sting.",
            coinCost = 150
        ),
        GameWeapon(
            id = "pillow",
            name = "Exploding Pillow",
            emoji = "🛏️",
            damage = 10f,
            speedMultiplier = 0.9f,
            knockback = 12.0f,
            description = "Soft but massive knockback. Bursting with soft white feathers!",
            coinCost = 200
        ),
        GameWeapon(
            id = "bottle",
            name = "Plastic Bottle",
            emoji = "🍼",
            damage = 8f,
            speedMultiplier = 1.2f,
            knockback = 3.0f,
            description = "Clanky, funny noise on hits, fast rapid firing speed.",
            coinCost = 100
        ),
        GameWeapon(
            id = "rubber",
            name = "Spiked Rubber Slipper",
            emoji = "👟",
            damage = 18f,
            speedMultiplier = 1.1f,
            knockback = 6.0f,
            description = "Aerodynamic modern chappal for precise curves.",
            coinCost = 300
        ),
        GameWeapon(
            id = "rolling_pin",
            name = "Rolling Pin",
            emoji = "🪵",
            damage = 22f,
            speedMultiplier = 0.85f,
            knockback = 8.0f,
            description = "Heavy hard-wood roller, double hit combo potential.",
            coinCost = 350
        ),
        GameWeapon(
            id = "wooden",
            name = "Wooden Khadau Sankar",
            emoji = "🪵🩴",
            damage = 30f,
            speedMultiplier = 0.7f,
            knockback = 15.0f,
            description = "Solid ancient Indian wooden sandal. Extremely heavy damage.",
            coinCost = 500
        )
    )

    val powers = listOf(
        SpecialPower(
            id = "damage",
            name = "Emotional Damage!",
            emoji = "💔😭",
            description = "Causes target to break into tears, decreasing their attack power and defense by 50%!",
            statusBubble = "EMOTIONAL DAMAGE!"
        ),
        SpecialPower(
            id = "rain",
            name = "Chappal Rain",
            emoji = "🩴🌧️",
            description = "Summons a chaotic barrage of slippers of all sizes falling down from heaven!",
            statusBubble = "CHAPPAL STORM!"
        ),
        SpecialPower(
            id = "rage",
            name = "Mummy Angry Mode",
            emoji = "🤬🔥",
            description = "Increases damage, movement speed, and slipper throw velocity by 200% for 8 seconds!",
            statusBubble = "FURY RAGE!"
        ),
        SpecialPower(
            id = "slowmo",
            name = "Slow-Mo Slap",
            emoji = "🐌👋",
            description = "Temporarily slows down all opponents by 70%, creating high slap accuracy!",
            statusBubble = "SLO-MO ACTION!"
        ),
        SpecialPower(
            id = "laugh",
            name = "Indian Laugh Bomb",
            emoji = "😂💣",
            description = "Spawns a laughing gas cloud that stuns all players with laughter for 3 seconds!",
            statusBubble = "HAHAHAHA!"
        )
    )

    val maps = listOf(
        ArenaMap(
            id = "wedding",
            name = "Indian Wedding Hall",
            emoji = "🏛️💐",
            description = "Draped in gold-marigold curtains, featuring random falling sweets, flying paneer cubes, and dancing DJs.",
            wallColorHex = "#D32F2F", // Rich Indian Red
            floorColorHex = "#FFB300", // Festive Gold
            hazards = listOf("Falling Food Plates", "Baraat Speakers shake")
        ),
        ArenaMap(
            id = "rooftop",
            name = "Hostel Rooftop",
            emoji = "🏢💦",
            description = "Under the majestic blue water tanks, wind-prone lines of drying bedsheets and messy water leaks.",
            wallColorHex = "#455A64", // Grayish blue
            floorColorHex = "#90A4AE", // Light stone
            hazards = listOf("Wind gust blows slippers", "Water leakage")
        ),
        ArenaMap(
            id = "classroom",
            name = "School Classroom",
            emoji = "🏫📝",
            description = "Desks as barriers, blackboards containing funny drawings, and falling chalk box hazards.",
            wallColorHex = "#004D40", // Blackboard Green
            floorColorHex = "#8D6E63", // Desks Brown
            hazards = listOf("Flying Chalks", "Tteacher's desk block")
        ),
        ArenaMap(
            id = "colony",
            name = "Colony Street",
            emoji = "🏘️🐕",
            description = "Parked scooters, plants on balconies, and a stray dog that barking-runs across.",
            wallColorHex = "#E0F2F1", // Light teal
            floorColorHex = "#B2DFDB", // Paved floor
            hazards = listOf("Stray Dog sweep", "Parked scooter collision")
        ),
        ArenaMap(
            id = "kitchen",
            name = "Kitchen Arena",
            emoji = "🍳🌶️",
            description = "A spicy battleground of gas burners, flying flour bags, and water pools.",
            wallColorHex = "#F4511E", // Chilli Orange
            floorColorHex = "#D84315", // Dark Clay floor
            hazards = listOf("Chili gas cloud", "Water puddle slip")
        )
    )
}
