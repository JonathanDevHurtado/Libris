package com.example.data.model

enum class BookFormat(val displayName: String, val badgeColor: Long) {
    PDF("PDF", 0xFFE53935),
    EPUB("EPUB", 0xFF1E88E5),
    TXT("TXT", 0xFF43A047),
    WATTPAD("Wattpad", 0xFFFF500A),
    CLASSIC("Clásico", 0xFF8E24AA)
}

data class ChapterContent(
    val title: String,
    val paragraphs: List<String>
)

data class BookItem(
    val id: String,
    val title: String,
    val author: String,
    val coverColorHex: Long = 0xFF2C3E50,
    val coverAccentHex: Long = 0xFFD4A373,
    val format: BookFormat,
    val filePath: String? = null,
    val isDownloadedOffline: Boolean = true,
    val totalPages: Int,
    val currentPage: Int = 1,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val category: String = "General",
    val description: String = "",
    val chapters: List<ChapterContent> = emptyList(),
    val totalWords: Int = 12000,
    val readingTimeMinutes: Int = 45,
    val isFavorite: Boolean = false,
    val cloudSynced: Boolean = true
) {
    val progressPercent: Float
        get() = if (totalPages > 0) (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f) else 0f
}

data class ReadingHistoryItem(
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val author: String,
    val pageReached: Int,
    val totalPages: Int,
    val progressPercent: Float,
    val timestamp: Long,
    val durationMinutes: Int,
    val coverColorHex: Long
)
