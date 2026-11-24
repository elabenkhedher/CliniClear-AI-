package com.example.cliniclearai.data.data_sources.local

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.cliniclearai.domain.models.DoctorBrief
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Générateur de documents PDF haute fidélité pour le Brief Médecin au format SOAP.
 *
 * Utilise l'API native [PdfDocument] d'Android pour créer un document A4 (595 × 842 pt)
 * médical, épuré, structuré et prêt à être imprimé ou transmis directement au médecin traitant.
 */
object SoapPdfGenerator {

    private const val PAGE_WIDTH = 595 // Largeur standard A4 en points (72 DPI)
    private const val PAGE_HEIGHT = 842 // Hauteur standard A4 en points
    private const val MARGIN_HORIZONTAL = 40f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 45f
    private const val CONTENT_WIDTH = (PAGE_WIDTH - (2 * MARGIN_HORIZONTAL)).toInt()

    // Couleurs de la charte médicale CliniClear AI
    private val COLOR_PRIMARY_TEAL = Color.parseColor("#0D9488") // Deep Teal (Bandeau et titre)
    private val COLOR_ACCENT_BLUE = Color.parseColor("#0284C7")  // Sky Blue
    private val COLOR_DARK_SLATE = Color.parseColor("#0F172A")   // Texte principal
    private val COLOR_MUTED_SLATE = Color.parseColor("#475569")  // Sous-titres et légendes
    private val COLOR_LIGHT_BORDER = Color.parseColor("#E2E8F0") // Lignes séparatrices
    private val COLOR_BG_SECTION = Color.parseColor("#F8FAFC")   // Fond des sections

    // Couleurs spécifiques à chaque axe SOAP
    private val COLOR_SOAP_S = Color.parseColor("#0284C7") // Subjectif (Bleu)
    private val COLOR_SOAP_O = Color.parseColor("#0D9488") // Objectif (Teal)
    private val COLOR_SOAP_A = Color.parseColor("#D97706") // Analyse / Alertes (Ambre)
    private val COLOR_SOAP_P = Color.parseColor("#6366F1") // Plan d'action (Indigo)

    /**
     * Génère le fichier PDF du DoctorBrief de manière asynchrone sur [Dispatchers.IO].
     *
     * @param context Contexte Android pour accéder aux répertoires de cache et au FileProvider.
     * @param brief Modèle immuable du brief médical au format SOAP.
     * @return Le fichier [File] sauvegardé dans le répertoire `cacheDir/briefs/`.
     */
    suspend fun generateSoapPdf(context: Context, brief: DoctorBrief): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()

        // Pinceaux et styles graphiques
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_DARK_SLATE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_PRIMARY_TEAL
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_MUTED_SLATE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val sectionTitlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_LIGHT_BORDER
            strokeWidth = 1f
        }

        val topBarPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_PRIMARY_TEAL
            style = Paint.Style.FILL
        }

        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas

        // Dessin du bandeau supérieur sur la première page
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 7f, topBarPaint)

        var currentY = MARGIN_TOP

        // --- EN-TÊTE DU DOCUMENT ---
        // Titre principal
        canvas.drawText("CliniClear AI — Synthèse de Consultation (SOAP)", MARGIN_HORIZONTAL, currentY + 14f, titlePaint)
        currentY += 28f

        // Sous-titre & Contexte
        canvas.drawText(
            "Fiche clinique préparatoire générée par le patient pour le praticien de santé",
            MARGIN_HORIZONTAL,
            currentY,
            subtitlePaint
        )
        currentY += 15f

        // Métadonnées (Date et identifiant)
        val formattedDate = if (brief.generatedDate.isNotBlank()) brief.generatedDate else {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
        }
        val metaText = "Date de génération : $formattedDate   |   Format standard international SOAP   |   Confidentiel médical"
        subtitlePaint.color = COLOR_MUTED_SLATE
        canvas.drawText(metaText, MARGIN_HORIZONTAL, currentY, subtitlePaint)
        currentY += 12f

        // Ligne séparatrice de l'en-tête
        canvas.drawLine(MARGIN_HORIZONTAL, currentY, PAGE_WIDTH - MARGIN_HORIZONTAL, currentY, linePaint)
        currentY += 18f

        // Données des 4 axes SOAP
        val sections = listOf(
            SoapSectionData(
                code = "S",
                title = "S — SUBJECTIF (Symptômes & ressentis du patient)",
                description = "Plaintes exprimées, intensité des douleurs relevées sur la carte corporelle et facteurs déclenchants.",
                content = brief.subjective,
                accentColor = COLOR_SOAP_S
            ),
            SoapSectionData(
                code = "O",
                title = "O — OBJECTIF (Analyses biologiques & traitements actuels)",
                description = "Biomarqueurs hors normes relevés sur les bilans sanguins récents et ordonnances en cours.",
                content = brief.objective,
                accentColor = COLOR_SOAP_O
            ),
            SoapSectionData(
                code = "A",
                title = "A — ANALYSE (Évaluation des alertes & risques médicamenteux)",
                description = "Synthèse croisée : doublons d'ingrédients actifs, interactions médicamenteuses majeures et rappels FDA.",
                content = brief.assessment,
                accentColor = COLOR_SOAP_A
            ),
            SoapSectionData(
                code = "P",
                title = "P — PLAN (Points clés & questions pour la consultation)",
                description = "Questions préparées par le patient pour structurer l'échange médical et orienter le suivi.",
                content = brief.plan,
                accentColor = COLOR_SOAP_P
            )
        )

        // Dessin des 4 sections SOAP avec pagination dynamique
        for (section in sections) {
            val sectionLayout = createStaticLayout(section.content, textPaint, CONTENT_WIDTH - 24)
            val estimatedBlockHeight = 44f + sectionLayout.height + 16f

            // Vérifier s'il reste assez de place sur la page courante
            if (currentY + estimatedBlockHeight > PAGE_HEIGHT - MARGIN_BOTTOM - 30f) {
                drawFooter(canvas, currentPageNumber)
                document.finishPage(currentPage)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas

                // Bandeau haut sur les pages suivantes
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 4f, topBarPaint)
                currentY = MARGIN_TOP

                // Mini en-tête de rappel sur les pages suivantes
                subtitlePaint.color = COLOR_MUTED_SLATE
                canvas.drawText("CliniClear AI — Synthèse de Consultation (SOAP) [Suite]", MARGIN_HORIZONTAL, currentY, subtitlePaint)
                currentY += 8f
                canvas.drawLine(MARGIN_HORIZONTAL, currentY, PAGE_WIDTH - MARGIN_HORIZONTAL, currentY, linePaint)
                currentY += 16f
            }

            // Dessiner la boîte de section
            val boxTop = currentY
            val boxHeight = 36f + sectionLayout.height + 12f
            val boxRect = RectF(MARGIN_HORIZONTAL, boxTop, PAGE_WIDTH - MARGIN_HORIZONTAL, boxTop + boxHeight)

            // Fond gris doux
            val bgPaint = Paint().apply {
                color = COLOR_BG_SECTION
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(boxRect, 6f, 6f, bgPaint)

            // Bordure latérale colorée (indicateur d'axe SOAP)
            val leftBarPaint = Paint().apply {
                color = section.accentColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(MARGIN_HORIZONTAL, boxTop, MARGIN_HORIZONTAL + 5f, boxTop + boxHeight),
                4f, 4f, leftBarPaint
            )

            // Bordure externe discrète
            val borderPaint = Paint().apply {
                color = COLOR_LIGHT_BORDER
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            canvas.drawRoundRect(boxRect, 6f, 6f, borderPaint)

            // Titre de section
            sectionTitlePaint.color = section.accentColor
            canvas.drawText(section.title, MARGIN_HORIZONTAL + 16f, boxTop + 18f, sectionTitlePaint)

            // Sous-titre explicatif
            subtitlePaint.color = COLOR_MUTED_SLATE
            canvas.drawText(section.description, MARGIN_HORIZONTAL + 16f, boxTop + 30f, subtitlePaint)

            // Corps du texte avec StaticLayout (retours à la ligne automatiques)
            canvas.save()
            canvas.translate(MARGIN_HORIZONTAL + 16f, boxTop + 38f)
            sectionLayout.draw(canvas)
            canvas.restore()

            currentY += boxHeight + 14f
        }

        // Pied de page sur la dernière page
        drawFooter(canvas, currentPageNumber)
        document.finishPage(currentPage)

        // Écriture du fichier dans context.cacheDir/briefs/
        val briefsDir = File(context.cacheDir, "briefs").apply {
            if (!exists()) mkdirs()
        }
        val pdfFile = File(briefsDir, "CliniClear_SOAP_Brief.pdf")
        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        document.close()
        outputStream.flush()
        outputStream.close()

        pdfFile
    }

    /**
     * Construit un Intent de partage natif Android de type application/pdf avec les permissions nécessaires.
     */
    fun createSharePdfIntent(context: Context, pdfFile: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Synthèse de Consultation Médicale — CliniClear AI (SOAP)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Veuillez trouver ci-joint ma synthèse de consultation médicale (format SOAP) préparée avec CliniClear AI."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Sauvegarde le PDF généré dans le répertoire public des Téléchargements (Downloads)
     * via le MediaStore (Android 10+) ou le stockage externe afin qu'il soit directement
     * accessible et téléchargeable dans le gestionnaire de fichiers de l'appareil.
     */
    fun savePdfToDownloads(context: Context, pdfFile: File, displayName: String = "CliniClear_SOAP_Brief.pdf"): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CliniClearAI")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        pdfFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    uri
                } else null
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "CliniClearAI").apply { if (!exists()) mkdirs() }
                val targetFile = File(targetDir, displayName)
                pdfFile.copyTo(targetFile, overwrite = true)
                Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Crée un Intent pour ouvrir et visualiser le document PDF avec l'application par défaut de l'appareil.
     */
    fun createOpenPdfIntent(context: Context, pdfFile: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Dessine le pied de page réglementaire avec mention légale et numéro de page.
     */
    private fun drawFooter(canvas: Canvas, pageNumber: Int) {
        val linePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_LIGHT_BORDER
            strokeWidth = 0.8f
        }

        val footerY = PAGE_HEIGHT - MARGIN_BOTTOM
        canvas.drawLine(MARGIN_HORIZONTAL, footerY, PAGE_WIDTH - MARGIN_HORIZONTAL, footerY, linePaint)

        val legalPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        val disclaimer = "⚠️ Document d'aide à la consultation rédigé pour le praticien. Ne remplace pas un diagnostic ni un avis médical."
        canvas.drawText(disclaimer, MARGIN_HORIZONTAL, footerY + 12f, legalPaint)

        val pageNumberText = "Page $pageNumber"
        val pageNumberWidth = legalPaint.measureText(pageNumberText)
        canvas.drawText(pageNumberText, PAGE_WIDTH - MARGIN_HORIZONTAL - pageNumberWidth, footerY + 12f, legalPaint)
    }

    /**
     * Helper pour créer un [StaticLayout] compatible avec toutes les versions d'Android.
     */
    private fun createStaticLayout(text: CharSequence, paint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1.15f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.15f,
                2f,
                true
            )
        }
    }

    private data class SoapSectionData(
        val code: String,
        val title: String,
        val description: String,
        val content: String,
        val accentColor: Int
    )
}
