package com.example.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Intelligent Physics & Easing system enforcing snappy 150ms–220ms micro-interactions
 * and spatial transform origins anchored directly to trigger coordinates.
 */
object PhysicsMotion {
    // Snappy deceleration curve (fast entry, precise settle, zero sluggish ease-in)
    val SnappyOutEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
    val CrispExitEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    const val MICRO_DURATION_MS = 180
    const val PANEL_DURATION_MS = 220

    fun <T> snappySpring() = spring<T>(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * Spatial popover/dialog enter transition scaling outward directly from the trigger's
     * TransformOrigin rather than floating from arbitrary screen center coordinates.
     */
    fun spatialScaleEnter(origin: TransformOrigin = TransformOrigin(0.5f, 0.9f)): EnterTransition {
        return scaleIn(
            animationSpec = tween(durationMillis = PANEL_DURATION_MS, easing = SnappyOutEasing),
            initialScale = 0.86f,
            transformOrigin = origin
        ) + fadeIn(
            animationSpec = tween(durationMillis = MICRO_DURATION_MS, easing = SnappyOutEasing)
        )
    }

    fun spatialScaleExit(origin: TransformOrigin = TransformOrigin(0.5f, 0.9f)): ExitTransition {
        return scaleOut(
            animationSpec = tween(durationMillis = MICRO_DURATION_MS, easing = CrispExitEasing),
            targetScale = 0.88f,
            transformOrigin = origin
        ) + fadeOut(
            animationSpec = tween(durationMillis = 150, easing = CrispExitEasing)
        )
    }
}

/**
 * Applies fast tactile press & hover scale feedback (180ms snappy curve)
 * while maintaining explicit visual states.
 */
@Composable
fun Modifier.tactilePressScale(
    pressedScale: Float = 0.96f,
    hoveredScale: Float = 1.015f
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetScale = when {
        isPressed -> pressedScale
        isHovered -> hoveredScale
        else -> 1f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = PhysicsMotion.MICRO_DURATION_MS,
            easing = PhysicsMotion.SnappyOutEasing
        ),
        label = "tactileScale"
    )

    this.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}
