package com.example.cliniclearai.ui.report_detail.ui_elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cliniclearai.ui.report_detail.state_holder.ReportDetailViewModel
import com.example.cliniclearai.ui.theme.GridSpacingMedium
import com.example.cliniclearai.ui.theme.GridSpacingSmall
import com.example.cliniclearai.ui.theme.StandardCardShape
import com.example.cliniclearai.ui.theme.StatusCritical
import com.example.cliniclearai.ui.theme.StatusCriticalContainer
import com.example.cliniclearai.ui.theme.StatusCriticalText
import com.example.cliniclearai.ui.theme.StatusNormal
import com.example.cliniclearai.ui.theme.StatusNormalContainer
import com.example.cliniclearai.ui.theme.StatusNormalText

/**
 * Écran détaillé d'un bilan biologique au style Material 3 Health-Tech.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    viewModel: ReportDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.report?.title ?: "Détail du Bilan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        uiState.report?.laboratory?.let { lab ->
                            if (lab.isNotBlank()) {
                                Text(
                                    text = lab,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (uiState.report != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Supprimer ce bilan",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.report == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.report == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bilan biologique introuvable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val report = uiState.report!!
                val abnormalCount = report.biomarkers.count { it.isAbnormal }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = GridSpacingMedium, end = GridSpacingMedium, top = GridSpacingSmall, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Carte En-tête : Informations du laboratoire, date et diagnostic global
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = StandardCardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(GridSpacingMedium)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Science,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = report.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.DateRange,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(13.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${report.date} • ${report.laboratory.ifBlank { "Laboratoire" }}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Badge de diagnostic global (Soft Tinted)
                                if (report.hasAlerts) {
                                    val alertText = when (uiState.explanationLanguage) {
                                        "en" -> "Vigilance: $abnormalCount biomarker(s) outside recommended ranges."
                                        "ar" -> "تنبيه: $abnormalCount مؤشر(ات) خارج المعدلات الموصى بها."
                                        else -> "Vigilance : $abnormalCount biomarqueur(s) hors des bornes recommandées."
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusCriticalContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = StatusCriticalText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = alertText,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusCriticalText
                                            )
                                        }
                                    }
                                } else {
                                    val normalText = when (uiState.explanationLanguage) {
                                        "en" -> "All biomarkers are within standard reference ranges."
                                        "ar" -> "جميع المؤشرات البيولوجية تقع ضمن المعدلات الطبيعية."
                                        else -> "Tous les biomarqueurs sont dans les normes de référence."
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusNormalContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = StatusNormalText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = normalText,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusNormalText
                                            )
                                        }
                                    }
                                }

                                // Synthèse du praticien
                                if (report.doctorSummary.isNotBlank()) {
                                    val summaryTitle = when (uiState.explanationLanguage) {
                                        "en" -> "Medical practitioner summary:"
                                        "ar" -> "التقرير الطبي الموجز:"
                                        else -> "Synthèse médicale du praticien :"
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MedicalServices,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .padding(top = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = summaryTitle,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = report.doctorSummary,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Sélecteur de langue de vulgarisation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val langPrompt = when (uiState.explanationLanguage) {
                                        "en" -> "Language:"
                                        "ar" -> "اللغة:"
                                        else -> "Langue :"
                                    }
                                    Text(
                                        text = langPrompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = uiState.explanationLanguage == "fr",
                                            onClick = { viewModel.onExplanationLanguageChanged("fr") },
                                            label = { Text("Français", style = MaterialTheme.typography.labelSmall) },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        FilterChip(
                                            selected = uiState.explanationLanguage == "en",
                                            onClick = { viewModel.onExplanationLanguageChanged("en") },
                                            label = { Text("English", style = MaterialTheme.typography.labelSmall) },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        FilterChip(
                                            selected = uiState.explanationLanguage == "ar",
                                            onClick = { viewModel.onExplanationLanguageChanged("ar") },
                                            label = { Text("العربية", style = MaterialTheme.typography.labelSmall) },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Barre de filtre "Hors normes uniquement"
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val filterLabel = when (uiState.explanationLanguage) {
                                "en" -> "Out of range only"
                                "ar" -> "المؤشرات غير الطبيعية فقط"
                                else -> "Hors normes uniquement"
                            }
                            val countLabel = when (uiState.explanationLanguage) {
                                "en" -> "${report.biomarkers.size} biomarker(s)"
                                "ar" -> "${report.biomarkers.size} مؤشر(ات)"
                                else -> "${report.biomarkers.size} biomarqueur(s)"
                            }
                            FilterChip(
                                selected = uiState.filterAbnormalOnly,
                                onClick = { viewModel.toggleFilterAbnormalOnly() },
                                label = { Text(filterLabel) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusCriticalContainer,
                                    selectedLabelColor = StatusCriticalText,
                                    selectedLeadingIconColor = StatusCriticalText
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = countLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Liste des biomarqueurs avec la jauge
                    if (report.biomarkers.isEmpty()) {
                        val emptyLabel = when (uiState.explanationLanguage) {
                            "en" -> "No biomarkers match the active filter."
                            "ar" -> "لا توجد مؤشرات مطابقة للتصفية الحالية."
                            else -> "Aucun biomarqueur ne correspond au filtre actif."
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emptyLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(report.biomarkers, key = { it.id }) { biomarker ->
                            BiomarkerGaugeCard(
                                biomarker = biomarker,
                                explanationLanguage = uiState.explanationLanguage
                            )
                        }
                    }

                    // Bouton de suppression en bas de page
                    item {
                        val deleteLabel = when (uiState.explanationLanguage) {
                            "en" -> "Delete this lab report"
                            "ar" -> "حذف هذا التقرير الطبي"
                            else -> "Supprimer ce bilan médical"
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = deleteLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation && uiState.report != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Supprimer ce bilan ?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment supprimer définitivement « ${uiState.report?.title} » ? Cette action effacera l'ensemble de ses ${uiState.report?.biomarkers?.size ?: 0} biomarqueurs de votre dossier.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteReport(onSuccess = onNavigateBack)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
