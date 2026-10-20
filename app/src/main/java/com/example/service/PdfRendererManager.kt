package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfRendererManager(private val context: Context) {

    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var currentFilePath: String? = null

    val pageCount: Int
        get() = pdfRenderer?.pageCount ?: 0

    suspend fun openPdf(file: File): Boolean = withContext(Dispatchers.IO) {
        close()
        try {
            if (!file.exists()) return@withContext false
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pfd = parcelFileDescriptor ?: return@withContext false
            pdfRenderer = PdfRenderer(pfd)
            currentFilePath = file.absolutePath
            true
        } catch (e: Exception) {
            Log.e("PdfRendererManager", "Error opening PDF: ${file.absolutePath}", e)
            close()
            false
        }
    }

    suspend fun openPdfFromUri(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val booksDir = File(context.filesDir, "imported_books")
            if (!booksDir.exists()) booksDir.mkdirs()

            val tempFile = File(booksDir, "pdf_${System.currentTimeMillis()}.pdf")
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            if (openPdf(tempFile)) {
                tempFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("PdfRendererManager", "Error copying PDF from URI", e)
            null
        }
    }

    suspend fun renderPage(pageIndex: Int, targetWidth: Int = 1080): ImageBitmap? = withContext(Dispatchers.IO) {
        val renderer = pdfRenderer ?: return@withContext null
        if (pageIndex !in 0 until renderer.pageCount) return@withContext null

        try {
            val page = renderer.openPage(pageIndex)
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt().coerceAtLeast(100)

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            // Render white background first so transparency in PDF doesn't look black
            bitmap.eraseColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.e("PdfRendererManager", "Error rendering page $pageIndex", e)
            null
        }
    }

    /**
     * Generates a sample classic literary PDF for instant reading and verification
     */
    suspend fun createSampleLiteraryPdf(fileName: String, title: String, author: String, pagesText: List<String>): File =
        withContext(Dispatchers.IO) {
            val booksDir = File(context.filesDir, "sample_books")
            if (!booksDir.exists()) booksDir.mkdirs()
            val file = File(booksDir, fileName)
            if (file.exists() && file.length() > 0) return@withContext file

            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard pt
            val pageHeight = 842

            val titlePaint = Paint().apply {
                color = AndroidColor.rgb(40, 40, 45)
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val authorPaint = Paint().apply {
                color = AndroidColor.rgb(120, 100, 80)
                textSize = 14f
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = AndroidColor.rgb(50, 50, 55)
                textSize = 12f
                isAntiAlias = true
            }
            val footerPaint = Paint().apply {
                color = AndroidColor.rgb(160, 160, 160)
                textSize = 10f
                isAntiAlias = true
            }

            pagesText.forEachIndexed { index, text ->
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Elegant book border margin
                canvas.drawColor(AndroidColor.rgb(253, 251, 247)) // Warm Ivory Book Paper

                var y = 80f
                if (index == 0) {
                    canvas.drawText(title, 60f, y, titlePaint)
                    y += 30f
                    canvas.drawText(author, 60f, y, authorPaint)
                    y += 40f
                } else {
                    canvas.drawText(title, 60f, 45f, footerPaint)
                }

                // Split lines
                val words = text.split(" ")
                var line = StringBuilder()
                val maxWidth = pageWidth - 120f

                for (w in words) {
                    val testLine = if (line.isEmpty()) w else "$line $w"
                    if (bodyPaint.measureText(testLine) < maxWidth) {
                        line = StringBuilder(testLine)
                    } else {
                        canvas.drawText(line.toString(), 60f, y, bodyPaint)
                        y += 20f
                        line = StringBuilder(w)
                        if (y > pageHeight - 80f) break
                    }
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line.toString(), 60f, y, bodyPaint)
                }

                // Page number
                val pageNumStr = "— ${index + 1} —"
                val numWidth = footerPaint.measureText(pageNumStr)
                canvas.drawText(pageNumStr, (pageWidth - numWidth) / 2f, pageHeight - 40f, footerPaint)

                pdfDocument.finishPage(page)
            }

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            file
        }

    fun close() {
        try {
            pdfRenderer?.close()
            pdfRenderer = null
            parcelFileDescriptor?.close()
            parcelFileDescriptor = null
            currentFilePath = null
        } catch (e: Exception) {
            Log.e("PdfRendererManager", "Error closing renderer", e)
        }
    }
}
