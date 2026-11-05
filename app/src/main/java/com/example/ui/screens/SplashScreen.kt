package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemePreference
import com.example.ui.components.ZPrivateLogoVector
import com.example.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay

/**
 * High-craft Animated Splash Screen.
 * - Enlarged 112dp zPrivate logo
 * - Responsive background adaptation: Pure Obsidian (#060B14) in Dark Mode, Crisp White (#FFFFFF) in Light Mode
 * - Emil Kowalski physics: Spring-based entrance scaling (stiffness 320, damping 0.72) and gentle typography fade
 * - Fast, seamless transition into the main application (~1.1s total)
 */
@Composable
fun ZPrivateSplashScreen(
    themePreference: AppThemePreference,
    onSplashFinished: () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        AppThemePreference.DARK -> true
        AppThemePreference.LIGHT -> false
        AppThemePreference.SYSTEM -> systemInDark
    }

    val backgroundColor = if (isDark) Color(0xFF060B14) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var animationStarted by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 320f
        ),
        label = "splashLogoScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "splashContentAlpha"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(1150)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(logoScale)
                .alpha(contentAlpha)
        ) {
            // Enlarged brand logo (112dp) as requested in Feedback 003
            ZPrivateLogoVector(size = 112.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "zPrivate",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "WIRE GUARD • AES-256",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = JetBrainsMono,
                color = Color(0xFF3AA6DD),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Protection de la vie privée",
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor
            )
        }
    }
}
