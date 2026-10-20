package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ReaderTheme(
    val title: String,
    val backgroundColor: Color,
    val textColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
) {
    LIGHT(
        title = "Claro",
        backgroundColor = ReaderLightBg,
        textColor = ReaderLightText,
        secondaryColor = Color(0xFF5F6368),
        accentColor = DarkAmber
    ),
    SEPIA(
        title = "Sepia",
        backgroundColor = ReaderSepiaBg,
        textColor = ReaderSepiaText,
        secondaryColor = Color(0xFF7D6B5D),
        accentColor = ReaderSepiaAccent
    ),
    DARK(
        title = "Oscuro",
        backgroundColor = ReaderDarkBg,
        textColor = ReaderDarkText,
        secondaryColor = Color(0xFF9AA0A6),
        accentColor = AmberGold
    ),
    OLED(
        title = "OLED Negro",
        backgroundColor = ReaderOledBg,
        textColor = ReaderOledText,
        secondaryColor = Color(0xFF80868B),
        accentColor = AmberGold
    )
}

enum class PageTurnAnimation(val title: String, val subtitle: String) {
    REAL_CURL_3D("Página Real 3D", "Física de papel realista al deslizar"),
    SMOOTH_SLIDE("Deslizamiento", "Transición horizontal fluida"),
    CONTINUOUS_SCROLL("Desplazamiento Continuo", "Lectura vertical sin cortes")
}

enum class ReaderFont(val title: String) {
    SERIF("Editorial Clásico (Serif)"),
    SANS("Moderno Limpio (Sans)"),
    MONOSPACE("Tipografía Máquina (Mono)"),
    CURSIVE("Manuscrito Literario")
}

data class ReaderSettings(
    val theme: ReaderTheme = ReaderTheme.DARK,
    val pageTurnAnimation: PageTurnAnimation = PageTurnAnimation.REAL_CURL_3D,
    val font: ReaderFont = ReaderFont.SERIF,
    val fontSizeSp: Float = 18f,
    val lineSpacingMultiplier: Float = 1.4f,
    val horizontalMarginDp: Int = 18,
    // Local TTS Voice Settings
    val selectedVoiceName: String = "Automático (Local)",
    val selectedVoiceLocale: String = "es",
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val highlightSentence: Boolean = true
)

data class ConnectedAccount(
    val provider: String,
    val isConnected: Boolean,
    val username: String,
    val email: String,
    val avatarLetter: String,
    val cloudBooksCount: Int,
    val lastSyncTime: String
)
