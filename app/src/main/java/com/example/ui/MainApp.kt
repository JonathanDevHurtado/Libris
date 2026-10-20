package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.repository.BookRepository
import com.example.service.GeminiBookAdvisorService
import com.example.service.NarratorManager
import com.example.service.PdfRendererManager
import com.example.ui.screens.*

enum class AppDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Biblioteca", Icons.Filled.LocalLibrary, Icons.Outlined.LocalLibrary),
    HISTORY("Historial", Icons.Filled.History, Icons.Outlined.History),
    EXPLORE("Explorar", Icons.Filled.Explore, Icons.Outlined.Explore),
    GEMINI("Asistente IA", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    SETTINGS("Ajustes", Icons.Filled.ManageAccounts, Icons.Outlined.ManageAccounts)
}

@Composable
fun MainApp(
    repository: BookRepository,
    narratorManager: NarratorManager,
    pdfRendererManager: PdfRendererManager,
    geminiService: GeminiBookAdvisorService
) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var activeBookId by remember { mutableStateOf<String?>(null) }

    // If a book is open, show the reader full-bleed
    if (activeBookId != null) {
        ReaderScreen(
            bookId = activeBookId!!,
            repository = repository,
            narratorManager = narratorManager,
            pdfRendererManager = pdfRendererManager,
            onBack = { activeBookId = null }
        )
    } else {
        // Adaptive container: NavigationRail on Tablet/Wide, NavigationBar on Phone
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 650.dp

            if (isTablet) {
                // Tablet Layout: Side NavigationRail + Content
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("tablet_navigation_rail"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        header = {
                            Spacer(modifier = Modifier.height(16.dp))
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = "Libris",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    ) {
                        AppDestination.values().forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = { Text(destination.title) },
                                modifier = Modifier.testTag("nav_rail_${destination.name.lowercase()}")
                            )
                        }
                    }

                    // Main Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        AppScreenContent(
                            destination = currentDestination,
                            repository = repository,
                            geminiService = geminiService,
                            onOpenBook = { activeBookId = it },
                            onNavigateToHistory = { currentDestination = AppDestination.HISTORY }
                        )
                    }
                }
            } else {
                // Smartphone Layout: Scaffold with bottom NavigationBar
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("phone_bottom_navigation"),
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            AppDestination.values().forEach { destination ->
                                val isSelected = currentDestination == destination
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = destination.title
                                        )
                                    },
                                    label = { Text(destination.title) },
                                    modifier = Modifier.testTag("bottom_nav_${destination.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        AppScreenContent(
                            destination = currentDestination,
                            repository = repository,
                            geminiService = geminiService,
                            onOpenBook = { activeBookId = it },
                            onNavigateToHistory = { currentDestination = AppDestination.HISTORY }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppScreenContent(
    destination: AppDestination,
    repository: BookRepository,
    geminiService: GeminiBookAdvisorService,
    onOpenBook: (bookId: String) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Crossfade(targetState = destination, label = "screen_transition") { screen ->
        when (screen) {
            AppDestination.HOME -> HomeScreen(
                repository = repository,
                onOpenBook = onOpenBook,
                onNavigateToHistory = onNavigateToHistory
            )
            AppDestination.HISTORY -> HistoryScreen(
                repository = repository,
                onOpenBook = onOpenBook
            )
            AppDestination.EXPLORE -> ExploreScreen(
                repository = repository,
                onOpenBook = onOpenBook
            )
            AppDestination.GEMINI -> GeminiAdvisorScreen(
                repository = repository,
                geminiService = geminiService,
                onOpenBook = onOpenBook
            )
            AppDestination.SETTINGS -> AccountsSyncScreen(
                repository = repository
            )
        }
    }
}
