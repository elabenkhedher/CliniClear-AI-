package com.example.cliniclearai.ui.report_detail.ui_elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cliniclearai.data.data_sources.local.BiomarkerLocalizationHelper
import com.example.cliniclearai.domain.models.Biomarker
import com.example.cliniclearai.domain.models.BiomarkerStatus
import com.example.cliniclearai.ui.theme.GridSpacingMedium
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

/**
 * Carte interactive présentant un biomarqueur clinique selon les exigences du Material 3 Health-Tech :
 * - Jauge à 3 zones en capsule arrondie (StrokeCap.Round) avec curseur animé blanc bordé
 * - Hiérarchie visuelle forte : valeur numérique en gras (headlineSmall), unité atténuée plus petite, pilule de statut teintée douce
 * - Section d'explication IA dépliable avec Modifier.animateContentSize()
 * - Traduction dynamique garantie selon la langue sélectionnée (Français, English, العربية).
 */
@Composable
fun BiomarkerGaugeCard(
    biomarker: Biomarker,
    explanationLanguage: String = "fr",
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Résolution dynamique multilingue (Français, English, العربية)
    val canonicalKey = remember(biomarker.name) {
        BiomarkerLocalizationHelper.findCanonicalKey(biomarker.name)
    }
    val localizedDef = remember(canonicalKey, explanationLanguage) {
        canonicalKey?.let { BiomarkerLocalizationHelper.getBiomarkerDefinition(it, explanationLanguage) }
    }

    val displayName = localizedDef?.name?.ifBlank { biomarker.name } ?: biomarker.name
    val displayVulgarization = localizedDef?.vulgarization?.ifBlank { biomarker.vulgarization } ?: biomarker.vulgarization
    val displayRecommendation = localizedDef?.recommendation?.ifBlank { biomarker.recommendation } ?: biomarker.recommendation

    val statusLabel = when (explanationLanguage) {
        "en" -> when (biomarker.status) {
            BiomarkerStatus.NORMAL -> "Optimal"
            BiomarkerStatus.LOW -> "Insufficiency"
            BiomarkerStatus.HIGH -> "High"
            BiomarkerStatus.CRITICAL -> "Alert"
        }
        "ar" -> when (biomarker.status) {
            BiomarkerStatus.NORMAL -> "طبيعي / مثالي"
            BiomarkerStatus.LOW -> "أقل من المعدل"
            BiomarkerStatus.HIGH -> "مرتفع"
            BiomarkerStatus.CRITICAL -> "حرج / تنبيه"
        }
        else -> when (biomarker.status) {
            BiomarkerStatus.NORMAL -> "Optimal"
            BiomarkerStatus.LOW -> "Insuffisance"
            BiomarkerStatus.HIGH -> "Élevé"
            BiomarkerStatus.CRITICAL -> "Alerte"
        }
    }

    val (statusColor, containerColor, textColor, statusIcon) = when (biomarker.status) {
        BiomarkerStatus.NORMAL -> StatusBundle(
            accent = StatusNormal,
            container = StatusNormalContainer,
            text = StatusNormalText,
            icon = Icons.Default.CheckCircle
        )
        BiomarkerStatus.LOW -> StatusBundle(
            accent = StatusBorderline,
            container = StatusBorderlineContainer,
            text = StatusBorderlineText,
            icon = Icons.Default.Info
        )
        BiomarkerStatus.HIGH -> StatusBundle(
            accent = StatusBorderline,
            container = StatusBorderlineContainer,
            text = StatusBorderlineText,
            icon = Icons.Default.Warning
        )
        BiomarkerStatus.CRITICAL -> StatusBundle(
            accent = StatusCritical,
            container = StatusCriticalContainer,
            text = StatusCriticalText,
            icon = Icons.Default.Error
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = StandardCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GridSpacingMedium)
        ) {
            // Ligne 1 : Titre du biomarqueur et Pilule d'état teintée douce (Soft Tinted Status Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = containerColor,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ligne 2 : Hiérarchie visuelle forte (Grande valeur headlineSmall + unité atténuée plus petite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${biomarker.value}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = biomarker.unit,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val normLabel = when (explanationLanguage) {
                        "en" -> "Ref"
                        "ar" -> "المعدل"
                        else -> "Norme"
                    }
                    Text(
                        text = "$normLabel : ${biomarker.minReference} - ${biomarker.maxReference} ${biomarker.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ligne 3 : Jauge multi-zones en capsule épaisse avec indicateur blanc bordé
            ClinicalBiomarkerCapsuleGauge(
                value = biomarker.value,
                minRef = biomarker.minReference,
                maxRef = biomarker.maxReference,
                status = biomarker.status,
                statusLabel = statusLabel,
                statusColor = textColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ligne 4 : Accordéon d'explication IA avec animateContentSize()
            val aiButtonText = when (explanationLanguage) {
                "en" -> if (isExpanded) "Hide AI analysis" else "Medical explanation & AI vulgarization"
                "ar" -> if (isExpanded) "إخفاء التحليل الطبي" else "التفسير الطبي والتبسيط بالذكاء الاصطناعي"
                else -> if (isExpanded) "Masquer l'analyse IA" else "Explication médicale & vulgarisation IA"
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = aiButtonText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Contenu déplié de l'explication IA
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (displayVulgarization.isNotBlank()) {
                        val roleTitle = when (explanationLanguage) {
                            "en" -> "Role in the body:"
                            "ar" -> "الدور في الجسم:"
                            else -> "Rôle dans l'organisme :"
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
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
                                        text = roleTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = displayVulgarization,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    if (displayRecommendation.isNotBlank()) {
                        val recTitle = when (explanationLanguage) {
                            "en" -> "Patient recommendation:"
                            "ar" -> "توصية للمريض:"
                            else -> "Recommandation patient :"
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusColor.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = recTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = displayRecommendation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Jauge à 3 zones (Optimale, Insuffisance, Élevée) en capsules épaisses arrondies
 * avec curseur indicateur animé à bordure blanche nette et aiguille verticale,
 * correspondant fidèlement à la maquette de l'écran 2 ("Lab Biomarkers").
 */
@Composable
private fun ClinicalBiomarkerCapsuleGauge(
    value: Double,
    minRef: Double,
    maxRef: Double,
    status: BiomarkerStatus,
    statusLabel: String,
    statusColor: Color
) {
    // Calcul de la position normalisée du curseur sur la largeur (0.0 .. 1.0)
    // Zone 1 (Optimal / Vert) : 0f .. 0.33f (centre ~0.16f)
    // Zone 2 (Insuffisance / Jaune) : 0.33f .. 0.66f (centre ~0.50f)
    // Zone 3 (Élevé / Corail) : 0.66f .. 1.0f (centre ~0.83f)
    val targetPosition = remember(value, minRef, maxRef, status) {
        val range = (maxRef - minRef).coerceAtLeast(0.001)
        when (status) {
            BiomarkerStatus.NORMAL -> {
                // Positionné harmonieusement dans la capsule verte
                val normalRatio = ((value - minRef) / range).coerceIn(0.0, 1.0).toFloat()
                0.08f + 0.17f * normalRatio
            }
            BiomarkerStatus.LOW -> {
                // Positionné dans la capsule jaune
                val underRatio = if (minRef > 0) {
                    ((minRef - value) / minRef).coerceIn(0.0, 1.0).toFloat()
                } else 0.5f
                0.56f - 0.14f * underRatio
            }
            BiomarkerStatus.HIGH, BiomarkerStatus.CRITICAL -> {
                // Positionné dans la capsule rose/corail
                val overRatio = if (maxRef > 0) {
                    ((value - maxRef) / (maxRef * 0.5)).coerceIn(0.0, 1.0).toFloat()
                } else 0.5f
                0.74f + 0.18f * overRatio
            }
        }
    }

    val animatedPos by animateFloatAsState(
        targetValue = targetPosition,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "biomarkerGaugeNeedle"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        ) {
            val w = size.width
            val h = size.height

            val capsuleHeight = 12.dp.toPx()
            val capsuleY = (h - capsuleHeight) / 2f
            val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

            val segmentGap = 4.dp.toPx()
            val segmentWidth = (w - 2 * segmentGap) / 3f

            // Zone 1 : Vert menthe / Optimal (A7F3D0)
            drawRoundRect(
                color = Color(0xFFA7F3D0),
                topLeft = Offset(0f, capsuleY),
                size = Size(segmentWidth, capsuleHeight),
                cornerRadius = cornerRadius
            )

            // Zone 2 : Jaune ambre doux / Insuffisance (FDE68A)
            drawRoundRect(
                color = Color(0xFFFDE68A),
                topLeft = Offset(segmentWidth + segmentGap, capsuleY),
                size = Size(segmentWidth, capsuleHeight),
                cornerRadius = cornerRadius
            )

            // Zone 3 : Corail doux / Élevé (FECDD3)
            drawRoundRect(
                color = Color(0xFFFECDD3),
                topLeft = Offset((segmentWidth + segmentGap) * 2f, capsuleY),
                size = Size(segmentWidth, capsuleHeight),
                cornerRadius = cornerRadius
            )

            // Indicateur animé : Aiguille fine et point curseur avec bordure blanche
            val cursorX = w * animatedPos
            val needleHalfHeight = capsuleHeight / 2f + 4.dp.toPx()

            // 1. Aiguille verticale fine dessinée d'abord
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(cursorX, h / 2f - needleHalfHeight),
                end = Offset(cursorX, h / 2f + needleHalfHeight),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 2. Halo d'ombre du curseur
            drawCircle(
                color = Color(0xFF0F172A).copy(alpha = 0.15f),
                radius = 8.dp.toPx(),
                center = Offset(cursorX, h / 2f)
            )

            // 3. Cercle extérieur blanc (bordure blanche nette recouvrant l'aiguille)
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(cursorX, h / 2f)
            )

            // 4. Point noir / slate intérieur
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 4.dp.toPx(),
                center = Offset(cursorX, h / 2f)
            )
        }

        // Libellé de statut aligné sous la zone correspondante (Optimal sous Vert, Insuffisance sous Jaune, Élevé sous Corail)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (status == BiomarkerStatus.NORMAL) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (status == BiomarkerStatus.LOW) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (status == BiomarkerStatus.HIGH || status == BiomarkerStatus.CRITICAL) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

private data class StatusBundle(
    val accent: Color,
    val container: Color,
    val text: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
