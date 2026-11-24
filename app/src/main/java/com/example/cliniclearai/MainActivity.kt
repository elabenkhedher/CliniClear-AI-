package com.example.cliniclearai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import com.example.cliniclearai.ui.preferences.state_holder.PreferencesViewModel
import com.example.cliniclearai.ui.preferences.ui_elements.PreferencesScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.ui.Screen
import com.example.cliniclearai.ui.body_map.state_holder.BodyMapViewModel
import com.example.cliniclearai.ui.body_map.ui_elements.BodyMapScreen
import com.example.cliniclearai.ui.doctor_brief.state_holder.DoctorBriefViewModel
import com.example.cliniclearai.ui.doctor_brief.ui_elements.DoctorBriefScreen
import com.example.cliniclearai.ui.report_detail.state_holder.ReportDetailViewModel
import com.example.cliniclearai.ui.report_detail.ui_elements.ReportDetailScreen
import com.example.cliniclearai.ui.report_list.state_holder.ReportListViewModel
import com.example.cliniclearai.ui.report_list.ui_elements.ReportListScreen
import com.example.cliniclearai.ui.theme.CliniClearAITheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activité principale de CliniClear AI.
 *
 * Points clés du cours pour le jury :
 * - Point d'entrée Hilt : `@AndroidEntryPoint` injecte les dépendances dans le graphe Compose.
 * - Architecture de navigation Jetpack Compose : Centralisation des routes dans un `NavHost`
 *   piloté par un `NavHostController`.
 * - Séparation stricte : Les ViewModels sont fournis via `hiltViewModel()`, préservant la survie
 *   aux changements de configuration (rotation d'écran).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CliniClearAITheme {
                CliniClearAppNavigation()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun CliniClearAppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.ReportList.route,
            title = "Bilans & Santé",
            selectedIcon = Icons.Filled.Science,
            unselectedIcon = Icons.Outlined.Science
        ),
        BottomNavItem(
            route = Screen.BodyMap.route,
            title = "Carte Douleur",
            selectedIcon = Icons.Filled.HealthAndSafety,
            unselectedIcon = Icons.Outlined.HealthAndSafety
        ),
        BottomNavItem(
            route = Screen.DoctorBrief.route,
            title = "Brief Médecin",
            selectedIcon = Icons.AutoMirrored.Filled.Assignment,
            unselectedIcon = Icons.AutoMirrored.Outlined.Assignment
        ),
        BottomNavItem(
            route = Screen.Preferences.route,
            title = "Préférences",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    // N'afficher la barre de navigation inférieure que sur les écrans racine
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ReportList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Écran Liste des bilans & médicaments
            composable(route = Screen.ReportList.route) {
                val viewModel: ReportListViewModel = hiltViewModel()
                ReportListScreen(
                    viewModel = viewModel,
                    onNavigateToReportDetail = { reportId ->
                        navController.navigate(Screen.ReportDetail.createRoute(reportId))
                    }
                )
            }

            // 2. Écran Détail du bilan médical
            composable(
                route = Screen.ReportDetail.route,
                arguments = listOf(
                    navArgument(Constants.PARAM_REPORT_ID) {
                        type = NavType.StringType
                    }
                )
            ) {
                val viewModel: ReportDetailViewModel = hiltViewModel()
                ReportDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 3. Écran Carte Corporelle (Body Map)
            composable(route = Screen.BodyMap.route) {
                val viewModel: BodyMapViewModel = hiltViewModel()
                BodyMapScreen(viewModel = viewModel)
            }

            // 4. Écran Brief Médecin (Format SOAP)
            composable(route = Screen.DoctorBrief.route) {
                val viewModel: DoctorBriefViewModel = hiltViewModel()
                DoctorBriefScreen(viewModel = viewModel)
            }

            // 5. Écran Préférences & Paramètres
            composable(route = Screen.Preferences.route) {
                val viewModel: PreferencesViewModel = hiltViewModel()
                PreferencesScreen(viewModel = viewModel)
            }
        }
    }
}
