package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeSplashScreen(
    onSplashComplete: () -> Unit
) {
    val fullTitleText = "Welcome to Shina AI Keyboard"
    val fullSubtitleText = "Preserving & Modernizing the Shina Language with On-Device AI"
    
    var typedTitleText by remember { mutableStateOf("") }
    var typedSubtitleText by remember { mutableStateOf("") }
    var isTitleFinished by remember { mutableStateOf(false) }
    var isSubtitleFinished by remember { mutableStateOf(false) }
    
    // Cursor blinking state
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    // Logo scale transition
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }

    // Typewriter effect logic
    LaunchedEffect(Unit) {
        // 1. Fade & scale in logo
        logoAlpha.animateTo(1f, animationSpec = tween(600))
        logoScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(200)

        // 2. Typewriter animation for title
        for (i in 1..fullTitleText.length) {
            typedTitleText = fullTitleText.substring(0, i)
            delay(50) // typing speed
        }
        isTitleFinished = true
        delay(300)

        // 3. Typewriter animation for subtitle
        for (i in 1..fullSubtitleText.length) {
            typedSubtitleText = fullSubtitleText.substring(0, i)
            delay(30)
        }
        isSubtitleFinished = true

        // 4. Auto-advance after animation completes
        delay(1200)
        onSplashComplete()
    }

    // Shina decorative characters floating background
    val floatingGlyphs = listOf("ش", "ݜ", "ݗ", "ڇ", "ڙ", "ا", "شین", "شینا")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background Ambient Shina Characters
        Box(modifier = Modifier.fillMaxSize()) {
            floatingGlyphs.forEachIndexed { index, glyph ->
                val alphaVal = 0.08f + (index % 3) * 0.05f
                val fontSizeVal = (24 + (index * 6)).sp
                Text(
                    text = glyph,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alphaVal),
                    fontSize = fontSizeVal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(
                            when (index % 6) {
                                0 -> Alignment.TopStart
                                1 -> Alignment.TopEnd
                                2 -> Alignment.CenterStart
                                3 -> Alignment.CenterEnd
                                4 -> Alignment.BottomStart
                                else -> Alignment.BottomEnd
                            }
                        )
                        .padding((20 + (index * 15)).dp)
                )
            }
        }

        // Main Splash Content Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // App Logo Icon Frame
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(3.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.shina_app_icon_logo_1785340309151),
                    contentDescription = "Shina AI App Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Typing Title Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = typedTitleText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 30.sp
                )
                if (!isTitleFinished) {
                    Text(
                        text = "│",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.alpha(cursorAlpha)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Typing Subtitle Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = typedSubtitleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 20.sp
                )
                if (isTitleFinished && !isSubtitleFinished) {
                    Text(
                        text = "│",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.alpha(cursorAlpha)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Typing Key Visual Matrix Shimmer
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("ش", "ی", "ن", "ا").forEachIndexed { idx, char ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (typedTitleText.length > idx * 4) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Skip / Launch Button
            Button(
                onClick = onSplashComplete,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSubtitleFinished) "Open Keyboard Studio" else "Skip",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
