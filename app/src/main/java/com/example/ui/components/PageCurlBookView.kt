package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.data.model.PageTurnAnimation
import com.example.data.model.ReaderTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PageCurlBookView(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    animationStyle: PageTurnAnimation,
    readerTheme: ReaderTheme,
    isTabletSpread: Boolean = false,
    modifier: Modifier = Modifier,
    pageContent: @Composable (pageIndex: Int, isLeftPage: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Drag offset in pixels: negative = dragging towards next page, positive = dragging towards prev page
    val dragOffset = remember { Animatable(0f) }
    var containerWidthPx by remember { mutableFloatStateOf(1000f) }

    // Sound effect or haptic feel placeholder
    val canGoForward = if (isTabletSpread) (currentPage + 1) < totalPages else currentPage < totalPages
    val canGoBackward = currentPage > 1

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(readerTheme.backgroundColor)
            .pointerInput(currentPage, totalPages, canGoForward, canGoBackward, animationStyle) {
                detectTapGestures(
                    onTap = { offset ->
                        val tapFraction = offset.x / size.width
                        if (tapFraction > 0.75f && canGoForward) {
                            coroutineScope.launch {
                                val target = -size.width.toFloat()
                                dragOffset.animateTo(target, tween(320))
                                onPageChange(if (isTabletSpread) (currentPage + 2).coerceAtMost(totalPages) else currentPage + 1)
                                dragOffset.snapTo(0f)
                            }
                        } else if (tapFraction < 0.25f && canGoBackward) {
                            coroutineScope.launch {
                                val target = size.width.toFloat()
                                dragOffset.animateTo(target, tween(320))
                                onPageChange(if (isTabletSpread) (currentPage - 2).coerceAtLeast(1) else currentPage - 1)
                                dragOffset.snapTo(0f)
                            }
                        }
                    }
                )
            }
            .pointerInput(currentPage, totalPages, canGoForward, canGoBackward, animationStyle) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        val currentDrag = dragOffset.value
                        val threshold = containerWidthPx * 0.22f
                        coroutineScope.launch {
                            if (currentDrag < -threshold && canGoForward) {
                                // Animate completion to next page
                                dragOffset.animateTo(-containerWidthPx, spring(stiffness = Spring.StiffnessMediumLow))
                                onPageChange(if (isTabletSpread) (currentPage + 2).coerceAtMost(totalPages) else currentPage + 1)
                                dragOffset.snapTo(0f)
                            } else if (currentDrag > threshold && canGoBackward) {
                                // Animate completion to prev page
                                dragOffset.animateTo(containerWidthPx, spring(stiffness = Spring.StiffnessMediumLow))
                                onPageChange(if (isTabletSpread) (currentPage - 2).coerceAtLeast(1) else currentPage - 1)
                                dragOffset.snapTo(0f)
                            } else {
                                // Snap back to flat page
                                dragOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val nextVal = dragOffset.value + dragAmount.x
                            // Add resistance if can't go further
                            val clamped = if (nextVal < 0 && !canGoForward) {
                                nextVal * 0.15f
                            } else if (nextVal > 0 && !canGoBackward) {
                                nextVal * 0.15f
                            } else {
                                nextVal
                            }
                            dragOffset.snapTo(clamped)
                        }
                    }
                )
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        LaunchedEffect(widthPx) {
            containerWidthPx = widthPx
        }

        val progress = (dragOffset.value / containerWidthPx).coerceIn(-1f, 1f)
        val isDraggingForward = progress < 0
        val absProgress = abs(progress)

        // Realistic book container with rounded paper corners and subtle book shadow
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTabletSpread) 20.dp else 6.dp)
                .shadow(
                    elevation = if (readerTheme == ReaderTheme.OLED) 0.dp else 12.dp,
                    shape = RoundedCornerShape(8.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(8.dp)),
            color = readerTheme.backgroundColor
        ) {
            if (isTabletSpread) {
                // Dual Page Spread (Tablet Open Hardcover Book Mode)
                TabletTwoPageSpread(
                    leftPageIndex = currentPage,
                    rightPageIndex = (currentPage + 1).coerceAtMost(totalPages),
                    readerTheme = readerTheme,
                    progress = progress,
                    animationStyle = animationStyle,
                    pageContent = pageContent
                )
            } else {
                // Single Page (Mobile Smartphone Mode)
                SinglePageCurlLayout(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    progress = progress,
                    isDraggingForward = isDraggingForward,
                    absProgress = absProgress,
                    animationStyle = animationStyle,
                    readerTheme = readerTheme,
                    pageContent = pageContent
                )
            }
        }
    }
}

@Composable
private fun SinglePageCurlLayout(
    currentPage: Int,
    totalPages: Int,
    progress: Float,
    isDraggingForward: Boolean,
    absProgress: Float,
    animationStyle: PageTurnAnimation,
    readerTheme: ReaderTheme,
    pageContent: @Composable (pageIndex: Int, isLeftPage: Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Underneath Page (The page that will be revealed)
        val underPageIndex = if (isDraggingForward) {
            (currentPage + 1).coerceAtMost(totalPages)
        } else {
            (currentPage - 1).coerceAtLeast(1)
        }

        if (absProgress > 0.001f) {
            Box(modifier = Modifier.fillMaxSize()) {
                pageContent(underPageIndex, false)

                // Shadow cast by turning page onto page underneath
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            if (isDraggingForward) {
                                // Shadow along left side of revealed page
                                val shadowWidth = size.width * (1f - absProgress).coerceAtMost(0.4f)
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.45f * absProgress),
                                            Color.Transparent
                                        ),
                                        startX = 0f,
                                        endX = shadowWidth
                                    )
                                )
                            } else {
                                // Dragging back from left
                                val shadowWidth = size.width * (1f - absProgress).coerceAtMost(0.4f)
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.45f * absProgress)
                                        ),
                                        startX = size.width - shadowWidth,
                                        endX = size.width
                                    )
                                )
                            }
                        }
                )
            }
        }

        // Current Page (Turns and curls)
        val curlAngle = when (animationStyle) {
            PageTurnAnimation.REAL_CURL_3D -> {
                if (isDraggingForward) {
                    -progress * 90f // turns away from right edge
                } else {
                    -progress * 90f // curls from left edge
                }
            }
            PageTurnAnimation.SMOOTH_SLIDE -> 0f
            PageTurnAnimation.CONTINUOUS_SCROLL -> 0f
        }

        val slideTranslationX = when (animationStyle) {
            PageTurnAnimation.SMOOTH_SLIDE -> progress * 1000f
            PageTurnAnimation.REAL_CURL_3D -> 0f
            PageTurnAnimation.CONTINUOUS_SCROLL -> 0f
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (animationStyle == PageTurnAnimation.REAL_CURL_3D) {
                        cameraDistance = 14f * density
                        rotationY = curlAngle
                        // Origin of rotation depends on direction
                        transformOrigin = if (isDraggingForward) {
                            TransformOrigin(0f, 0.5f) // Hinged at left spine
                        } else {
                            TransformOrigin(1f, 0.5f) // Hinged at right edge
                        }
                        alpha = if (absProgress > 0.95f) 0f else 1f
                    } else if (animationStyle == PageTurnAnimation.SMOOTH_SLIDE) {
                        translationX = slideTranslationX
                        alpha = (1f - (absProgress * 0.4f)).coerceIn(0f, 1f)
                    }
                }
                .drawWithContent {
                    drawContent()
                    // Realistic Paper Curl Gradient Shading
                    if (animationStyle == PageTurnAnimation.REAL_CURL_3D && absProgress > 0.01f) {
                        if (isDraggingForward) {
                            // Cylinder curve specular highlight and crease shadow
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.22f * absProgress),
                                        Color.White.copy(alpha = 0.35f * absProgress),
                                        Color.Black.copy(alpha = 0.30f * absProgress)
                                    ),
                                    startX = size.width * (1f - absProgress * 0.8f),
                                    endX = size.width
                                )
                            )
                        } else {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.30f * absProgress),
                                        Color.White.copy(alpha = 0.35f * absProgress),
                                        Color.Black.copy(alpha = 0.22f * absProgress),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = size.width * (absProgress * 0.8f)
                                )
                            )
                        }
                    }
                }
        ) {
            pageContent(currentPage, false)
        }
    }
}

@Composable
private fun TabletTwoPageSpread(
    leftPageIndex: Int,
    rightPageIndex: Int,
    readerTheme: ReaderTheme,
    progress: Float,
    animationStyle: PageTurnAnimation,
    pageContent: @Composable (pageIndex: Int, isLeftPage: Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Page
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            pageContent(leftPageIndex, true)

            // Inner Spine Shadow on right edge of left page
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(36.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (readerTheme == ReaderTheme.OLED) 0.4f else 0.18f)
                            )
                        )
                    )
            )
        }

        // Center Book Spine / Gutter Stitch Line
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(readerTheme.secondaryColor.copy(alpha = 0.25f))
        )

        // Right Page (Turns with Curl Animation)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .graphicsLayer {
                    if (progress < 0) {
                        cameraDistance = 16f * density
                        rotationY = -progress * 90f
                        transformOrigin = TransformOrigin(0f, 0.5f) // Turns from center spine!
                    }
                }
        ) {
            pageContent(rightPageIndex, false)

            // Inner Spine Shadow on left edge of right page
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(36.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = if (readerTheme == ReaderTheme.OLED) 0.4f else 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
