package com.example.cliniclearai.ui.body_map.ui_elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cliniclearai.R
import com.example.cliniclearai.domain.models.SymptomEntry
import com.example.cliniclearai.ui.body_map.state_holder.BodyMapViewModel
import com.example.cliniclearai.ui.theme.DarkSlateGradient
import com.example.cliniclearai.ui.theme.GridSpacingLarge
import com.example.cliniclearai.ui.theme.GridSpacingMedium
import com.example.cliniclearai.ui.theme.GridSpacingSmall
import com.example.cliniclearai.ui.theme.StandardCardShape
import com.example.cliniclearai.ui.theme.StatusBorderline
import com.example.cliniclearai.ui.theme.StatusBorderlineContainer
import com.example.cliniclearai.ui.theme.StatusBorderlineText
import com.example.cliniclearai.ui.theme.StatusCritical
import com.example.cliniclearai.ui.theme.StatusCriticalContainer
import com.example.cliniclearai.ui.theme.StatusCriticalText
import com.example.cliniclearai.ui.theme.StatusNormal
import com.example.cliniclearai.ui.theme.StatusNormalContainer
import com.example.cliniclearai.ui.theme.StatusNormalText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.hypot

/**
 * Écran Body Map (Symptom Tracker) redessiné selon le Material 3 Health-Tech Aesthetic :
 * - Squelette anatomique vectoriel Canvas dans un conteneur dégradé ardoise foncé (#0F172A à #1E293B) avec grille médicale subtile.
 * - Halos de douleur corail pulsants.
 * - Retour haptique (LocalHapticFeedback) lors de la frappe d'un hotspot.
 * - ModalBottomSheet Material 3 pour régler la sévérité (slider 1–10) et consigner les notes de symptômes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BodyMapScreen(
    viewModel: BodyMapViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val currentError = uiState.errorMessages.firstOrNull()
    LaunchedEffect(currentError) {
        if (currentError != null) {
            val result = snackbarHostState.showSnackbar(
                message = currentError.message,
                actionLabel = "Réessayer",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.saveSymptom()
            }
            viewModel.dismissError(currentError.id)
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
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Suivi des Symptômes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cartographie Anatomique & Douleur",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = GridSpacingMedium, end = GridSpacingMedium, top = GridSpacingSmall, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Conteneur Squelette Anatomique Élégant (#0F172A à #1E293B) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(GridSpacingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // En-tête de la carte anatomique
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = Color(0xFF2DD4BF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Squelette Anatomique",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (uiState.selectedZone.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0D9488).copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { showBottomSheet = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFF2DD4BF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = uiState.selectedZone,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Touchez une articulation ou zone osseuse pour consigner l'intensité de la douleur.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Squelette interactif Canvas avec grille médicale et halos corail pulsants
                        AnatomicalSkeletonCanvas(
                            selectedZone = uiState.selectedZone,
                            symptoms = uiState.symptoms,
                            onZoneTapped = { zone ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onSelectZone(zone)
                                showBottomSheet = true
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Légende sous le squelette
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StatusCritical)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Douleur active (Halo corail)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2DD4BF))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Zone ciblée",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // --- Bouton rapide d'ajout si zone sélectionnée ---
            if (uiState.selectedZone.isNotBlank()) {
                item {
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = StandardCardShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Qualifier la douleur : ${uiState.selectedZone}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- Historique des symptômes consignés (SOAP) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Journal des Douleurs (${uiState.symptoms.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Format SOAP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (uiState.symptoms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = StandardCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune douleur consignée",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Touchez directement le squelette anatomique pour ajouter un symptôme.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.symptoms, key = { it.id }) { symptom ->
                    SymptomCard(
                        symptom = symptom,
                        onDelete = { viewModel.deleteSymptom(symptom.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // --- ModalBottomSheet Material 3 pour l'évaluation de la douleur (Slider 1-10 + Notes) ---
    if (showBottomSheet && uiState.selectedZone.isNotBlank()) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PainSeverityBottomSheetContent(
                selectedZone = uiState.selectedZone,
                intensity = uiState.intensity,
                description = uiState.description,
                associatedFactors = uiState.associatedFactors,
                isSaving = uiState.isSaving,
                onIntensityChange = { viewModel.onIntensityChange(it) },
                onDescriptionChange = { viewModel.onDescriptionChange(it) },
                onAssociatedFactorsChange = { viewModel.onAssociatedFactorsChange(it) },
                onSaveSymptom = {
                    coroutineScope.launch {
                        viewModel.saveSymptom()
                        sheetState.hide()
                        showBottomSheet = false
                    }
                }
            )
        }
    }
}

/**
 * Contenu du ModalBottomSheet Material 3 pour l'évaluation de la douleur (comme sur la maquette écran 3).
 */
@Composable
private fun PainSeverityBottomSheetContent(
    selectedZone: String,
    intensity: Int,
    description: String,
    associatedFactors: String,
    isSaving: Boolean,
    onIntensityChange: (Int) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAssociatedFactorsChange: (String) -> Unit,
    onSaveSymptom: () -> Unit
) {
    val intensityColor = when (intensity) {
        in 1..3 -> StatusNormal
        in 4..6 -> Color(0xFFF97316) // Coral orange
        else -> StatusCritical // Red coral
    }

    val intensityLabel = when (intensity) {
        in 1..3 -> "Douleur Légère (1-3)"
        in 4..6 -> "Douleur Modérée (4-6)"
        in 7..8 -> "Douleur Intense (7-8)"
        else -> "Douleur Sévère / Insupportable (9-10)"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // En-tête : Titre & Zone ciblée
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Sévérité de la Douleur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Zone : $selectedZone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Bulle d'intensité en corail (comme sur la maquette)
            Surface(
                shape = CircleShape,
                color = intensityColor,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$intensity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Libellé de sévérité sous forme de tag
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = intensityColor.copy(alpha = 0.12f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = intensityLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = intensityColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Slider 1-10
        Slider(
            value = intensity.toFloat(),
            onValueChange = { onIntensityChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = intensityColor,
                activeTrackColor = intensityColor,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1 (Min)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("5 (Modéré)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("10 (Max)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Puces de caractéristiques du symptôme (Lancinant, Constant, Brûlure, etc.)
        Text(
            text = "Caractéristique de la douleur :",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val quickTags = listOf("Lancinant", "Constant", "Brûlure", "Aigu", "Diffus", "Par crises")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickTags.forEach { tag ->
                val isSelected = description.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newDesc = if (isSelected) {
                            description.replace(tag, "").trim().trim(',', ' ')
                        } else {
                            if (description.isBlank()) tag else "$description, $tag"
                        }
                        onDescriptionChange(newDesc)
                    },
                    label = { Text(tag) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Champs de notes textuelles
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Notes & sensations (ex: tiraillement, irradiation...)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = associatedFactors,
            onValueChange = onAssociatedFactorsChange,
            label = { Text("Facteurs déclenchants (ex: effort, réveil...)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Bouton Deep Teal pleine largeur "Enregistrer le symptôme"
        Button(
            onClick = onSaveSymptom,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isSaving,
            shape = StandardCardShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enregistrer le symptôme",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Visualisation anatomique interactive avec image du squelette (body_skeleton)
 * centrée dans la carte sombre (#0F172A à #1E293B) et Canvas en superposition
 * pour les points ciblés verts, les halos corail pulsatifs et la détection tactile normalisée.
 */
@Composable
private fun AnatomicalSkeletonCanvas(
    selectedZone: String,
    symptoms: List<SymptomEntry>,
    onZoneTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coralPainColor = Color(0xFFFB7185) // Coral 400

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    data class HotspotDef(
        val zone: String,
        val points: List<Offset>,
        val hitRadiusNorm: Float
    )

    // Coordonnées normalisées (0f..1f) précisément alignées sur les articulations de la silhouette body_skeleton
    val hotspots = remember {
        listOf(
            HotspotDef("Tête & Crâne", listOf(Offset(0.50f, 0.11f)), 0.08f),
            HotspotDef("Gorge & Cou", listOf(Offset(0.50f, 0.18f)), 0.06f),
            HotspotDef("Thorax & Poitrine", listOf(Offset(0.50f, 0.28f)), 0.08f),
            HotspotDef(
                "Épaules & Bras",
                listOf(
                    Offset(0.33f, 0.23f), Offset(0.67f, 0.23f), // Épaules
                    Offset(0.27f, 0.37f), Offset(0.73f, 0.37f), // Coudes
                    Offset(0.20f, 0.50f), Offset(0.80f, 0.50f)  // Poignets
                ),
                0.07f
            ),
            HotspotDef("Dos & Rachis", listOf(Offset(0.50f, 0.38f)), 0.06f),
            HotspotDef("Abdomen & Estomac", listOf(Offset(0.50f, 0.45f)), 0.07f),
            HotspotDef("Bassin & Hanches", listOf(Offset(0.36f, 0.52f), Offset(0.64f, 0.52f)), 0.08f),
            HotspotDef("Jambes & Genoux", listOf(Offset(0.38f, 0.74f), Offset(0.62f, 0.74f)), 0.08f),
            HotspotDef("Chevilles & Pieds", listOf(Offset(0.40f, 0.93f), Offset(0.60f, 0.93f)), 0.08f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSlateGradient)
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Conteneur centré sur l'aspect ratio exact (1536 x 2752) de l'image anatomique
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1536f / 2752f),
            contentAlignment = Alignment.Center
        ) {
            // Image anatomique réaliste du squelette
            Image(
                painter = painterResource(id = R.drawable.body_skeleton),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Canvas interactif en superposition exacte
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val w = size.width
                            val h = size.height

                            var closestZone: String? = null
                            var minDistance = Float.MAX_VALUE

                            for (spot in hotspots) {
                                for (pt in spot.points) {
                                    val targetX = pt.x * w
                                    val targetY = pt.y * h
                                    val dist = hypot(tapOffset.x - targetX, tapOffset.y - targetY)
                                    val maxThreshold = spot.hitRadiusNorm * minOf(w, h) * 1.5f

                                    if (dist < maxThreshold && dist < minDistance) {
                                        minDistance = dist
                                        closestZone = spot.zone
                                    }
                                }
                            }

                            if (closestZone != null) {
                                onZoneTapped(closestZone)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                for (spot in hotspots) {
                    val isSelected = spot.zone == selectedZone
                    val matchingSymptom = symptoms.firstOrNull { it.bodyPart == spot.zone }

                    for (pt in spot.points) {
                        val px = pt.x * w
                        val py = pt.y * h

                        if (matchingSymptom != null) {
                            // Halo corail pulsatif pour douleur active (comme sur la maquette écran 3)
                            drawCircle(
                                color = coralPainColor.copy(alpha = pulseAlpha * 0.35f),
                                radius = 28.dp.toPx(),
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = coralPainColor.copy(alpha = pulseAlpha * 0.55f),
                                radius = 18.dp.toPx(),
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = coralPainColor,
                                radius = 9.dp.toPx(),
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5.dp.toPx(),
                                center = Offset(px, py)
                            )
                        } else if (isSelected) {
                            // Halo turquoise/vert pour la zone ciblée active
                            drawCircle(
                                color = Color(0xFF2DD4BF).copy(alpha = pulseAlpha * 0.35f),
                                radius = 22.dp.toPx(),
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = Color(0xFF2DD4BF),
                                radius = 14.dp.toPx(),
                                center = Offset(px, py),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFF2DD4BF),
                                radius = 7.dp.toPx(),
                                center = Offset(px, py)
                            )
                        } else {
                            // Point vert/cyan ciblé discret sur l'articulation
                            drawCircle(
                                color = Color(0xFF2DD4BF).copy(alpha = 0.55f),
                                radius = 5.dp.toPx(),
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = Color(0xFF0F172A).copy(alpha = 0.60f),
                                radius = 2.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SymptomCard(
    symptom: SymptomEntry,
    onDelete: () -> Unit
) {
    val (statusColor, containerColor, textColor) = when (symptom.intensity) {
        in 1..3 -> Triple(StatusNormal, StatusNormalContainer, StatusNormalText)
        in 4..6 -> Triple(StatusBorderline, StatusBorderlineContainer, StatusBorderlineText)
        else -> Triple(StatusCritical, StatusCriticalContainer, StatusCriticalText)
    }

    val formattedDate = remember(symptom.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(symptom.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = StandardCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GridSpacingMedium),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = symptom.bodyPart,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = containerColor
                    ) {
                        Text(
                            text = "Douleur : ${symptom.intensity}/10",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = symptom.description.ifBlank { "Douleur signalée sans note additionnelle." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (symptom.associatedFactors.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Facteurs : ${symptom.associatedFactors}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer le symptôme",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
