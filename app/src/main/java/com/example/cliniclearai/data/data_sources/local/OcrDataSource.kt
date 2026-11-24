package com.example.cliniclearai.data.data_sources.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

/**
 * Source de données locale pour la reconnaissance optique de caractères (OCR)
 * via Google ML Kit Text Recognition.
 * Prend en charge les photos/images (JPG, PNG, WebP) et les documents PDF multipages.
 */
@Singleton
class OcrDataSource @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extrait le texte d'un document désigné par son Uri (Image ou PDF).
     */
    suspend fun recognizeTextFromUri(context: Context, documentUri: Uri): String = withContext(Dispatchers.IO) {
        if (isPdfUri(context, documentUri)) {
            recognizeTextFromPdf(context, documentUri)
        } else {
            recognizeTextFromImageUri(context, documentUri)
        }
    }

    private suspend fun recognizeTextFromImageUri(context: Context, imageUri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(reconstructTextByRows(visionText))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    /**
     * Rendu et extraction de texte de chaque page d'un document PDF via PdfRenderer natif d'Android.
     */
    private suspend fun recognizeTextFromPdf(context: Context, pdfUri: Uri): String {
        val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw IllegalArgumentException("Impossible d'ouvrir le fichier PDF")

        val extractedText = StringBuilder()
        pfd.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                val pageCount = renderer.pageCount
                // Limite sécurisée à 10 pages pour éviter l'épuisement mémoire sur les bilans massifs
                val maxPages = minOf(pageCount, 10)
                for (pageIndex in 0 until maxPages) {
                    renderer.openPage(pageIndex).use { page ->
                        // Facteur d'échelle 2x pour une netteté optimale de l'OCR ML Kit
                        val scale = 2
                        val width = (page.width * scale).coerceAtLeast(1)
                        val height = (page.height * scale).coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)

                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        val pageText = recognizeTextFromBitmap(bitmap)
                        if (pageText.isNotBlank()) {
                            extractedText.append(pageText).append("\n\n")
                        }
                        bitmap.recycle()
                    }
                }
            }
        }
        return extractedText.toString().trim()
    }

    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(reconstructTextByRows(visionText))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    /**
     * Reconstruit le texte ligne par ligne à partir des éléments ML Kit pour préserver
     * l'alignement tabulaire des bilans médicaux (au lieu d'une lecture verticale par colonnes).
     * Les éléments ayant un boundingBox.centerY() proche sont regroupés sur la même ligne,
     * triés horizontalement par boundingBox.left et séparés par '|'.
     */
    private fun reconstructTextByRows(visionText: Text): String {
        val elements = visionText.textBlocks
            .flatMap { it.lines }
            .flatMap { it.elements }
            .filter { it.boundingBox != null && it.text.isNotBlank() }

        if (elements.isEmpty()) {
            return visionText.text
        }

        // Tri vertical initial pour regrouper de haut en bas
        val sortedElements = elements.sortedWith(
            compareBy({ it.boundingBox!!.centerY() }, { it.boundingBox!!.left })
        )

        val rows = mutableListOf<MutableList<Text.Element>>()

        for (element in sortedElements) {
            val rect = element.boundingBox!!
            val centerY = rect.centerY()
            val height = rect.height().coerceAtLeast(1)

            // Tolérance relative à la hauteur de ligne (environ 55% de la hauteur)
            val tolerance = height * 0.55f

            // Recherche d'une ligne existante dont le centerY moyen est proche
            val matchingRow = rows.find { row ->
                val rowAvgCenterY = row.map { it.boundingBox!!.centerY() }.average()
                val rowAvgHeight = row.map { it.boundingBox!!.height() }.average().coerceAtLeast(1.0)
                val effectiveTolerance = maxOf(tolerance.toDouble(), rowAvgHeight * 0.55)
                abs(centerY - rowAvgCenterY) <= effectiveTolerance
            }

            if (matchingRow != null) {
                matchingRow.add(element)
            } else {
                rows.add(mutableListOf(element))
            }
        }

        // Tri des lignes de haut en bas selon leur centerY moyen
        val sortedRows = rows.sortedBy { row -> row.map { it.boundingBox!!.centerY() }.average() }

        // Pour chaque ligne : tri de gauche à droite par boundingBox.left et séparation par " | "
        return sortedRows.joinToString("\n") { row ->
            row.sortedBy { it.boundingBox!!.left }
                .joinToString(" | ") { it.text.trim() }
        }
    }

    /**
     * Détermine si l'URI pointe vers un document PDF
     * en vérifiant le type MIME, l'extension et les octets magiques (%PDF).
     */
    private fun isPdfUri(context: Context, uri: Uri): Boolean {
        val mime = context.contentResolver.getType(uri)
        if (mime?.equals("application/pdf", ignoreCase = true) == true) {
            return true
        }
        val path = uri.path?.lowercase() ?: ""
        if (path.endsWith(".pdf")) {
            return true
        }
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val header = ByteArray(4)
                val read = stream.read(header)
                if (read == 4 &&
                    header[0] == 0x25.toByte() && // '%'
                    header[1] == 0x50.toByte() && // 'P'
                    header[2] == 0x44.toByte() && // 'D'
                    header[3] == 0x46.toByte()    // 'F'
                ) {
                    return true
                }
            }
        } catch (_: Exception) {
            // Ignoré
        }
        return false
    }
}
