package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.BackupResult
import com.example.data.RestoreResult
import com.example.data.ShinaKeyboardRepository
import com.example.data.StorageStats
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    repository: ShinaKeyboardRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var stats by remember { mutableStateOf<StorageStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var actionMessage by remember { mutableStateOf("") }
    var integrityResult by remember { mutableStateOf<String?>(null) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var showPasteRestoreDialog by remember { mutableStateOf(false) }
    var showDatasetExportDialog by remember { mutableStateOf(false) }
    var showDatasetImportDialog by remember { mutableStateOf(false) }
    var exportedDatasetText by remember { mutableStateOf("") }
    var datasetInputText by remember { mutableStateOf("") }
    var datasetImportStatus by remember { mutableStateOf("") }
    var availableBackups by remember { mutableStateOf<List<File>>(emptyList()) }

    val backupManager = remember { repository.getBackupManager(context) }
    val currentUser by backupManager.currentUser.collectAsState()
    val isSyncing by backupManager.isSyncing.collectAsState()
    val syncState by backupManager.syncState.collectAsState()
    var cloudActionMessage by remember { mutableStateOf("") }

    fun refreshStats() {
        scope.launch {
            isLoading = true
            stats = repository.getStorageStats(context)
            availableBackups = repository.getAvailableBackups(context)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Storage & Permanent Data Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "ACID Local SQLite Storage • Automated Backups",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshStats() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh storage metrics"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading && stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                com.example.ui.components.AiQuantumOrbLoader(
                    size = 110.dp,
                    statusText = "Analyzing 'SAIPBG' Permanent Storage & WAL Engine..."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Hero Storage Overview Card ---
                item {
                    Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Permanent Storage Status",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (stats != null) "Database Size: ${stats!!.formattedTotalSize()}" else "Calculating...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "WAL Mode",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        stats?.let { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StorageMetricItem("Words & Lexicon", "${s.wordsCount} (${s.userAddedWordsCount} user)")
                                StorageMetricItem("Dictionary", "${s.dictionaryCount} entries")
                                StorageMetricItem("Sentences", "${s.sentencesCount} total")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StorageMetricItem("Learned Phrases", "${s.phrasesCount}")
                                StorageMetricItem("N-Grams", "${s.ngramsCount}")
                                StorageMetricItem("Schema Version", "Room V${s.schemaVersion}")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (s.lastBackupTimestamp != null) {
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(s.lastBackupTimestamp))
                                    "Last Successful Backup: $dateStr"
                                } else {
                                    "No manual backup created yet."
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // --- Backup & Restore Engine Card ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Full Snapshot Backup & Restore",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Safeguard words, verified sentences, dictionary, and models forever",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val result = repository.createBackup(context, "Manual Backup")
                                        actionMessage = if (result.success) {
                                            "Backup created successfully! (${result.fileSizeFormatted}, ${result.itemsCount} items)"
                                        } else {
                                            result.message
                                        }
                                        refreshStats()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("storage_create_backup_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Create Backup", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("storage_restore_backup_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Restore...", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showPasteRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Import Backup via JSON Text", fontSize = 12.sp)
                        }

                        if (actionMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = actionMessage,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Firebase Firestore Cloud Synchronization ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_cloud_firestore_sync"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Firestore Cloud Synchronization",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Permanent cloud persistence across device changes & reinstalls",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Google Account Status
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentUser != null) (currentUser?.displayName ?: "Google Account") else "Not Connected to Google Account",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (currentUser != null) (currentUser?.email ?: "Cloud sync active") else "Sign in to backup contributed Shina data",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (currentUser == null) {
                                    Button(
                                        onClick = {
                                            val activity = context as? Activity
                                            if (activity != null) {
                                                scope.launch {
                                                    cloudActionMessage = "Connecting with Google..."
                                                    val res = backupManager.signInWithGoogle(activity)
                                                    cloudActionMessage = if (res.isSuccess) {
                                                        "Connected as ${res.getOrNull()?.email ?: "User"}!"
                                                    } else {
                                                        res.exceptionOrNull()?.message ?: "Sign-in failed"
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_google_sign_in_backup")
                                    ) {
                                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Sign in with Google", fontSize = 12.sp)
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            backupManager.signOut()
                                            cloudActionMessage = "Signed out from Google Account."
                                        }
                                    ) {
                                        Text("Sign Out", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val res = backupManager.syncLocalDataToFirestore()
                                        cloudActionMessage = res.message
                                        refreshStats()
                                    }
                                },
                                enabled = currentUser != null && !isSyncing,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_sync_to_firestore"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Syncing...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Sync to Firestore", fontSize = 13.sp)
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    scope.launch {
                                        val res = backupManager.restoreDataFromFirestore()
                                        cloudActionMessage = res.message
                                        refreshStats()
                                    }
                                },
                                enabled = currentUser != null && !isSyncing,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_restore_from_firestore"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Restore from Cloud", fontSize = 13.sp)
                            }
                        }

                        if (cloudActionMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = cloudActionMessage,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Linguistic Dataset Export & AI Acceleration Import ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dataset,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Shina Contributed Linguistic Dataset",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Export user vocabulary & grammar as JSON, or import datasets to accelerate AI learning",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val res = repository.exportLinguisticDataset(context)
                                        if (res.success) {
                                            exportedDatasetText = res.jsonContent
                                            showDatasetExportDialog = true
                                            actionMessage = "Dataset ready for export (${res.fileSizeFormatted})"
                                        } else {
                                            actionMessage = res.message
                                        }
                                        refreshStats()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_dataset"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Export Dataset", fontSize = 13.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    datasetImportStatus = ""
                                    showDatasetImportDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_import_dataset"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Import Dataset", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // --- Database Maintenance & Compaction ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Database Compaction & ACID Integrity",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Maintain maximum read/write performance and clean fragmented space",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val check = repository.verifyDatabaseIntegrity()
                                        integrityResult = check
                                        Toast.makeText(context, "SQLite Integrity Check: $check", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("storage_integrity_check_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Check Integrity", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = repository.compactAndVacuumDatabase()
                                        if (success) {
                                            Toast.makeText(context, "Database VACUUM & Checkpoint complete!", Toast.LENGTH_SHORT).show()
                                            refreshStats()
                                        } else {
                                            Toast.makeText(context, "Compaction failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("storage_vacuum_db_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Optimize (VACUUM)", fontSize = 12.sp)
                            }
                        }

                        if (integrityResult != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Integrity Result: $integrityResult",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (integrityResult == "ok") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Safe Cache Clean
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Clear Transient Cache & Unpinned Clips", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Does NOT delete any words, sentences, or dictionary entries.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.clearTemporaryCache(context)
                                        Toast.makeText(context, "Temporary cache cleared successfully", Toast.LENGTH_SHORT).show()
                                        refreshStats()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Clean", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // --- Database Architecture & File Information Card ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Internal Storage Architecture",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        stats?.let { s ->
                            DetailRow("Database Engine", "SQLite 3 with Android Room")
                            DetailRow("Journal Mode", "WAL (Write-Ahead Logging)")
                            DetailRow("Database File", s.databasePath)
                            DetailRow("Main DB Size", s.formattedDbSize())
                            DetailRow("WAL Journal Size", s.formattedWalSize())
                            DetailRow("App Cache Size", s.formattedCacheSize())
                        }
                    }
                }
            }
        }
    }
}

    // --- Restore from File Dialog ---
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            confirmButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Available Saved Backups", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                if (availableBackups.isEmpty()) {
                    Text(
                        "No local backup snapshots found. Create a backup first by tapping 'Create Backup'.",
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableBackups) { file ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f KB", file.length() / 1024.0)} • Modified: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(file.lastModified()))}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    val text = file.readText()
                                                    val res = repository.restoreFromBackupJson(text)
                                                    actionMessage = res.message
                                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                                    showRestoreDialog = false
                                                    refreshStats()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RestorePage,
                                                contentDescription = "Restore this snapshot",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                try {
                                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_SUBJECT, "Shina AI Keyboard Backup")
                                                        putExtra(Intent.EXTRA_TEXT, file.readText())
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, "Share Backup"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    // --- Paste JSON Text Restore Dialog ---
    if (showPasteRestoreDialog) {
        var jsonInput by remember { mutableStateOf("") }
        var restoreStatus by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPasteRestoreDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = repository.restoreFromBackupJson(jsonInput)
                            restoreStatus = res.message
                            if (res.success) {
                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                refreshStats()
                                showPasteRestoreDialog = false
                            }
                        }
                    },
                    enabled = jsonInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Import & Merge")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteRestoreDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Import Backup from JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Paste a previously exported Shina AI Keyboard JSON backup snippet below to restore all words, dictionary entries, and training sentences:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("Paste JSON here: {\"app\": \"Shina AI Keyboard\"...}") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (restoreStatus.isNotBlank()) {
                        Text(restoreStatus, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // --- Dataset Export Dialog ---
    if (showDatasetExportDialog) {
        AlertDialog(
            onDismissRequest = { showDatasetExportDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Shina Linguistic Dataset", exportedDatasetText)
                        cm.setPrimaryClip(clip)
                        Toast.makeText(context, "Dataset JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showDatasetExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        try {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Shina Contributed Linguistic Dataset")
                                putExtra(Intent.EXTRA_TEXT, exportedDatasetText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Shina Dataset"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dataset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Exported Shina Dataset", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your contributed Shina vocabulary and grammar dataset has been packaged as portable JSON. You can share it with other researchers or import it across devices:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = exportedDatasetText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        )
    }

    // --- Dataset Import & Accelerate AI Learning Dialog ---
    if (showDatasetImportDialog) {
        AlertDialog(
            onDismissRequest = { showDatasetImportDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (datasetInputText.isBlank()) {
                            datasetImportStatus = "Please paste a JSON dataset first."
                            return@Button
                        }
                        scope.launch {
                            val res = repository.importLinguisticDataset(context, datasetInputText)
                            if (res.success) {
                                datasetImportStatus = res.message
                                Toast.makeText(context, "AI Learning pass complete with new dataset!", Toast.LENGTH_LONG).show()
                                refreshStats()
                            } else {
                                datasetImportStatus = res.message
                            }
                        }
                    },
                    enabled = datasetInputText.isNotBlank()
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import & Train AI")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatasetImportDialog = false }) {
                    Text("Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Import Dataset & Accelerate AI", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste a Shina vocabulary and grammar JSON dataset below. Once imported, the on-device AI model will immediately extract tokens, train n-grams, and update predictive weights to learn faster:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = datasetInputText,
                        onValueChange = { datasetInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("Paste JSON: {\"vocabulary\": [...], \"grammar_rules\": [...]}") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (datasetImportStatus.isNotBlank()) {
                        Text(
                            text = datasetImportStatus,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}


@Composable
private fun StorageMetricItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
