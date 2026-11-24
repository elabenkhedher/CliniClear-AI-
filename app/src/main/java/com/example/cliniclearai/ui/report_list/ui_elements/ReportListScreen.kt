package com.example.cliniclearai.ui.report_list.ui_elements

import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cliniclearai.domain.models.Drug
import com.example.cliniclearai.domain.models.DrugAlert
import com.example.cliniclearai.domain.models.LabReport
import com.example.cliniclearai.ui.report_list.state_holder.ReportListTab
import com.example.cliniclearai.ui.report_list.state_holder.ReportListViewModel
import com.example.cliniclearai.ui.theme.GridSpacingLarge
import com.example.cliniclearai.ui.theme.GridSpacingMedium
import com.example.cliniclearai.ui.theme.GridSpacingSmall
import com.example.cliniclearai.ui.theme.HeroTealGradient
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
import java.io.File

/**
 * Dashboard moderne redessiné pour CliniClear AI (Material 3 Health-Tech Aesthetic).
 * Intègre le Health Summary Hero Card, des FilterChips horizontaux défilants,
 * des bannières d'alerte corail/ambre douces et un shimmer loading élégant.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    viewModel: ReportListViewModel,
    onNavigateToReportDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showArchitectureDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showAddDrugDialog by remember { mutableStateOf(false) }
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var prescriptionCameraTempUri by remember { mutableStateOf<Uri?>(null) }
    var userGeminiApiKey by remember { mutableStateOf(com.example.cliniclearai.common.Constants.DEFAULT_GEMINI_API_KEY) }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var pendingCameraAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Demande de permission Caméra au runtime
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val action = pendingCameraAction
            pendingCameraAction = null
            action?.invoke()
        } else {
            pendingCameraAction = null
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "L'autorisation de la caméra est requise pour photographier vos documents.",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // Launcher pour la Galerie (Bilan)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.scanReportFromUri(context, uri, userGeminiApiKey)
        }
    }

    // Launcher pour les Documents PDF (Bilan)
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.scanReportFromUri(context, uri, userGeminiApiKey)
        }
    }

    // Launcher pour la Caméra (Bilan)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraTempUri != null) {
            viewModel.scanReportFromUri(context, cameraTempUri!!, userGeminiApiKey)
        }
    }

    // Launchers pour Ordonnance
    val prescriptionGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.scanPrescriptionFromUri(context, uri, userGeminiApiKey)
        }
    }

    val prescriptionPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.scanPrescriptionFromUri(context, uri, userGeminiApiKey)
        }
    }

    val prescriptionCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && prescriptionCameraTempUri != null) {
            viewModel.scanPrescriptionFromUri(context, prescriptionCameraTempUri!!, userGeminiApiKey)
        }
    }

    fun launchCameraInternal(isPrescription: Boolean) {
        try {
            val imagesDir = File(context.cacheDir, "camera_images").apply { mkdirs() }
            val prefix = if (isPrescription) "rx_scan_" else "lab_scan_"
            val tempFile = File.createTempFile(prefix, ".jpg", imagesDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            if (isPrescription) {
                prescriptionCameraTempUri = uri
                prescriptionCameraLauncher.launch(uri)
            } else {
                cameraTempUri = uri
                cameraLauncher.launch(uri)
            }
        } catch (e: ActivityNotFoundException) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Aucune application caméra disponible sur cet appareil. Utilisez la Galerie ou le PDF.",
                    duration = SnackbarDuration.Long
                )
            }
        } catch (e: Exception) {
            Log.e("CliniClearCamera", "Erreur lors du lancement de l'appareil photo", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Erreur caméra : ${e.localizedMessage ?: "Impossible d'ouvrir l'appareil photo"}",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraInternal(isPrescription = false)
        } else {
            pendingCameraAction = { launchCameraInternal(isPrescription = false) }
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    fun launchPrescriptionCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraInternal(isPrescription = true)
        } else {
            pendingCameraAction = { launchCameraInternal(isPrescription = true) }
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

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

    val activeAlertsCount = uiState.drugAlerts.size + uiState.reports.count { it.hasAlerts }

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
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CliniClear AI",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Tableau de Bord Clinique",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDataConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Supprimer les données de test",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showArchitectureDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Architecture & Soutenance",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.selectedTab == ReportListTab.REPORTS) {
                ExtendedFloatingActionButton(
                    onClick = { showScanDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = StandardCardShape,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(3.dp),
                    icon = {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null)
                        }
                    },
                    text = {
                        Text(
                            text = if (uiState.isScanning) "Analyse en cours..." else "Scanner un Bilan",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { showAddDrugDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = StandardCardShape,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(3.dp),
                    icon = {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    },
                    text = {
                        Text(
                            text = if (uiState.isScanning) "Analyse en cours..." else "Ajouter un Traitement",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Bannière animée de statut d'analyse OCR en cours
            AnimatedVisibility(visible = uiState.isScanning) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GridSpacingMedium, vertical = GridSpacingSmall),
                    shape = StandardCardShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Numérisation & Analyse IA en cours",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Google ML Kit (OCR) + Gemini structurent vos biomarqueurs...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // --- 1. Health Summary Hero Card (Gradient Deep Teal #0D9488) ---
            HealthSummaryHeroCard(
                reportsCount = uiState.reports.size,
                activeAlertsCount = activeAlertsCount,
                lastReportDate = uiState.reports.firstOrNull()?.date ?: "Aujourd'hui, 09:00"
            )

            // --- 2. Barre de recherche épurée ---
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GridSpacingMedium, vertical = 6.dp),
                placeholder = { Text("Rechercher un bilan, hémoglobine, doliprane...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // --- 3. FilterChips horizontaux défilants (All, Alerts, Recent, Bilans, Traitements) ---
            ScrollableFilterChipsBar(
                selectedTab = uiState.selectedTab,
                showOnlyAlerts = uiState.showOnlyAlerts,
                onSelectAll = {
                    viewModel.onToggleShowOnlyAlerts(false)
                    viewModel.onTabSelected(ReportListTab.REPORTS)
                },
                onSelectAlerts = {
                    viewModel.onToggleShowOnlyAlerts(true)
                },
                onSelectRecent = {
                    viewModel.onSearchQueryChanged("")
                },
                onSelectReports = {
                    viewModel.onToggleShowOnlyAlerts(false)
                    viewModel.onTabSelected(ReportListTab.REPORTS)
                },
                onSelectDrugs = {
                    viewModel.onTabSelected(ReportListTab.DRUGS_AND_ALERTS)
                }
            )

            // --- 4. Bannière d'alerte interactions/rappels si présente (Soft Coral/Amber) ---
            if (uiState.drugAlerts.isNotEmpty() && uiState.selectedTab == ReportListTab.REPORTS) {
                SoftAlertBanner(
                    alertCount = uiState.drugAlerts.size,
                    firstAlert = uiState.drugAlerts.first(),
                    onClick = { viewModel.onTabSelected(ReportListTab.DRUGS_AND_ALERTS) },
                    modifier = Modifier.padding(horizontal = GridSpacingMedium, vertical = 6.dp)
                )
            }

            // Onglets principaux
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.selectedTab == ReportListTab.REPORTS,
                    onClick = { viewModel.onTabSelected(ReportListTab.REPORTS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bilans Biologiques",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (uiState.selectedTab == ReportListTab.REPORTS) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
                Tab(
                    selected = uiState.selectedTab == ReportListTab.DRUGS_AND_ALERTS,
                    onClick = { viewModel.onTabSelected(ReportListTab.DRUGS_AND_ALERTS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Traitements & Alertes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (uiState.selectedTab == ReportListTab.DRUGS_AND_ALERTS) FontWeight.Bold else FontWeight.Normal
                            )
                            val alertCount = uiState.drugAlerts.size
                            if (alertCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = StatusCritical,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$alertCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // Contenu principal : Shimmer loading ou données réelles
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isLoading || uiState.isScanning) {
                    ShimmerReportPlaceholderList()
                } else {
                    when (uiState.selectedTab) {
                        ReportListTab.REPORTS -> {
                            ReportsContent(
                                reports = uiState.reports,
                                onReportClick = onNavigateToReportDetail,
                                onDeleteReport = { viewModel.deleteLabReport(it) },
                                onScanClick = { showScanDialog = true }
                            )
                        }
                        ReportListTab.DRUGS_AND_ALERTS -> {
                            DrugsAndAlertsContent(
                                drugs = uiState.drugs,
                                alerts = uiState.drugAlerts,
                                onDeleteDrug = { viewModel.deleteDrug(it) },
                                onAddClick = { showAddDrugDialog = true },
                                onSyncClick = { viewModel.checkDrugRecallsOnline() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogue de confirmation de suppression des données de test
    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
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
                    text = "Supprimer les données de test ?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Voulez-vous supprimer les bilans et médicaments de test par défaut pour repartir d'une base propre ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearTestData()
                        showClearDataConfirm = false
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
                OutlinedButton(
                    onClick = { showClearDataConfirm = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialogue de numérisation (Caméra / PDF / Galerie)
    if (showScanDialog) {
        ScanReportDialog(
            onCameraClick = {
                showScanDialog = false
                launchCamera()
            },
            onPdfClick = {
                showScanDialog = false
                pdfLauncher.launch(arrayOf("application/pdf"))
            },
            onGalleryClick = {
                showScanDialog = false
                galleryLauncher.launch("image/*")
            },
            onDismiss = { showScanDialog = false }
        )
    }

    // Dialogue d'ajout ou de numérisation d'ordonnance
    if (showAddDrugDialog) {
        AddOrScanDrugDialog(
            onAddManualDrug = { drug ->
                viewModel.addDrug(drug)
            },
            onCameraClick = {
                showAddDrugDialog = false
                launchPrescriptionCamera()
            },
            onPdfClick = {
                showAddDrugDialog = false
                prescriptionPdfLauncher.launch(arrayOf("application/pdf"))
            },
            onGalleryClick = {
                showAddDrugDialog = false
                prescriptionGalleryLauncher.launch("image/*")
            },
            onDismiss = { showAddDrugDialog = false }
        )
    }

    // Dialogue de confirmation de succès après scan
    uiState.lastScannedReport?.let { scannedReport ->
        AlertDialog(
            onDismissRequest = { viewModel.clearLastScannedReport() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusNormal,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Bilan Analysé avec Succès !",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${scannedReport.title}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Laboratoire : ${scannedReport.laboratory} • Date : ${scannedReport.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${scannedReport.biomarkers.size} biomarqueurs extraits et vulgarisés avec succès par l'IA.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    if (scannedReport.hasAlerts) {
                        Text(
                            text = "⚠️ Alertes détectées sur certains marqueurs hors norme.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusCritical,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reportId = scannedReport.id
                        viewModel.clearLastScannedReport()
                        onNavigateToReportDetail(reportId)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Consulter le bilan")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearLastScannedReport() }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Dialogue de confirmation de succès après scan d'ordonnance
    uiState.lastScannedPrescriptionDrugs?.let { scannedDrugs ->
        AlertDialog(
            onDismissRequest = { viewModel.clearLastScannedPrescription() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusNormal,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Ordonnance Analysée avec Succès !",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${scannedDrugs.size} médicament(s) identifié(s) et enregistré(s) dans votre armoire à pharmacie :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    scannedDrugs.forEach { drug ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "• ${drug.brandName} (${drug.dosage.ifBlank { drug.genericName }})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (drug.frequency.isNotBlank()) {
                                    Text(
                                        text = "   Posologie : ${drug.frequency}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearLastScannedPrescription() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Voir l'armoire à pharmacie")
                }
            }
        )
    }

    if (showArchitectureDialog) {
        ArchitectureExplainingDialog(onDismiss = { showArchitectureDialog = false })
    }
}

/**
 * Health Summary Hero Card au style Health-Tech épuré (Deep Teal Gradient #0D9488).
 */
@Composable
private fun HealthSummaryHeroCard(
    reportsCount: Int,
    activeAlertsCount: Int,
    lastReportDate: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GridSpacingMedium, vertical = 6.dp),
        shape = StandardCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroTealGradient)
        ) {
            // Courbe médicale ondulée d'arrière-plan en Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.CenterEnd)
            ) {
                val w = size.width
                val h = size.height
                val wavePath = Path().apply {
                    moveTo(w * 0.45f, h * 0.65f)
                    cubicTo(
                        w * 0.60f, h * 0.25f,
                        w * 0.75f, h * 0.95f,
                        w, h * 0.40f
                    )
                }
                drawPath(
                    path = wavePath,
                    color = Color.White.copy(alpha = 0.22f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Deuxième onde plus douce
                val wavePath2 = Path().apply {
                    moveTo(w * 0.50f, h * 0.75f)
                    cubicTo(
                        w * 0.68f, h * 0.40f,
                        w * 0.82f, h * 0.85f,
                        w, h * 0.55f
                    )
                }
                drawPath(
                    path = wavePath2,
                    color = Color.White.copy(alpha = 0.10f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GridSpacingMedium)
            ) {
                // Ligne supérieure : Étiquette Health Summary & Indicateur
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RÉSUMÉ SANTÉ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.20f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (activeAlertsCount == 0) Color(0xFF4ADE80) else Color(0xFFFCA5A5))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (activeAlertsCount == 0) "Optimal" else "Vigilance",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Titre principal
                Text(
                    text = if (activeAlertsCount == 0) "Tout est sous contrôle" else "$activeAlertsCount Alerte(s) active(s)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Dernière mise à jour : $lastReportDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Métriques sous forme de pilules claires (8dp grid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GridSpacingSmall)
                ) {
                    HeroMetricPill(
                        icon = Icons.Default.Science,
                        label = "Bilans analysés",
                        value = "$reportsCount"
                    )

                    HeroMetricPill(
                        icon = if (activeAlertsCount == 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                        label = "Alertes actives",
                        value = "$activeAlertsCount",
                        isAlert = activeAlertsCount > 0
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Points de pagination sous la carte (comme sur la maquette)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isAlert: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAlert) Color(0xFFFCA5A5) else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Barre de FilterChips horizontaux scrollables (All, Alerts, Recent, Bilans, Traitements).
 */
@Composable
private fun ScrollableFilterChipsBar(
    selectedTab: ReportListTab,
    showOnlyAlerts: Boolean,
    onSelectAll: () -> Unit,
    onSelectAlerts: () -> Unit,
    onSelectRecent: () -> Unit,
    onSelectReports: () -> Unit,
    onSelectDrugs: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = GridSpacingMedium, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(GridSpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isAllSelected = selectedTab == ReportListTab.REPORTS && !showOnlyAlerts

        FilterChip(
            selected = isAllSelected,
            onClick = onSelectAll,
            label = { Text("Tous") },
            shape = RoundedCornerShape(12.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        FilterChip(
            selected = showOnlyAlerts,
            onClick = onSelectAlerts,
            label = { Text("Alertes") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Warning,
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

        FilterChip(
            selected = selectedTab == ReportListTab.REPORTS && !showOnlyAlerts,
            onClick = onSelectReports,
            label = { Text("Bilans Labo") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            shape = RoundedCornerShape(12.dp)
        )

        FilterChip(
            selected = selectedTab == ReportListTab.DRUGS_AND_ALERTS,
            onClick = onSelectDrugs,
            label = { Text("Traitements") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            shape = RoundedCornerShape(12.dp)
        )

        FilterChip(
            selected = false,
            onClick = onSelectRecent,
            label = { Text("Récents") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Bannière d'alerte interactions/rappels OpenFDA douce teintée corail/ambre avec icône d'alerte en tête.
 */
@Composable
private fun SoftAlertBanner(
    alertCount: Int,
    firstAlert: DrugAlert,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = StandardCardShape,
        colors = CardDefaults.cardColors(
            containerColor = StatusBorderlineContainer
        ),
        border = BorderStroke(1.dp, StatusBorderline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = StatusBorderline,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vigilance Sanitaire ($alertCount rappel/interaction)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = StatusBorderlineText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = firstAlert.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusBorderlineText.copy(alpha = 0.9f),
                    maxLines = 1
                )
            }
            TextButton(onClick = onClick) {
                Text(
                    text = "Détails",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = StatusBorderlineText
                )
            }
        }
    }
}

/**
 * Placeholder Shimmer élégant remplaçant le bare spinner pendant l'analyse ou le chargement.
 */
@Composable
private fun ShimmerReportPlaceholderList() {
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerAnim"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = GridSpacingMedium, vertical = GridSpacingSmall),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = StandardCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(GridSpacingMedium)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(shimmerBrush)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(shimmerBrush)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(shimmerBrush)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(shimmerBrush)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanReportDialog(
    onCameraClick: () -> Unit,
    onPdfClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Scanner un Bilan Médical",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Numérisez vos analyses papier (photo) ou vos documents PDF de laboratoire avec Google ML Kit (OCR) puis vulgarisez les biomarqueurs grâce à l'IA.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ScanActionOptionCard(
                    title = "Prendre une photo",
                    subtitle = "Scanner avec l'appareil photo",
                    icon = Icons.Default.CameraAlt,
                    onClick = onCameraClick
                )

                ScanActionOptionCard(
                    title = "Document PDF (Laboratoire)",
                    subtitle = "Importer un fichier PDF d'analyses",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = onPdfClick
                )

                ScanActionOptionCard(
                    title = "Choisir une image",
                    subtitle = "Importer depuis la galerie",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = onGalleryClick
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ScanActionOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isProminent: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isProminent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = if (isProminent) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isProminent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isProminent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
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
    }
}

@Composable
private fun ReportsContent(
    reports: List<LabReport>,
    onReportClick: (String) -> Unit,
    onDeleteReport: (String) -> Unit,
    onScanClick: () -> Unit
) {
    if (reports.isEmpty()) {
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
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucun bilan biologique",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Numérisez une analyse sanguine ou testez avec un bilan de démonstration.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onScanClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tester ou Scanner un bilan")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = GridSpacingMedium, end = GridSpacingMedium, top = GridSpacingSmall, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bilans Récents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${reports.size} bilan(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            items(reports, key = { it.id }) { report ->
                ReportListItem(
                    report = report,
                    onReportClick = onReportClick,
                    onDeleteReport = onDeleteReport
                )
            }
        }
    }
}

@Composable
private fun DrugsAndAlertsContent(
    drugs: List<Drug>,
    alerts: List<DrugAlert>,
    onDeleteDrug: (String) -> Unit,
    onAddClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = GridSpacingMedium, end = GridSpacingMedium, top = GridSpacingSmall, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (alerts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusCritical,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alertes Sanitaires & Rappels OpenFDA (${alerts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusCriticalText
                    )
                }
            }
            items(alerts, key = { it.id }) { alert ->
                DrugAlertCard(alert = alert)
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Armoire à Pharmacie (${drugs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onAddClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onSyncClick) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vérifier rappels", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (drugs.isEmpty()) {
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
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aucun médicament enregistré",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Ajoutez vos traitements manuellement ou numérisez une ordonnance par photo/PDF.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter ou Scanner une ordonnance")
                        }
                    }
                }
            }
        } else {
            items(drugs, key = { it.id }) { drug ->
                DrugCard(
                    drug = drug,
                    onDelete = { onDeleteDrug(drug.id) }
                )
            }
        }
    }
}

@Composable
private fun DrugAlertCard(alert: DrugAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = StandardCardShape,
        colors = CardDefaults.cardColors(
            containerColor = StatusCriticalContainer
        ),
        border = BorderStroke(1.dp, StatusCritical.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = StatusCritical,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = StatusCriticalText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DrugCard(
    drug: Drug,
    onDelete: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = drug.brandName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = drug.genericName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (drug.hasActiveRecall) {
                        Surface(
                            color = StatusCritical,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "RAPPEL SANITAIRE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Supprimer le médicament",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Posologie : ${drug.dosage.ifBlank { "Non renseignée" }} • ${drug.frequency.ifBlank { "Selon prescription" }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isExpanded) "Masquer les détails" else "Détails des substances & mises en garde",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Substances actives :",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = drug.activeIngredients.joinToString(", ").ifBlank { "Non spécifié" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (drug.warnings.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusCriticalContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Mises en garde :",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusCriticalText
                                )
                                Text(
                                    text = drug.warnings,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Supprimer ce médicament ?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Voulez-vous supprimer « ${drug.brandName} » de vos traitements enregistrés ?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
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
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun ArchitectureExplainingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Points Clés du Cours (Défense Jury)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ArchPoint(
                    title = "OCR & IA Hybride (Photos & PDF)",
                    desc = "Google ML Kit extrait le texte offline des photos et documents PDF via PdfRenderer natif d'Android, puis Gemini API structure et vulgarise les biomarqueurs sauvegardés dans Room (SSOT)."
                )
                ArchPoint(
                    title = "Single Source of Truth (SSOT)",
                    desc = "Room est l'unique source de vérité. L'UI réagit directement aux requêtes Flow exposées par le DAO."
                )
                ArchPoint(
                    title = "Unidirectional Data Flow (UDF)",
                    desc = "L'état immuable (StateFlow) descend du ViewModel vers les Composables. Les événements utilisateur remontent."
                )
                ArchPoint(
                    title = "Mutex Cache & Concurrence",
                    desc = "Le Mutex (`withLock`) sécurise la mémoire cache du Repository contre les accès concurrents sans bloquer le thread UI."
                )
                ArchPoint(
                    title = "Survie aux Changements de Configuration",
                    desc = "`stateIn(WhileSubscribed(5000))` préserve le flux actif 5 secondes lors d'une rotation d'écran pour économiser la bande passante."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Compris")
            }
        }
    )
}

@Composable
private fun ArchPoint(title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "• $title",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class QuickDrugTemplate(
    val brandName: String,
    val genericName: String,
    val activeIngredients: List<String>,
    val dosage: String,
    val frequency: String,
    val instructions: String,
    val purpose: String,
    val warnings: String,
    val badgeText: String
)

@Composable
private fun AddOrScanDrugDialog(
    onAddManualDrug: (Drug) -> Unit,
    onCameraClick: () -> Unit,
    onPdfClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    var brandName by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var warnings by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Ajouter un Traitement",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ajout Rapide", style = MaterialTheme.typography.labelSmall) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Scanner Ordonnance", style = MaterialTheme.typography.labelSmall) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Saisie Manuelle", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        Text(
                            text = "Ajoutez un médicament en 1 clic pour tester les alertes d'interactions et de surdosage :",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val templates = listOf(
                            QuickDrugTemplate(
                                brandName = "Advil 400 mg",
                                genericName = "Ibuprofène",
                                activeIngredients = listOf("ibuprofen"),
                                dosage = "400 mg",
                                frequency = "1 à 3 comprimés/jour si douleur",
                                instructions = "Prendre impérativement au cours d'un repas.",
                                purpose = "Anti-inflammatoire non stéroïdien (AINS)",
                                warnings = "Risque d'hémorragie si associé à un anticoagulant (Aspirine/Kardégic).",
                                badgeText = "Alerte AINS + Anticoagulant"
                            ),
                            QuickDrugTemplate(
                                brandName = "Kardégic 75 mg",
                                genericName = "Acide acétylsalicylique",
                                activeIngredients = listOf("acide acetylsalicylique", "aspirin"),
                                dosage = "75 mg",
                                frequency = "1 sachet par jour au déjeuner",
                                instructions = "Dissoudre dans un verre d'eau.",
                                purpose = "Antiagrégant plaquettaire préventif",
                                warnings = "Majore le risque de saignement avec les AINS.",
                                badgeText = "Antiagrégant Cardiovasculaire"
                            ),
                            QuickDrugTemplate(
                                brandName = "Dafalgan 1000 mg",
                                genericName = "Paracétamol",
                                activeIngredients = listOf("paracetamol"),
                                dosage = "1000 mg",
                                frequency = "1 comprimé toutes les 6 heures si besoin",
                                instructions = "Espacer les prises d'au moins 4 heures.",
                                purpose = "Antalgique et antipyrétique",
                                warnings = "Ne pas associer à Doliprane (risque toxicité hépatique).",
                                badgeText = "Alerte Surdosage Paracétamol"
                            ),
                            QuickDrugTemplate(
                                brandName = "Metformine 850 mg",
                                genericName = "Metformine",
                                activeIngredients = listOf("metformin"),
                                dosage = "850 mg",
                                frequency = "1 comprimé matin et soir",
                                instructions = "Prendre au milieu des repas.",
                                purpose = "Antidiabétique oral régulateur de glycémie",
                                warnings = "Arrêter en cas d'insuffisance rénale aiguë.",
                                badgeText = "Traitement Diabète"
                            ),
                            QuickDrugTemplate(
                                brandName = "Aldactone 25 mg",
                                genericName = "Spironolactone",
                                activeIngredients = listOf("spironolactone"),
                                dosage = "25 mg",
                                frequency = "1 comprimé le matin",
                                instructions = "Prendre de préférence le matin.",
                                purpose = "Diurétique épargneur de potassium",
                                warnings = "Risque d'hyperkaliémie avec IEC/Sartans.",
                                badgeText = "Alerte Hyperkaliémie"
                            ),
                            QuickDrugTemplate(
                                brandName = "Xanax 0.25 mg",
                                genericName = "Alprazolam",
                                activeIngredients = listOf("alprazolam"),
                                dosage = "0.25 mg",
                                frequency = "1 comprimé au coucher",
                                instructions = "Ne pas dépasser la durée prescrite.",
                                purpose = "Anxiolytique (Benzodiazépine)",
                                warnings = "Risque de dépression respiratoire si associé aux opioïdes.",
                                badgeText = "Alerte Sédatifs + Opioïdes"
                            ),
                            QuickDrugTemplate(
                                brandName = "Tahor 20 mg",
                                genericName = "Atorvastatine",
                                activeIngredients = listOf("atorvastatin"),
                                dosage = "20 mg",
                                frequency = "1 comprimé le soir",
                                instructions = "Prendre régulièrement le soir.",
                                purpose = "Hypolipémiant - Baisse du cholestérol LDL",
                                warnings = "Risque musculaire si associé aux fibrates.",
                                badgeText = "Statine Cholestérol"
                            ),
                            QuickDrugTemplate(
                                brandName = "Amoxicilline 1000 mg",
                                genericName = "Amoxicilline",
                                activeIngredients = listOf("amoxicillin"),
                                dosage = "1000 mg",
                                frequency = "1 comprimé 2 fois par jour pendant 6 jours",
                                instructions = "Poursuivre le traitement jusqu'à la fin.",
                                purpose = "Antibiotique bêta-lactamine",
                                warnings = "Signaler toute allergie à la pénicilline.",
                                badgeText = "Antibiotique"
                            )
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(templates) { t ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddManualDrug(
                                                Drug(
                                                    id = "drug_${java.util.UUID.randomUUID().toString().take(8)}",
                                                    brandName = t.brandName,
                                                    genericName = t.genericName,
                                                    activeIngredients = t.activeIngredients,
                                                    dosage = t.dosage,
                                                    frequency = t.frequency,
                                                    instructions = t.instructions,
                                                    purpose = t.purpose,
                                                    warnings = t.warnings,
                                                    hasActiveRecall = false,
                                                    isPrescription = true
                                                )
                                            )
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = t.brandName,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = t.badgeText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${t.genericName} • ${t.dosage}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Ajouter",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Text(
                            text = "Prenez en photo une ordonnance médicale ou sélectionnez un document PDF :",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ScanActionOptionCard(
                                icon = Icons.Default.CameraAlt,
                                title = "Photographier l'ordonnance",
                                subtitle = "Capture photo via l'appareil photo",
                                onClick = onCameraClick
                            )
                            ScanActionOptionCard(
                                icon = Icons.Default.PictureAsPdf,
                                title = "Document PDF",
                                subtitle = "Importer une ordonnance numérique PDF",
                                onClick = onPdfClick
                            )
                            ScanActionOptionCard(
                                icon = Icons.Default.PhotoLibrary,
                                title = "Galerie photo",
                                subtitle = "Choisir une photo d'ordonnance",
                                onClick = onGalleryClick
                            )
                        }
                    }

                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = brandName,
                                onValueChange = { brandName = it },
                                label = { Text("Nom de marque *") },
                                placeholder = { Text("ex: Kardégic, Advil...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = genericName,
                                onValueChange = { genericName = it },
                                label = { Text("DCI / Molécule active *") },
                                placeholder = { Text("ex: Acide acétylsalicylique, Paracétamol...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dosage,
                                    onValueChange = { dosage = it },
                                    label = { Text("Dosage") },
                                    placeholder = { Text("ex: 500 mg") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = frequency,
                                    onValueChange = { frequency = it },
                                    label = { Text("Posologie") },
                                    placeholder = { Text("ex: 1/jour") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = warnings,
                                onValueChange = { warnings = it },
                                label = { Text("Mises en garde") },
                                placeholder = { Text("ex: Au cours du repas...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    if (brandName.isNotBlank()) {
                                        val gName = genericName.ifBlank { brandName }
                                        val active = listOf(gName.trim().lowercase())
                                        onAddManualDrug(
                                            Drug(
                                                id = "drug_${java.util.UUID.randomUUID().toString().take(8)}",
                                                brandName = brandName.trim(),
                                                genericName = gName.trim(),
                                                activeIngredients = active,
                                                dosage = dosage.trim(),
                                                frequency = frequency.trim().ifBlank { "Selon prescription" },
                                                instructions = "Prendre selon avis médical.",
                                                purpose = "Traitement médical",
                                                warnings = warnings.trim(),
                                                hasActiveRecall = false,
                                                isPrescription = true
                                            )
                                        )
                                        onDismiss()
                                    }
                                },
                                enabled = brandName.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enregistrer dans l'armoire")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
