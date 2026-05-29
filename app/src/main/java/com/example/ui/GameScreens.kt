package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.database.UserProfile
import com.example.engine.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ComicCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    borderColor: Color = Color(0xFF0F172A),
    borderWidth: androidx.compose.ui.unit.Dp = 3.dp,
    shadowColor: Color = Color(0xFF0F172A),
    shadowOffset: androidx.compose.ui.unit.Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        // Shadow Box
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )
        // Main Foreground Box
        Column(
            modifier = Modifier
                .background(containerColor, shape)
                .border(borderWidth, borderColor, shape)
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
fun ComicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color.White,
    borderColor: Color = Color(0xFF0F172A),
    borderWidth: androidx.compose.ui.unit.Dp = 3.dp,
    shadowColor: Color = Color(0xFF0F172A),
    shadowOffset: androidx.compose.ui.unit.Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedOffset = if (isPressed) 1.dp else shadowOffset

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        if (enabled) {
            // Shadow Box
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(shadowColor, shape)
            )
        }
        // Main Foreground Box
        Row(
            modifier = Modifier
                .offset(
                    x = if (enabled) (shadowOffset - animatedOffset) else 0.dp,
                    y = if (enabled) (shadowOffset - animatedOffset) else 0.dp
                )
                .background(if (enabled) containerColor else Color(0xFFCBD5E1), shape)
                .border(borderWidth, if (enabled) borderColor else Color(0xFF94A3B8), shape)
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun ChappalFightApp(viewModel: GameViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val charUnlocks by viewModel.characterUnlocks.collectAsStateWithLifecycle()
    val chappalUnlocks by viewModel.chappalUnlocks.collectAsStateWithLifecycle()

    val profile = userProfile ?: UserProfile()

    val isGameplay = viewModel.currentScreen == "GAME_PLAY"
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isGameplay) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFACC15), Color(0xFFFFB300))
                    )
                }
            )
            .safeDrawingPadding()
    ) {
        Crossfade(targetState = viewModel.currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                "SPLASH" -> SplashView()
                "MAIN_MENU" -> MainMenuView(viewModel, profile)
                "CHARACTER_SELECT" -> CharacterSelectView(viewModel, profile, charUnlocks)
                "SLIPPER_SHOP" -> SlipperShopView(viewModel, profile, chappalUnlocks)
                "UPGRADE_SCREEN" -> UpgradesView(viewModel, profile)
                "DAILY_REWARD" -> DailyRewardView(viewModel)
                "GAME_PLAY" -> GamePlayView(viewModel, profile)
                "MATCH_SUMMARY" -> MatchSummaryView(viewModel)
            }
        }

        // Real-Time Global Announcement Overlay
        if (viewModel.currentSpecialAnnouncement.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .background(Color(0xFFFF1744), RoundedCornerShape(16.dp))
                    .border(3.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = viewModel.currentSpecialAnnouncement,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SplashView() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnim")
    val angle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChappalWiggle"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🩴 🔥 🩴",
            modifier = Modifier
                .rotate(angle)
                .padding(bottom = 16.dp),
            fontSize = 80.sp
        )
        Text(
            text = "CHAPPAL FIGHT",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
            lineHeight = 44.sp
        )
        Text(
            text = "ARENA",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFEC4899),
            textAlign = TextAlign.Center,
            lineHeight = 46.sp,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0xFF0F172A),
                    offset = Offset(3f, 3f),
                    blurRadius = 0f
                )
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "MOM'S DISCIPLINE ARENA",
            color = Color(0xFF0F172A).copy(alpha = 0.8f),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        CircularProgressIndicator(
            color = Color(0xFF0F172A),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Heating up the Belan...",
            color = Color(0xFF0F172A).copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MainMenuView(viewModel: GameViewModel, profile: UserProfile) {
    val activeCharacter = GameData.characters.firstOrNull { it.id == profile.selectedCharacterId } ?: GameData.characters[0]
    val activeWeapon = GameData.weapons.firstOrNull { it.id == profile.selectedSlipperId } ?: GameData.weapons[0]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("main_menu_scroll"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coin & Character indicator
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0x1A000000), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF3B82F6), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeCharacter.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeCharacter.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                // Currency pill badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coins Pill
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile.coins}",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.testTag("coin_indicator")
                        )
                    }

                    // Daily rewards Star Button styled with border-b-4
                    IconButton(
                        onClick = { viewModel.currentScreen = "DAILY_REWARD" },
                        modifier = Modifier
                            .testTag("btn_daily_reward_nav")
                            .size(38.dp)
                            .background(Color(0xFFFFC107), CircleShape)
                            .border(2.dp, Color(0xFF0F172A), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Daily Reward",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Comic Header Title Poster
        item {
            ComicCard(
                containerColor = Color(0xFFEC4899), // Pink
                borderColor = Color(0xFF0F172A),
                shadowOffset = 6.dp
            ) {
                Text(
                    text = "CHAPPAL FIGHT",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
                Text(
                    text = "ARENA!",
                    color = Color(0xFFFACC15), // Yellow
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF0F172A),
                            offset = Offset(2f, 2f),
                            blurRadius = 0f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🇮🇳 ULTIMATE COMEDY CLASH 🇮🇳",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Selected Fighter Comic Box
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                ComicCard(
                    containerColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowOffset = 5.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fighter emoji & floating mini-chappal decorator next to it!
                        Box(
                            modifier = Modifier.padding(end = 16.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Text(
                                text = activeCharacter.emoji,
                                fontSize = 68.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF60A5FA), RoundedCornerShape(6.dp))
                                    .border(1.5.dp, Color.White, RoundedCornerShape(6.dp))
                                    .offset(x = 4.dp, y = 4.dp)
                                    .rotate(15f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = activeWeapon.emoji, fontSize = 12.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeCharacter.name,
                                color = Color(0xFF0F172A),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Special Move: \"${activeCharacter.specialMoveName}\"",
                                color = Color(0xFFEF4444), // Pink/Red
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                            Text(
                                text = activeCharacter.description.take(50) + "...",
                                color = Color(0xFF475569),
                                fontSize = 10.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ComicButton(
                        onClick = { viewModel.currentScreen = "CHARACTER_SELECT" },
                        containerColor = Color(0xFF3B82F6), // Blue
                        shape = RoundedCornerShape(14.dp),
                        shadowOffset = 3.dp
                    ) {
                        Text(
                            text = "CHANGE HERO",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                // "Emotional Damage!" floating tag
                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = (-12).dp)
                        .rotate(-8f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Emotional Damage!",
                        color = Color(0xFFEC4899),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Selected Arena and Mode selections Header
        item {
            Text(
                text = "SELECT WEAPON & RULES",
                color = Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Weapon Display Comic Style
        item {
            ComicCard(
                containerColor = Color.White,
                borderColor = Color(0xFF0F172A),
                shadowOffset = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.currentScreen = "SLIPPER_SHOP" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = activeWeapon.emoji, fontSize = 42.sp, modifier = Modifier.padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Equipped: ${activeWeapon.name}",
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "💥 Damage: ${activeWeapon.damage} | 🦾 Knockback: ${activeWeapon.knockback}",
                            color = Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Shop",
                        tint = Color(0xFF0F172A)
                    )
                }
            }
        }

        // Mode Choice comic buttons style
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MATCH RULES",
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ffa" to "🎯 Free",
                        "team" to "🤝 Team",
                        "mom_kids" to "👵 Mom"
                    ).forEach { (modeCode, desc) ->
                        val isSel = viewModel.selectedBattleMode == modeCode
                        ComicButton(
                            onClick = { viewModel.selectedBattleMode = modeCode },
                            containerColor = if (isSel) Color(0xFF3B82F6) else Color.White,
                            shadowOffset = if (isSel) 2.dp else 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("mode_$modeCode")
                        ) {
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSel) Color.White else Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Map Selection Horizontal Scroll Comic Style
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SELECT ARENA MAP",
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(GameData.maps) { map ->
                        val isSel = viewModel.selectedMapId == map.id
                        Box(
                            modifier = Modifier
                                .width(135.dp)
                                .clickable { viewModel.selectedMapId = map.id }
                        ) {
                            ComicCard(
                                containerColor = if (isSel) Color(0xFF3B82F6) else Color.White,
                                borderColor = Color(0xFF0F172A),
                                shadowOffset = if (isSel) 2.dp else 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = map.emoji, fontSize = 32.sp)
                                Text(
                                    text = "ARENA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) Color.White.copy(alpha = 0.8f) else Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = map.name.replace(" Arena", "").replace(" Hall", "").replace(" Rooftop", "").replace(" School", ""),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) Color.White else Color(0xFF0F172A),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fast Action Hub (Upgrades, Slipper Shop)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ComicButton(
                    onClick = { viewModel.currentScreen = "UPGRADE_SCREEN" },
                    containerColor = Color(0xFF10B981), // Green-500
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("btn_upgrade_screen")
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "Upgrade", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("UPGRADES", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                }

                ComicButton(
                    onClick = { viewModel.currentScreen = "SLIPPER_SHOP" },
                    containerColor = Color(0xFF3B82F6), // Blue-500
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("btn_shop_screen")
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Shop", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SHOP", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                }
            }
        }

        // BIG PINK LAUNCH BATTLE BUTTON
        item {
            ComicButton(
                onClick = { viewModel.startMatch() },
                containerColor = Color(0xFFEC4899), // Pink-500
                shape = RoundedCornerShape(24.dp),
                shadowOffset = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_play_match")
            ) {
                Text(
                    text = "BATTLE!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        // Comedy Rules / Roasts Scroller info
        item {
            ComicCard(
                containerColor = Color.White,
                borderColor = Color(0xFF0F172A),
                shadowOffset = 3.dp,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "💡 SPECIAL LAWS OF CHAPPAL PHYSICS:",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Banana peels make players trip and spin out of control!\n• Emotional Damage attacks reduce strength by 50%!\n• Avoid being cornered by Angry Mom!",
                    color = Color(0xFF475569),
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

@Composable
fun CharacterSelectView(
    viewModel: GameViewModel,
    profile: UserProfile,
    charUnlocks: List<com.example.database.CharacterUnlock>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen = "MAIN_MENU" }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Text(
                text = "COMEDY FIGHTERS LOBBY",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(GameData.characters) { char ->
                val isUnlocked = char.coinCost == 0 || charUnlocks.any { it.characterId == char.id && it.isUnlocked }
                val isSelected = profile.selectedCharacterId == char.id

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ComicCard(
                        containerColor = if (isSelected) Color(0xFFFEF3C7) else Color.White,
                        borderColor = Color(0xFF0F172A),
                        shadowOffset = if (isSelected) 2.dp else 4.dp,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(text = char.emoji, fontSize = 54.sp, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = char.name,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Role: ${char.role}",
                                    color = Color(0xFF3B82F6),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = char.description,
                            color = Color(0xFF475569),
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Special attack: \"${char.specialMoveName}\" - ${char.specialMoveDesc}",
                            color = Color(0xFFEC4899),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("RUNNING PATIENCE", color = Color(0xFF475569), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = (char.baseSpeed / 1.5f).coerceIn(0f, 1f),
                                    color = Color(0xFF3B82F6),
                                    trackColor = Color(0xFFE2E8F0),
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Starting Health", color = Color(0xFF475569), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = (char.baseHealth / 150f).coerceIn(0f, 1f),
                                    color = Color(0xFFEF4444),
                                    trackColor = Color(0xFFE2E8F0),
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                )
                            }
                        }

                        // Unlock/Select actions
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!isUnlocked) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🪙 ${char.coinCost}",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                                ComicButton(
                                    onClick = { viewModel.attemptUnlockCharacter(char.id, char.coinCost) },
                                    containerColor = Color(0xFFEC4899),
                                    shape = RoundedCornerShape(12.dp),
                                    shadowOffset = 3.dp,
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text("UNLOCK", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                                }
                            }
                        } else {
                            ComicButton(
                                onClick = { viewModel.selectCharacter(char.id) },
                                containerColor = if (isSelected) Color(0xFF10B981) else Color(0xFF3B82F6),
                                enabled = !isSelected,
                                shape = RoundedCornerShape(12.dp),
                                shadowOffset = 3.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isSelected) "CURRENT FIGHTER" else "SELECT FIGHTER",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlipperShopView(
    viewModel: GameViewModel,
    profile: UserProfile,
    chappalUnlocks: List<com.example.database.ChappalUnlock>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen = "MAIN_MENU" }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Text(
                text = "SLIPPER & WEAPONS DEPOT",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "🪙 ${profile.coins} COINS",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(GameData.weapons) { weapon ->
                val isUnlocked = weapon.coinCost == 0 || chappalUnlocks.any { it.chappalId == weapon.id && it.isUnlocked }
                val isSelected = profile.selectedSlipperId == weapon.id

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ComicCard(
                        containerColor = if (isSelected) Color(0xFFFEF3C7) else Color.White,
                        borderColor = Color(0xFF0F172A),
                        shadowOffset = if (isSelected) 2.dp else 4.dp,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(text = weapon.emoji, fontSize = 48.sp, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = weapon.name,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = weapon.description,
                                    color = Color(0xFF475569),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Attack weapon indicators
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💥 Damage: ${weapon.damage}",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🛸 Speed: x${weapon.speedMultiplier}",
                                color = Color(0xFF3B82F6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🦾 Push: ${weapon.knockback}",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!isUnlocked) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🪙 ${weapon.coinCost}",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                ComicButton(
                                    onClick = { viewModel.attemptUnlockSlipper(weapon.id, weapon.coinCost) },
                                    containerColor = Color(0xFFEC4899),
                                    shape = RoundedCornerShape(12.dp),
                                    shadowOffset = 3.dp,
                                    modifier = Modifier.width(150.dp)
                                ) {
                                    Text("UNLOCK SLIPPER", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                                }
                            }
                        } else {
                            ComicButton(
                                onClick = { viewModel.selectSlipper(weapon.id) },
                                containerColor = if (isSelected) Color(0xFF10B981) else Color(0xFF3B82F6),
                                enabled = !isSelected,
                                shape = RoundedCornerShape(12.dp),
                                shadowOffset = 3.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isSelected) "EQUIPPED" else "EQUIP SLIPPER",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpgradesView(viewModel: GameViewModel, profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen = "MAIN_MENU" }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Text(
                text = "TRAINING COURT: UPGRADES",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        ComicCard(
            containerColor = Color(0xFFF1F5F9),
            borderColor = Color(0xFF0F172A),
            shadowOffset = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🪙 AVAILABLE BANK: ${profile.coins} COINS",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Every training grade improves slap damage, movement speed, and max health parameters permanently!",
                color = Color(0xFF475569),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upgrade Options
        listOf(
            Triple("💢 SLAP ATTACK POWER", profile.powerLevel, { viewModel.upgradePower() }),
            Triple("⚡ REACTION VELOCITY", profile.speedLevel, { viewModel.upgradeSpeed() }),
            Triple("❤️ TOUGH DERMIS HEALTH", profile.healthLevel, { viewModel.upgradeHealth() })
        ).forEach { (name, level, action) ->
            val cost = viewModel.getUpgradeCost(level)
            val canAfford = profile.coins >= cost

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                ComicCard(
                    containerColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowOffset = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = name, color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text(text = "Current Grade: Level $level", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        ComicButton(
                            onClick = action,
                            enabled = canAfford,
                            containerColor = Color(0xFFFACC15),
                            shape = RoundedCornerShape(12.dp),
                            shadowOffset = 3.dp,
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text(text = "🪙 $cost", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyRewardView(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ComicCard(
            containerColor = Color.White,
            borderColor = Color(0xFF0F172A),
            shadowOffset = 6.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(text = "👵🏽🩴", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "MOM'S COIN BLESSINGS",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Clean up your school bag and Mummy will reward you with rich arcade gold!",
                color = Color(0xFF475569),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            ComicButton(
                onClick = { viewModel.claimDailyReward() },
                enabled = !viewModel.dailyRewardCooldownActive,
                containerColor = Color(0xFF10B981),
                shape = RoundedCornerShape(16.dp),
                shadowOffset = 4.dp,
                modifier = Modifier.fillMaxWidth().testTag("btn_claim_daily_reward")
            ) {
                Text(
                    text = "CLAIM SACRED BLESSINGS",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }

            if (viewModel.dailyRewardFeedback.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = viewModel.dailyRewardFeedback,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ComicButton(
            onClick = { viewModel.currentScreen = "MAIN_MENU" },
            containerColor = Color.White,
            borderColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp),
            shadowOffset = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GO BACK TO COURT", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun GamePlayView(viewModel: GameViewModel, profile: UserProfile) {
    val engine = viewModel.gameEngine
    val textMeasurer = rememberTextMeasurer()
    val tick = viewModel.gameTick // Triggers optimized recomposition precisely on each engine tick

    // Cache structure to reuse pre-measured TextLayoutResult and prevent on-the-fly measure overhead
    val textLayoutCache = remember { mutableMapOf<String, TextLayoutResult>() }
    if (textLayoutCache.size > 250) {
        textLayoutCache.clear()
    }

    val getCachedResult = { text: String, fontSizeSp: Int, isBold: Boolean ->
        val cacheKey = "${text}_${fontSizeSp}_${isBold}"
        textLayoutCache.getOrPut(cacheKey) {
            textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(
                    fontSize = fontSizeSp.sp,
                    fontWeight = if (isBold) FontWeight.Black else FontWeight.Normal
                )
            )
        }
    }

    // Match details
    val currentMap = GameData.maps.firstOrNull { it.id == viewModel.selectedMapId } ?: GameData.maps[0]

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(android.graphics.Color.parseColor(currentMap.floorColorHex)))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Scaler between engine space and screen pixel coordinates
        // Engine coordinates: gameWidth = 1000f, gameHeight = 600f
        val scaleX = widthPx / engine.gameWidth
        val scaleY = heightPx / engine.gameHeight

        // Render Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("arena_canvas")
        ) {
            // Draw Arena background elements
            drawBackgroundDecoration(currentMap, size)

            // Draw passive interactive items
            engine.passiveItems.forEach { item ->
                val drawX = item.x * scaleX
                val drawY = item.y * scaleY
                val layout = getCachedResult(item.emoji, 24, false)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(drawX - 25f, drawY - 25f)
                )
            }

            // Draw players
            engine.players.forEach { p ->
                val drawX = p.x * scaleX
                val drawY = p.y * scaleY

                if (p.isDead) {
                    val layout = getCachedResult("💀", 28, false)
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(drawX - 20f, drawY - 30f)
                    )
                } else {
                    // Draw custom comic avatar skeleton body
                    val activeColor = Color(android.graphics.Color.parseColor(p.colorHex))

                    // Draw head circle/face
                    drawCircle(
                        color = activeColor,
                        radius = 22f * scaleX,
                        center = Offset(drawX, drawY - 30f * scaleY)
                    )

                    // Draw body line
                    drawLine(
                        color = Color.White,
                        start = Offset(drawX, drawY - 14f * scaleY),
                        end = Offset(drawX, drawY + 15f * scaleY),
                        strokeWidth = 6f * scaleX,
                        cap = StrokeCap.Round
                    )

                    // Left/Right hands
                    val handAngle = p.direction * (if (p.slapActive) (p.slapProgress * 70f) else 15f)
                    val radians = Math.toRadians((90f - handAngle).toDouble())
                    val handLength = 26f * scaleY
                    val handEndX = drawX + (handLength * cos(radians) * p.direction).toFloat()
                    val handEndY = drawY - 5f * scaleY + (handLength * sin(radians)).toFloat()

                    drawLine(
                        color = Color.LightGray,
                        start = Offset(drawX, drawY - 5f * scaleY),
                        end = Offset(handEndX, handEndY),
                        strokeWidth = 4f * scaleX,
                        cap = StrokeCap.Round
                    )

                    // Slipper at hand end
                    val equippedSlipper = GameData.weapons.firstOrNull { it.id == p.preferredWeaponId } ?: GameData.weapons[0]
                    val layout = getCachedResult(equippedSlipper.emoji, 16, false)
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(handEndX - 10f, handEndY - 10f)
                    )

                    // Face/Emoji expressions based on status
                    val faceText = when {
                        p.hitStun > 0 -> "😫"
                        p.slipTimer > 0 -> "🤪"
                        p.specialRageTimer > 0 -> "🤬"
                        else -> p.emoji.take(2)
                    }

                    val faceLayout = getCachedResult(faceText, 17, false)
                    drawText(
                        textLayoutResult = faceLayout,
                        topLeft = Offset(drawX - 16f, drawY - 42f * scaleY)
                    )

                    // Active Player Arrow Indicator
                    if (p.id == "player") {
                        drawPlayerPointer(drawX, drawY, scaleY)
                    }

                    // Floating text player name
                    val nameLayout = getCachedResult(p.name, 10, true)
                    drawText(
                        textLayoutResult = nameLayout,
                        color = Color.White,
                        topLeft = Offset(drawX - 45f, drawY - 65f * scaleY)
                    )

                    // Health mini-slider
                    val hpWidth = 50f * scaleX
                    val hpHeight = 5f * scaleY
                    val hpPercent = (p.health / p.maxHealth).coerceIn(0f, 1f)

                    drawRect(
                        color = Color.DarkGray,
                        topLeft = Offset(drawX - hpWidth / 2f, drawY + 25f * scaleY),
                        size = Size(hpWidth, hpHeight)
                    )
                    drawRect(
                        color = if (hpPercent > 0.45f) Color.Green else Color.Red,
                        topLeft = Offset(drawX - hpWidth / 2f, drawY + 25f * scaleY),
                        size = Size(hpWidth * hpPercent, hpHeight)
                    )
                }
            }

            // Draw Flying Projectiles
            engine.projectiles.forEach { proj ->
                val drawX = proj.x * scaleX
                val drawY = proj.y * scaleY

                val layout = getCachedResult(proj.emoji, 24, false)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(drawX - 15f, drawY - 15f)
                )
            }

            // Draw Wacky Particles
            engine.particles.forEach { pt ->
                val drawX = pt.x * scaleX
                val drawY = pt.y * scaleY

                val layout = getCachedResult(pt.emoji, 16, false)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(drawX - 10f, drawY - 10f)
                )
            }

            // Draw Comics bubble popups
            engine.popups.forEach { pop ->
                val drawX = pop.x * scaleX
                val drawY = pop.y * scaleY

                val sizeInt = (15f * pop.scale).roundToInt().coerceAtLeast(8)
                val layout = getCachedResult(pop.text, sizeInt, true)
                val popColor = Color(android.graphics.Color.parseColor(pop.colorHex))
                drawText(
                    textLayoutResult = layout,
                    color = popColor,
                    topLeft = Offset(drawX - 50f, drawY - 15f)
                )
            }
        }

        // --- GAME CONTROL INTERFACES OVERLAY ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // High Scores & Exit Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                            .border(2.5.dp, Color(0xFF0F172A), CircleShape)
                            .clickable { viewModel.exitToMenu() }
                            .testTag("btn_back_to_menu"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                            .border(2.5.dp, Color(0xFF0F172A), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CLOUT: ${viewModel.activeScore}",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                // Arena State / Mode Tag
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp))
                        .border(2.5.dp, Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentMap.name.uppercase(),
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // Joystick & Right action triggers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // LEFT VIRTUAL JOYSTICK (Drag sensitive zone)
                var joystickDragOffset by remember { mutableStateOf(Offset.Zero) }
                val joystickRadius = 55.dp
                val joystickRadiusPx = 140f

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color(0x7F000000), CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    joystickDragOffset = Offset.Zero
                                    viewModel.joystickX = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newX = (joystickDragOffset.x + dragAmount.x).coerceIn(-joystickRadiusPx, joystickRadiusPx)
                                    val newY = (joystickDragOffset.y + dragAmount.y).coerceIn(-joystickRadiusPx, joystickRadiusPx)
                                    joystickDragOffset = Offset(newX, newY)

                                    // Map X offset into joystick movement speed input
                                    viewModel.joystickX = newX / joystickRadiusPx
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Central thumb button
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    joystickDragOffset.x.roundToInt(),
                                    joystickDragOffset.y.roundToInt()
                                )
                            }
                            .size(54.dp)
                            .background(Color(0xFFE91E63), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                // RIGHT COMEDY TRIGGER ACTIONS (Vertical Grid Box)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val activePlayer = engine.players.firstOrNull { it.id == "player" }
                    val isSpecialPowerCharged = activePlayer != null && activePlayer.activeSpecialCooldown <= 0

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Throw slipper button
                        IconButton(
                            onClick = { viewModel.handleAction("throw") },
                            modifier = Modifier
                                .testTag("btn_throw_action")
                                .size(54.dp)
                                .background(Color(0xFF29B6F6), CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Text(text = "📡", fontSize = 24.sp)
                        }

                        // Jump to dodge button
                        IconButton(
                            onClick = { viewModel.handleAction("jump") },
                            modifier = Modifier
                                .testTag("btn_jump_action")
                                .size(54.dp)
                                .background(Color(0xFFFFB300), CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Text(text = "🪶", fontSize = 24.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        // SPECIAL EMOTIONAL DAMAGE/MUMMY POWER ATTACK BUTTON
                        Button(
                            onClick = { viewModel.handleAction("special") },
                            enabled = isSpecialPowerCharged,
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("btn_special_action"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSpecialPowerCharged) Color(0xFFFF1744) else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isSpecialPowerCharged) "🔥 LAUNCH POWER" else "⏳ CHARGING...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // QUICK SLAP MELEE SWING
                        Button(
                            onClick = { viewModel.handleAction("attack") },
                            modifier = Modifier
                                .size(72.dp)
                                .testTag("btn_slap_action"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "👋", fontSize = 24.sp)
                                Text(text = "SLAP", fontWeight = FontWeight.Black, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBackgroundDecoration(map: ArenaMap, layoutSize: Size) {
    // Fill background color
    drawRect(color = Color(android.graphics.Color.parseColor(map.wallColorHex)))

    // Draw comic stylized horizontal floor boundary
    val floorDiv = layoutSize.height - 110f
    drawRect(
        color = Color(0xFF3E2723),
        topLeft = Offset(0f, floorDiv),
        size = Size(layoutSize.width, 110f)
    )

    // Specific maps decorations
    when (map.id) {
        "wedding" -> {
            // Draw yellow gold marigold garland lines
            for (i in 0..10) {
                drawCircle(
                    color = Color(0xFFFFB300),
                    radius = 8f,
                    center = Offset(i * (layoutSize.width / 10), 40f)
                )
            }
        }
        "rooftop" -> {
            // Clothesline representing background
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, 150f),
                end = Offset(layoutSize.width, 150f),
                strokeWidth = 3f
            )
        }
    }
}

private fun DrawScope.drawPlayerPointer(x: Float, y: Float, scaleY: Float) {
    // Small neon downward triangular arrow pointing active player
    val arrowY = y - 75f * scaleY
    drawCircle(
        color = Color(0xFF00FF00),
        radius = 5f,
        center = Offset(x, arrowY)
    )
}

@Composable
fun MatchSummaryView(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ComicCard(
            containerColor = Color.White,
            borderColor = Color(0xFF0F172A),
            shadowOffset = 6.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            // Winner Emoji Highlight
            Text(text = viewModel.matchWinnerEmoji, fontSize = 84.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = viewModel.matchWinnerName,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Rewards breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Clout Gained:", color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${viewModel.activeScore} PTS", color = Color(0xFF3B82F6), fontWeight = FontWeight.Black, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dadi's Blessings Reward:", color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("🪙 +${viewModel.matchCoinsReward} COINS", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 13.sp)
            }

            // Generates extremely hilarious roasting message as bottom comment
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "💬 Roast: \"${GameData.characters.random().voiceLineRoasts.random()}\"",
                    color = Color(0xFF475569),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Match options
        ComicButton(
            onClick = { viewModel.startMatch() },
            containerColor = Color(0xFFEC4899),
            shape = RoundedCornerShape(16.dp),
            shadowOffset = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_rematch")
        ) {
            Text("PLAY REMATCH", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        ComicButton(
            onClick = { viewModel.currentScreen = "MAIN_MENU" },
            containerColor = Color.White,
            borderColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp),
            shadowOffset = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_back_to_main")
        ) {
            Text("MAIN COURT LOBBY", fontWeight = FontWeight.Black, color = Color(0xFF0F172A), fontSize = 14.sp)
        }
    }
}
