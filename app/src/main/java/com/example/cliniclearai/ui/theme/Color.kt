package com.example.cliniclearai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// =========================================================================
// CliniClear AI - Modern Clinical Health-Tech Design System (Material 3)
// Aesthetic: Deep Teal (#0D9488), Soft Off-White (#F8FAFC), Pure White (#FFFFFF),
// Deep Slate (#0F172A / #1E293B), and Soft Tinted Semantic Status.
// =========================================================================

// --- 8.dp Grid Spacing & Standard Shapes ---
val GridSpacingSmall = 8.dp
val GridSpacingMedium = 16.dp
val GridSpacingLarge = 24.dp
val StandardCardShape = RoundedCornerShape(16.dp)
val StandardButtonShape = RoundedCornerShape(16.dp)
val StandardPillShape = RoundedCornerShape(50)

// --- Primary Brand Colors (Deep Teal #0D9488 : Confiance, Précision Clinique) ---
val MedicalTealPrimary = Color(0xFF0D9488)           // Deep Teal 600
val MedicalTealDark = Color(0xFF0F766E)              // Teal 700
val MedicalTealLight = Color(0xFF14B8A6)             // Teal 500
val MedicalTealContainerLight = Color(0xFFCCFBF1)    // Teal 100 : Conteneur pastel doux
val MedicalTealOnContainerLight = Color(0xFF115E59)  // Teal 800

val MedicalTealPrimaryDark = Color(0xFF2DD4BF)       // Teal 400
val MedicalTealContainerDark = Color(0xFF134E48)     // Teal 900
val MedicalTealOnContainerDark = Color(0xFFCCFBF1)   // Teal 100

// Alias pour compatibilité ascendante
val MedicalBluePrimary = MedicalTealPrimary
val MedicalBlueDark = MedicalTealDark
val MedicalBlueLight = MedicalTealLight
val MedicalBlueContainerLight = MedicalTealContainerLight
val MedicalBlueOnContainerLight = MedicalTealOnContainerLight
val MedicalBluePrimaryDark = MedicalTealPrimaryDark
val MedicalBlueContainerDark = MedicalTealContainerDark
val MedicalBlueOnContainerDark = MedicalTealOnContainerDark

// --- Secondary Brand Colors (Deep Slate Élégant) ---
val SlateNavySecondary = Color(0xFF334155)          // Slate 700
val SlateNavyLight = Color(0xFF64748B)              // Slate 500
val SlateNavyContainerLight = Color(0xFFF1F5F9)     // Slate 100
val SlateNavyOnContainerLight = Color(0xFF0F172A)   // Slate 900

val SlateNavySecondaryDark = Color(0xFF94A3B8)      // Slate 400
val SlateNavyContainerDark = Color(0xFF1E293B)      // Slate 800
val SlateNavyOnContainerDark = Color(0xFFF8FAFC)    // Slate 50

val SlateIndigoSecondary = SlateNavySecondary
val SlateIndigoLight = SlateNavyLight
val SlateIndigoContainerLight = SlateNavyContainerLight
val SlateIndigoOnContainerLight = SlateNavyOnContainerLight
val SlateIndigoSecondaryDark = SlateNavySecondaryDark
val SlateIndigoContainerDark = SlateNavyContainerDark
val SlateIndigoOnContainerDark = SlateNavyOnContainerDark

// --- Tertiary Brand Colors (Menthe Thérapeutique) ---
val HealingMintTertiary = Color(0xFF059669)         // Emerald 600
val HealingMintContainerLight = Color(0xFFD1FAE5)   // Emerald 100
val HealingMintOnContainerLight = Color(0xFF065F46) // Emerald 800

val HealingMintTertiaryDark = Color(0xFF34D399)
val HealingMintContainerDark = Color(0xFF064E3B)
val HealingMintOnContainerDark = Color(0xFFD1FAE5)

// --- Background & Surface (Thème Clair : Off-White #F8FAFC, Blanc Pur #FFFFFF) ---
val BackgroundLight = Color(0xFFF8FAFC)             // Slate 50 : Fond doux reposant
val SurfaceLight = Color(0xFFFFFFFF)                // Blanc pur pour les cartes
val SurfaceVariantLight = Color(0xFFF1F5F9)         // Slate 100
val OnBackgroundLight = Color(0xFF0F172A)           // Slate 900
val OnSurfaceLight = Color(0xFF0F172A)              // Slate 900
val OnSurfaceVariantLight = Color(0xFF64748B)       // Slate 500
val OutlineLight = Color(0xFFE2E8F0)                // Slate 200 : Bordures crisp 1.dp
val OutlineVariantLight = Color(0xFFF1F5F9)         // Slate 100

// --- Background & Surface (Thème Sombre : Deep Slate #0F172A / #1E293B) ---
val BackgroundDark = Color(0xFF0F172A)              // Slate 900
val SurfaceDark = Color(0xFF1E293B)                 // Slate 800
val SurfaceVariantDark = Color(0xFF334155)          // Slate 700
val OnBackgroundDark = Color(0xFFF8FAFC)            // Slate 50
val OnSurfaceDark = Color(0xFFF8FAFC)               // Slate 50
val OnSurfaceVariantDark = Color(0xFF94A3B8)        // Slate 400
val OutlineDark = Color(0xFF475569)                 // Slate 600
val OutlineVariantDark = Color(0xFF334155)          // Slate 700

// --- Couleurs Sémantiques Médicales Douces et Teintées (Soft Tinted) ---
// 1. Normal / Optimal : #DCFCE7 container / #166534 text
val StatusNormal = Color(0xFF16A34A)                // Vert clinique accent
val StatusNormalContainer = Color(0xFFDCFCE7)       // Vert très doux (container)
val StatusNormalOnContainer = Color(0xFF166534)     // Vert foncé lisible (text)
val StatusNormalText = Color(0xFF166534)

// 2. Modéré / Inférieur : Bleu ciel doux
val StatusLow = Color(0xFF0284C7)
val StatusLowContainer = Color(0xFFE0F2FE)
val StatusLowOnContainer = Color(0xFF075985)
val StatusLowText = Color(0xFF075985)

// 3. Borderline / À surveiller : #FEF3C7 container / #92400E text
val StatusBorderline = Color(0xFFD97706)            // Ambre accent
val StatusBorderlineContainer = Color(0xFFFEF3C7)   // Ambre très doux (container)
val StatusBorderlineText = Color(0xFF92400E)        // Ambre foncé (text)

val StatusWarning = StatusBorderline
val StatusWarningContainer = StatusBorderlineContainer
val StatusWarningOnContainer = StatusBorderlineText
val StatusWarningText = StatusBorderlineText

// 4. High / Alert / Critique : #FEE2E2 container / #991B1B text
val StatusHigh = Color(0xFFDC2626)                  // Rouge corail accent
val StatusHighContainer = Color(0xFFFEE2E2)         // Rouge corail doux (container)
val StatusHighText = Color(0xFF991B1B)              // Rouge foncé (text)

val StatusCritical = StatusHigh
val StatusCriticalContainer = StatusHighContainer
val StatusCriticalOnContainer = StatusHighText
val StatusCriticalText = StatusHighText

// --- Dégradés Health-Tech & Slate ---
val HeroTealGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0D9488), Color(0xFF0F766E))
)

val HeroHeaderGradient = HeroTealGradient
val TealGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0D9488), Color(0xFF14B8A6))
)

val DarkSlateGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
)

val AlertGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFEF4444), Color(0xFFF97316))
)

val CardGlowSurface = Color(0xFFFFFFFF)
