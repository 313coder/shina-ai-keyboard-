package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Professional, high-performance AI Loading and Neural Processing Animations
 * designed specifically for 'SAIPBG' Shina AI (Power by Grazina).
 *
 * Lightweight, pure Compose Canvas implementation with zero external runtime overhead.
 */

// =========================================================================
// 1. FULL-SIZE AI QUANTUM ORB LOADER (Screens, Dialogs, Model Training)
// =========================================================================

@Composable
fun AiQuantumOrbLoader(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    statusText: String? = null,
    showSubtext: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = Color(0xFF00E5FF), // Cyber Cyan
    tertiaryColor: Color = Color(0xFFFFB300)   // Grazina Amber Gold
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_quantum_loader")

    // Rotation 1: Fast clockwise outer ring
    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerAngle"
    )

    // Rotation 2: Counter-clockwise middle orbital arc
    val middleAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "middleAngle"
    )

    // Core pulsing scale
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    // Synaptic glow intensity
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Dynamic AI Status message cycler
    val statusMessages = remember {
        listOf(
            "Grazina Neural Engine active...",
            "Synthesizing Shina linguistic patterns...",
            "Calculating contextual n-grams...",
            "Processing dialect phonetics...",
            "'SAIPBG' Intelligence inference..."
        )
    }
    var currentStatusIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2200)
            currentStatusIndex = (currentStatusIndex + 1) % statusMessages.size
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Background diffused glow
            Box(
                modifier = Modifier
                    .size(size * 0.75f)
                    .scale(corePulse)
                    .alpha(glowAlpha * 0.4f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                secondaryColor.copy(alpha = 0.6f),
                                accentColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Canvas containing multi-layer quantum orbital rings and particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                val baseRadius = (canvasWidth.coerceAtMost(canvasHeight) / 2f) - 10f

                // 1. Outer Gyro Rings with Gradient Sweep
                rotate(degrees = outerAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                accentColor.copy(alpha = 0.1f),
                                secondaryColor,
                                tertiaryColor,
                                accentColor.copy(alpha = 0.1f)
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                        size = Size(baseRadius * 2, baseRadius * 2)
                    )

                    // Orbiting particle on outer rim
                    val rad = Math.toRadians(260.0)
                    val particleX = center.x + (baseRadius * cos(rad)).toFloat()
                    val particleY = center.y + (baseRadius * sin(rad)).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(particleX, particleY)
                    )
                    drawCircle(
                        color = tertiaryColor.copy(alpha = 0.6f),
                        radius = 8.dp.toPx(),
                        center = Offset(particleX, particleY)
                    )
                }

                // 2. Middle Counter-Rotating Arc
                val midRadius = baseRadius * 0.75f
                rotate(degrees = middleAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                tertiaryColor.copy(alpha = 0.3f),
                                secondaryColor,
                                Color.Transparent
                            )
                        ),
                        startAngle = 45f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = Size(midRadius * 2, midRadius * 2)
                    )

                    // Counter particle
                    val midRad = Math.toRadians(45.0 + 180.0)
                    val midPx = center.x + (midRadius * cos(midRad)).toFloat()
                    val midPy = center.y + (midRadius * sin(midRad)).toFloat()
                    drawCircle(
                        color = secondaryColor,
                        radius = 3.5.dp.toPx(),
                        center = Offset(midPx, midPy)
                    )
                }

                // 3. Inner Pulsing Core
                val coreRadius = baseRadius * 0.42f * corePulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            secondaryColor,
                            accentColor
                        ),
                        center = center,
                        radius = coreRadius
                    ),
                    radius = coreRadius,
                    center = center
                )

                // 4. Center icon badge / sparkle
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 3.dp.toPx(),
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Live Animated Status Text
        Text(
            text = statusText ?: statusMessages[currentStatusIndex],
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (showSubtext) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(secondaryColor)
                )
                Text(
                    text = "SAIPBG • Power by Grazina",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Neural Audio Wave Visualizer Bars
            AiWaveVisualizerBars(
                barCount = 7,
                activeColor = secondaryColor,
                modifier = Modifier.height(16.dp)
            )
        }
    }
}

// =========================================================================
// 2. COMPACT AI BUTTON LOADER (Replaces standard CircularProgressIndicator)
// =========================================================================

@Composable
fun AiCompactButtonLoader(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    strokeWidth: Dp = 2.5.dp,
    color: Color = MaterialTheme.colorScheme.onPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_button_loader")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "btnRotation"
    )

    val sweep by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btnSweep"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = (this.size.width.coerceAtMost(this.size.height) / 2f) - (strokeWidth.toPx() / 2f)

        rotate(degrees = rotation, pivot = center) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.6f),
                        color,
                        color.copy(alpha = 0.15f)
                    )
                ),
                startAngle = 0f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Sparkle dot at the head of the rotation
            val rad = Math.toRadians(sweep.toDouble())
            val px = center.x + (radius * cos(rad)).toFloat()
            val py = center.y + (radius * sin(rad)).toFloat()
            drawCircle(
                color = Color.White,
                radius = strokeWidth.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

// =========================================================================
// 3. AI THINKING CHAT BUBBLE (For AiAssistantScreen)
// =========================================================================

@Composable
fun AiThinkingBubble(
    modifier: Modifier = Modifier,
    customText: String = "SAIPBG Shina AI is synthesizing response..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_bubble")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Glowing AI Core Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF00E5FF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SAIPBG Shina AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.height(18.dp)
                    ) {
                        Text(
                            text = "Power by Grazina",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = customText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Wave equalizer indicator
                AiWaveVisualizerBars(
                    barCount = 6,
                    activeColor = Color(0xFF00E5FF),
                    modifier = Modifier.height(12.dp)
                )
            }
        }
    }
}

// =========================================================================
// 4. NEURAL AUDIO/COMPUTE WAVE VISUALIZER BARS
// =========================================================================

@Composable
fun AiWaveVisualizerBars(
    modifier: Modifier = Modifier,
    barCount: Int = 6,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_bars")

    val animValues = List(barCount) { index ->
        val duration = 450 + (index * 110)
        infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animValues.forEachIndexed { idx, animState ->
            val heightFraction = animState.value
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(fraction = heightFraction)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (idx % 2 == 0) activeColor else activeColor.copy(alpha = 0.65f)
                    )
            )
        }
    }
}
