package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.ReaderTheme
import com.example.data.repository.BookRepository
import com.example.service.GeminiBookAdvisorService
import com.example.service.NarratorManager
import com.example.service.PdfRendererManager
import com.example.ui.MainApp
import com.example.ui.theme.LibrisTheme

class MainActivity : ComponentActivity() {

    private lateinit var pdfRendererManager: PdfRendererManager
    private lateinit var narratorManager: NarratorManager
    private lateinit var bookRepository: BookRepository
    private lateinit var geminiService: GeminiBookAdvisorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pdfRendererManager = PdfRendererManager(applicationContext)
        narratorManager = NarratorManager(applicationContext)
        bookRepository = BookRepository(applicationContext, pdfRendererManager)
        geminiService = GeminiBookAdvisorService()

        setContent {
            val settings by bookRepository.settings.collectAsState()
            // Default to dark mode as requested by user
            val isDark = when (settings.theme) {
                ReaderTheme.LIGHT, ReaderTheme.SEPIA -> false
                else -> true // Defaults to Dark mode
            }

            LibrisTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainApp(
                        repository = bookRepository,
                        narratorManager = narratorManager,
                        pdfRendererManager = pdfRendererManager,
                        geminiService = geminiService
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        narratorManager.release()
        pdfRendererManager.close()
    }
}
