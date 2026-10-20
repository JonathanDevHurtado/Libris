package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.BookRepository
import com.example.service.NarratorManager
import com.example.service.PdfRendererManager
import com.example.ui.components.NarratorControlSheet
import com.example.ui.components.PageCurlBookView
import com.example.ui.components.ReaderCustomizationModal
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    repository: BookRepository,
    narratorManager: NarratorManager,
    pdfRendererManager: PdfRendererManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val books by repository.books.collectAsState()
    val settings by repository.settings.collectAsState()
    val isNarratorPlaying by narratorManager.isPlaying.collectAsState()
    val currentSentenceIndex by narratorManager.currentSentenceIndex.collectAsState()

    val book = books.find { it.id == bookId }
    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Libro no encontrado")
        }
        return
    }

    var currentPage by remember { mutableIntStateOf(book.currentPage) }
    var showControls by remember { mutableStateOf(false) }
    var showNarratorSheet by remember { mutableStateOf(false) }
    var showCustomizationModal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Handle Android system back button
    BackHandler {
        narratorManager.stop()
        repository.updateReadingProgress(book.id, currentPage)
        onBack()
    }

    // Load PDF if book is PDF format
    var pdfLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(book) {
        if (book.format == BookFormat.PDF && book.filePath != null) {
            val file = File(book.filePath)
            if (file.exists()) {
                pdfLoaded = pdfRendererManager.openPdf(file)
            }
        }
    }

    // Save progress periodically or on page change
    LaunchedEffect(currentPage) {
        repository.updateReadingProgress(book.id, currentPage)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(settings.theme.backgroundColor)
    ) {
        val isTablet = maxWidth > 650.dp

        // Main Book Page View with 3D Curl or Slide Animation
        PageCurlBookView(
            currentPage = currentPage,
            totalPages = book.totalPages,
            onPageChange = { newPage ->
                currentPage = newPage
            },
            animationStyle = settings.pageTurnAnimation,
            readerTheme = settings.theme,
            isTabletSpread = isTablet,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    showControls = !showControls
                }
        ) { pageIndex, isLeftPage ->
            PageContentRenderer(
                book = book,
                pageIndex = pageIndex,
                settings = settings,
                isNarratorActive = showNarratorSheet || isNarratorPlaying,
                currentSentenceIndex = currentSentenceIndex,
                pdfRendererManager = pdfRendererManager,
                isPdf = book.format == BookFormat.PDF
            )
        }

        // Top App Bar Controls
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = settings.theme.backgroundColor.copy(alpha = 0.95f),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            narratorManager.stop()
                            repository.updateReadingProgress(book.id, currentPage)
                            onBack()
                        },
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = settings.theme.textColor
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = settings.theme.textColor,
                            maxLines = 1
                        )
                        Text(
                            text = "Página $currentPage de ${book.totalPages}",
                            style = MaterialTheme.typography.bodySmall,
                            color = settings.theme.secondaryColor
                        )
                    }

                    Row {
                        // Narrator Toggle Button
                        IconButton(
                            onClick = {
                                showNarratorSheet = !showNarratorSheet
                                if (showNarratorSheet) {
                                    // Start reading current page paragraphs
                                    val paragraphs = getCurrentPageParagraphs(book, currentPage)
                                    narratorManager.startReading(paragraphs)
                                }
                            },
                            modifier = Modifier.testTag("reader_narrator_toggle")
                        ) {
                            Icon(
                                imageVector = if (isNarratorPlaying) Icons.Default.VolumeUp else Icons.Default.Headphones,
                                contentDescription = "Narrador de voz",
                                tint = if (isNarratorPlaying) MaterialTheme.colorScheme.primary else settings.theme.textColor
                            )
                        }

                        // Appearance / Typography Modal Button
                        IconButton(
                            onClick = { showCustomizationModal = true },
                            modifier = Modifier.testTag("reader_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatColorText,
                                contentDescription = "Ajustes de texto",
                                tint = settings.theme.textColor
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Slider Controls
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = settings.theme.backgroundColor.copy(alpha = 0.95f),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Página $currentPage",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = settings.theme.textColor
                        )
                        Text(
                            text = "${(book.progressPercent * 100).toInt()}% completado",
                            style = MaterialTheme.typography.labelSmall,
                            color = settings.theme.secondaryColor
                        )
                    }

                    Slider(
                        value = currentPage.toFloat(),
                        onValueChange = { currentPage = it.toInt() },
                        valueRange = 1f..book.totalPages.coerceAtLeast(1).toFloat(),
                        steps = (book.totalPages - 2).coerceAtLeast(0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reader_page_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = settings.theme.accentColor,
                            activeTrackColor = settings.theme.accentColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anterior", color = settings.theme.textColor)
                        }

                        TextButton(
                            onClick = { if (currentPage < book.totalPages) currentPage++ },
                            enabled = currentPage < book.totalPages
                        ) {
                            Text("Siguiente", color = settings.theme.textColor)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }

        // Floating Narrator Control Sheet Overlay
        AnimatedVisibility(
            visible = showNarratorSheet,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NarratorControlSheet(
                narratorManager = narratorManager,
                settings = settings,
                onSettingsChange = { newSettings ->
                    repository.updateSettings(newSettings)
                },
                onDismiss = {
                    showNarratorSheet = false
                }
            )
        }
    }

    // Modal Sheet for Font, Theme & Animation Customization
    if (showCustomizationModal) {
        ReaderCustomizationModal(
            settings = settings,
            onSettingsChange = { newSettings ->
                repository.updateSettings(newSettings)
            },
            onDismiss = { showCustomizationModal = false }
        )
    }
}

@Composable
fun PageContentRenderer(
    book: BookItem,
    pageIndex: Int,
    settings: ReaderSettings,
    isNarratorActive: Boolean,
    currentSentenceIndex: Int,
    pdfRendererManager: PdfRendererManager,
    isPdf: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPdf) {
        // PDF Render
        var renderedBitmap by remember(pageIndex) { mutableStateOf<ImageBitmap?>(null) }
        LaunchedEffect(pageIndex) {
            renderedBitmap = pdfRendererManager.renderPage(pageIndex - 1)
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val bmp = renderedBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = "Página $pageIndex",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                CircularProgressIndicator(
                    color = settings.theme.accentColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    } else {
        // Text / Novel / Wattpad / Classic Book Formatted Page
        val paragraphs = remember(book, pageIndex) {
            getCurrentPageParagraphs(book, pageIndex)
        }

        val fontFamily = when (settings.font) {
            ReaderFont.SERIF -> FontFamily.Serif
            ReaderFont.SANS -> FontFamily.SansSerif
            ReaderFont.MONOSPACE -> FontFamily.Monospace
            ReaderFont.CURSIVE -> FontFamily.Cursive
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = settings.horizontalMarginDp.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Book header on page
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = settings.theme.secondaryColor
                    ),
                    maxLines = 1
                )
                Text(
                    text = "Capítulo ${(pageIndex + 1) / 2 + 1}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = settings.theme.secondaryColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Paragraphs with optional drop cap on first paragraph
            paragraphs.forEachIndexed { pIndex, paragraph ->
                val isSentenceHighlighted = isNarratorActive && pIndex == (currentSentenceIndex % paragraphs.size.coerceAtLeast(1))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = (settings.fontSizeSp * 0.35f).dp)
                        .then(
                            if (isSentenceHighlighted) {
                                Modifier
                                    .background(
                                        settings.theme.accentColor.copy(alpha = 0.20f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(6.dp)
                            } else Modifier
                        )
                ) {
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = fontFamily,
                            fontSize = settings.fontSizeSp.sp,
                            lineHeight = (settings.fontSizeSp * settings.lineSpacingMultiplier).sp,
                            color = settings.theme.textColor,
                            textAlign = TextAlign.Justify
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Page Number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "— $pageIndex —",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = settings.theme.secondaryColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

private fun getCurrentPageParagraphs(book: BookItem, page: Int): List<String> {
    if (book.chapters.isEmpty()) {
        return listOf(
            "Página $page de ${book.title}.",
            "Esta historia forma parte de tu biblioteca de lectura offline en Libris.",
            "Continúa explorando capítulos y personalizando el estilo de lectura."
        )
    }

    // Distribute chapters across total pages
    val chapterIndex = ((page - 1) % book.chapters.size).coerceIn(0, book.chapters.size - 1)
    val chapter = book.chapters[chapterIndex]
    return if (chapter.paragraphs.isNotEmpty()) {
        chapter.paragraphs
    } else {
        listOf(
            chapter.title,
            "El contenido de este capítulo está sincronizado y disponible en modo sin conexión."
        )
    }
}
