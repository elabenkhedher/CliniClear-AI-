package com.example.cliniclearai.ui.doctor_brief.ui_elements

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cliniclearai.data.data_sources.local.SoapPdfGenerator
import com.example.cliniclearai.domain.models.DoctorBrief
import com.example.cliniclearai.ui.doctor_brief.state_holder.DoctorBriefViewModel
import com.example.cliniclearai.ui.theme.GridSpacingLarge
import com.example.cliniclearai.ui.theme.GridSpacingMedium
import com.example.cliniclearai.ui.theme.GridSpacingSmall
import com.example.cliniclearai.ui.theme.MedicalTealPrimary
import com.example.cliniclearai.ui.theme.StandardButtonShape
import com.example.cliniclearai.ui.theme.StandardCardShape
import com.example.cliniclearai.ui.theme.StatusBorderline
import com.example.cliniclearai.ui.theme.StatusNormal
import kotlinx.coroutines.launch

/**
 * Écran Brief Médecin (SOAP Doctor Brief) selon le Material 3 Health-Tech Aesthetic :
 * - 4 cartes distinctes SOAP (Subjective, Objective, Assessment, Plan) avec barre d'accent colorée à gauche et badge circulaire d'icône.
 * - Bouton pleine largeur Deep Teal "Share with Doctor" épinglé dans le bottomBar du Scaffold.
 * - Téléchargement direct PDF depuis l'icône TopAppBar (comme sur la maquette écran 4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorBriefScreen(
    viewModel: DoctorBriefViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }

    // Gestion des erreurs via Snackbar
    val currentError = uiState.errorMessages.firstOrNull()
    LaunchedEffect(currentError) {
        if (currentError != null) {
            val result = snackbarHostState.showSnackbar(
                message = currentError.message,
                actionLabel = "Réessayer",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retryLastAction()
            }
            viewModel.dismissError(currentError.id)
        }
    }

    fun sharePdfBrief(brief: DoctorBrief) {
        coroutineScope.launch {
            try {
                isGeneratingPdf = true
                val pdfFile = SoapPdfGenerator.generateSoapPdf(context, brief)
                val shareIntent = SoapPdfGenerator.createSharePdfIntent(context, pdfFile)
                context.startActivity(
                    Intent.createChooser(shareIntent, "Partager la synthèse SOAP avec votre médecin")
                )
            } catch (e: Exception) {
                // Si échec du partage PDF, repli sur le partage texte
                shareDoctorBriefText(context, brief)
            } finally {
                isGeneratingPdf = false
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Synthèse Médicale SOAP",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Synthèse Clinique Pré-Consultation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Régénérer
                    IconButton(
                        onClick = { viewModel.generateBrief() },
                        enabled = !uiState.isGenerating
                    ) {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Régénérer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Télécharger le PDF dans les Téléchargements (icône en haut à droite comme sur la maquette écran 4)
                    if (uiState.brief != null) {
                        IconButton(
                            onClick = {
                                val brief = uiState.brief ?: return@IconButton
                                coroutineScope.launch {
                                    try {
                                        isGeneratingPdf = true
                                        val pdfFile = SoapPdfGenerator.generateSoapPdf(context, brief)
                                        SoapPdfGenerator.savePdfToDownloads(context, pdfFile)
                                        val snackResult = snackbarHostState.showSnackbar(
                                            message = "PDF téléchargé dans Téléchargements/CliniClearAI !",
                                            actionLabel = "Ouvrir",
                                            duration = SnackbarDuration.Long
                                        )
                                        if (snackResult == SnackbarResult.ActionPerformed) {
                                            try {
                                                context.startActivity(SoapPdfGenerator.createOpenPdfIntent(context, pdfFile))
                                            } catch (_: Exception) {}
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            message = "Erreur : ${e.localizedMessage ?: "Erreur de téléchargement"}",
                                            duration = SnackbarDuration.Short
                                        )
                                    } finally {
                                        isGeneratingPdf = false
                                    }
                                }
                            },
                            enabled = !isGeneratingPdf
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Télécharger le PDF",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // --- Bouton Deep Teal pleine largeur "Share with Doctor" épinglé dans le bottomBar ---
        bottomBar = {
            if (uiState.brief != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GridSpacingMedium, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = {
                                uiState.brief?.let { sharePdfBrief(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isGeneratingPdf,
                            shape = StandardButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MedicalTealPrimary
                            )
                        ) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Préparation du document...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Partager avec le médecin",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.brief == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(GridSpacingLarge),
                        shape = StandardCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(GridSpacingLarge)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Préparez votre consultation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Agrège vos bilans biologiques, votre armoire à pharmacie et vos douleurs consignées dans une fiche clinique au format médical international SOAP.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { viewModel.generateBrief() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Générer la fiche SOAP")
                            }
                        }
                    }
                }
            } else {
                val brief = uiState.brief!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = GridSpacingMedium, end = GridSpacingMedium, top = GridSpacingSmall, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Carte d'en-tête clinique
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = StandardCardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(GridSpacingMedium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Fiche Clinique SOAP",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Générée le ${brief.generatedDate} • Prête pour transmission",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 1. S : Subjectif (Notes du patient)
                    item {
                        SoapAccentCard(
                            title = "Subjectif (S)",
                            subtitle = "Notes du patient",
                            content = brief.subjective,
                            accentColor = Color(0xFF0284C7), // Medical Sky Blue
                            cardBgColor = Color(0xFFF0F9FF), // Soft sky tint
                            icon = Icons.Default.Healing
                        )
                    }

                    // 2. O : Objectif (Données cliniques et biologiques)
                    item {
                        SoapAccentCard(
                            title = "Objectif (O)",
                            subtitle = "Données cliniques & constantes",
                            content = brief.objective,
                            accentColor = Color(0xFF10B981), // Emerald green
                            cardBgColor = Color(0xFFF0FDF4), // Soft emerald tint
                            icon = Icons.Default.Science
                        )
                    }

                    // 3. A : Évaluation (Analyse et diagnostic assisté par IA)
                    item {
                        SoapAccentCard(
                            title = "Évaluation (A)",
                            subtitle = "Évaluation & analyse assistée par IA",
                            content = brief.assessment,
                            accentColor = Color(0xFFF59E0B), // Warm amber
                            cardBgColor = Color(0xFFFFFBEB), // Soft amber tint
                            icon = Icons.Default.Psychology
                        )
                    }

                    // 4. P : Plan (Prochaines étapes)
                    item {
                        SoapAccentCard(
                            title = "Plan de soins (P)",
                            subtitle = "Prochaines étapes & conduite à tenir",
                            content = brief.plan,
                            accentColor = MedicalTealPrimary, // Deep Teal
                            cardBgColor = Color(0xFFF0FDFA), // Soft teal tint
                            icon = Icons.AutoMirrored.Filled.Assignment
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Carte SOAP avec barre d'accent colorée à gauche et badge circulaire d'icône,
 * reproduisant la structure de la maquette (écran 4 : SOAP Doctor Brief).
 */
@Composable
private fun SoapAccentCard(
    title: String,
    subtitle: String,
    content: String,
    accentColor: Color,
    cardBgColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = StandardCardShape,
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Barre d'accentuation colorée à gauche
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GridSpacingMedium)
            ) {
                // En-tête : Titre + Sous-titre + Badge circulaire
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.16f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Contenu textuel de la section SOAP
                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lance le sélecteur Android pour partager la fiche clinique au format texte brut.
 */
private fun shareDoctorBriefText(context: Context, brief: DoctorBrief) {
    val shareBody = buildString {
        appendLine("🩺 CLINICLEAR AI — SYNTHÈSE CLINIQUE SOAP")
        appendLine("Date : ${brief.generatedDate}")
        appendLine("=".repeat(40))
        appendLine()
        appendLine("[S - SUBJECTIF]")
        appendLine(brief.subjective)
        appendLine()
        appendLine("[O - OBJECTIF]")
        appendLine(brief.objective)
        appendLine()
        appendLine("[A - ANALYSE & ÉVALUATION]")
        appendLine(brief.assessment)
        appendLine()
        appendLine("[P - PLAN DE SOINS]")
        appendLine(brief.plan)
        appendLine()
        appendLine("=".repeat(40))
        appendLine("Généré par CliniClear AI (Mode Offline-First)")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "CliniClear AI - Synthèse SOAP (${brief.generatedDate})")
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(intent, "Partager la synthèse clinique avec votre médecin"))
}
