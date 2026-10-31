package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editor.templates.TemplateData
import com.example.editor.templates.VideoTemplate
import com.example.model.EditingProject
import com.example.model.ScreenOrientationMode
import com.example.ui.theme.SazfloyAmber
import com.example.ui.theme.SazfloyBackground
import com.example.ui.theme.SazfloyBorder
import com.example.ui.theme.SazfloyCyan
import com.example.ui.theme.SazfloyDivider
import com.example.ui.theme.SazfloyPink
import com.example.ui.theme.SazfloySuccess
import com.example.ui.theme.SazfloySurface
import com.example.ui.theme.SazfloySurfaceElevated
import com.example.ui.theme.SazfloySurfaceVariant
import com.example.ui.theme.SazfloyTextMuted
import com.example.ui.theme.SazfloyTextPrimary
import com.example.ui.theme.SazfloyTextSecondary
import com.example.ui.theme.SazfloyToska
import com.example.ui.theme.SazfloyViolet
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportedMediaItem(
    val name: String,
    val uri: Uri,
    val isVideo: Boolean,
    val sizeText: String,
    val dateModified: Long
)

@Composable
fun HomeScreen(
    projects: List<EditingProject>,
    orientationMode: ScreenOrientationMode,
    onToggleOrientation: (ScreenOrientationMode) -> Unit,
    onNewProject: () -> Unit,
    onNewProjectWithMedia: (List<Uri>) -> Unit,
    onOpenProject: (EditingProject) -> Unit,
    onDeleteProject: (String) -> Unit,
    onRenameProject: (String, String) -> Unit,
    onDuplicateProject: (EditingProject) -> Unit = {},
    onStartPhotoEditor: (Uri?) -> Unit,
    onStartFromTemplate: (VideoTemplate) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Proyek", "Template", "Ekspor", "Panduan")
    val exportedItems = remember(selectedTab) { loadExportedMedia(context) }

    // Rename project dialog state
    var projectToRename by remember { mutableStateOf<EditingProject?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Delete confirmation dialog state
    var projectToDelete by remember { mutableStateOf<EditingProject?>(null) }

    // Settings dialog state
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Media pickers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onStartPhotoEditor(uri)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onNewProjectWithMedia(listOf(uri))
        } else {
            onNewProject()
        }
    }

    val multiMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onNewProjectWithMedia(uris)
        } else {
            onNewProject()
        }
    }

    Scaffold(
        containerColor = SazfloyBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    multiMediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                containerColor = SazfloyCyan,
                contentColor = SazfloyBackground,
                modifier = Modifier.testTag("fab_new_project")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Project")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_root"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // App Header & Branding
            item {
                HomeHeader(
                    orientationMode = orientationMode,
                    onToggleOrientation = onToggleOrientation,
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            // Quick Actions Cards (New Project, Edit Video, Edit Photo, Templates)
            item {
                QuickActionSection(
                    onNewProject = {
                        multiMediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    onEditVideo = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onEditPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onOpenTemplates = { selectedTab = 1 }
                )
            }

            // Tabs (Projects, Templates, Exports, Tutorials)
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SazfloyBackground,
                    contentColor = SazfloyCyan,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = SazfloyCyan
                        )
                    },
                    divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SazfloyDivider)) }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) SazfloyCyan else SazfloyTextSecondary,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Recent Projects List
                    if (projects.isEmpty()) {
                        item {
                            EmptyProjectsPlaceholder(
                                onNewProject = {
                                    multiMediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                                onTryTemplate = { selectedTab = 1 }
                            )
                        }
                    } else {
                        items(projects, key = { it.id }) { project ->
                            ProjectListItem(
                                project = project,
                                onClick = { onOpenProject(project) },
                                onDelete = { projectToDelete = project },
                                onRename = {
                                    projectToRename = project
                                    renameInput = project.title
                                },
                                onDuplicate = { onDuplicateProject(project) }
                            )
                        }
                    }
                }
                1 -> {
                    // Templates Grid
                    items(TemplateData.TEMPLATES, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            onUseTemplate = { onStartFromTemplate(template) }
                        )
                    }
                }
                2 -> {
                    // Exported Media Gallery
                    if (exportedItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        tint = SazfloyTextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Exports Yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SazfloyTextPrimary
                                    )
                                    Text(
                                        text = "Videos & Photos exported from SAZFLOY will appear here",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SazfloyTextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        items(exportedItems, key = { it.name }) { item ->
                            ExportedMediaListItem(item = item)
                        }
                    }
                }
                3 -> {
                    // Quick Tutorials
                    item {
                        TutorialSection()
                    }
                }
            }
        }
    }

    // Rename Dialog
    projectToRename?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project", color = SazfloyTextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenameProject(project.id, renameInput)
                        }
                        projectToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel", color = SazfloyTextSecondary)
                }
            },
            containerColor = SazfloySurfaceElevated
        )
    }

    // Delete Confirmation Dialog
    val currentProjectToDelete = projectToDelete
    if (currentProjectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            modifier = Modifier.border(1.dp, SazfloyBorder, RoundedCornerShape(20.dp)),
            title = {
                Text(
                    "Hapus Proyek?",
                    color = SazfloyTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "Apakah kamu yakin ingin menghapus proyek \"${currentProjectToDelete.title}\"? File media asli di perangkat kamu tidak akan dihapus.",
                    color = SazfloyTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(currentProjectToDelete.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyPink, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Batal", color = SazfloyTextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SazfloySurfaceElevated
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            modifier = Modifier.border(1.dp, SazfloyBorder, RoundedCornerShape(20.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = SazfloyToska, modifier = Modifier.size(22.dp))
                    Text("Pengaturan Sazfloy", color = SazfloyTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Versi: 3.0 (Pro Multi-Track & 4K Master Engine)", color = SazfloyToska, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Proyek Tersimpan: ${projects.size} proyek aktif di database", color = SazfloyTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("Penyimpanan Ekspor: Galeri perangkat (Movies/Sazfloy & Pictures/Sazfloy)", color = SazfloyTextSecondary, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val cache = context.cacheDir
                            runCatching { cache.deleteRecursively() }
                            android.widget.Toast.makeText(context, "Cache sementara berhasil dibersihkan", android.widget.Toast.LENGTH_SHORT).show()
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = SazfloyPink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bersihkan Cache Sementara", color = SazfloyPink, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyToska, contentColor = SazfloyBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Selesai", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SazfloySurfaceElevated
        )
    }
}

@Composable
private fun HomeHeader(
    orientationMode: ScreenOrientationMode,
    onToggleOrientation: (ScreenOrientationMode) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // HyperOS Squircle Brand Emblem
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(SazfloyToska, SazfloyCyan))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = SazfloyBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                }

                Column {
                    Text(
                        text = "SAZFLOY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = SazfloyTextPrimary
                    )
                    Text(
                        text = "Studio Kamu",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SazfloyToska
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Orientation toggle button
                OutlinedButton(
                    onClick = {
                        val nextMode = when (orientationMode) {
                            ScreenOrientationMode.AUTO -> ScreenOrientationMode.PORTRAIT
                            ScreenOrientationMode.PORTRAIT -> ScreenOrientationMode.LANDSCAPE
                            ScreenOrientationMode.LANDSCAPE -> ScreenOrientationMode.AUTO
                        }
                        onToggleOrientation(nextMode)
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SazfloySurfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("toggle_orientation_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Rotate",
                        tint = SazfloyToska,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = orientationMode.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SazfloyTextPrimary
                    )
                }

                // Settings Button in modern circle surface
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SazfloySurfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, SazfloyBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SazfloyTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionSection(
    onNewProject: () -> Unit,
    onEditVideo: () -> Unit,
    onEditPhoto: () -> Unit,
    onOpenTemplates: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main New Project Card (HyperOS Squircle with glowing Toska accent)
        Card(
            onClick = onNewProject,
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
                .testTag("card_new_video_project"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SazfloySurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0E2226), SazfloySurfaceElevated)
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(listOf(SazfloyToska, SazfloyCyan))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = SazfloyBackground,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Buat Proyek Baru",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = SazfloyTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pilih foto & video dari galeri perangkat",
                                style = MaterialTheme.typography.bodySmall,
                                color = SazfloyTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SazfloySurfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, SazfloyBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = SazfloyToska,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Action Row: Edit Video, Edit Foto, Template
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Edit Video
            Card(
                onClick = onEditVideo,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .testTag("card_edit_video"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SazfloySurfaceVariant.copy(alpha = 0.7f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SazfloyToska.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = SazfloyToska,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Edit Video",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyTextPrimary
                    )
                }
            }

            // Edit Foto
            Card(
                onClick = onEditPhoto,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .testTag("card_edit_photo"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SazfloySurfaceVariant.copy(alpha = 0.7f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SazfloyPink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = SazfloyPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Edit Foto",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyTextPrimary
                    )
                }
            }

            // Template
            Card(
                onClick = onOpenTemplates,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .testTag("card_templates"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SazfloySurfaceVariant.copy(alpha = 0.7f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SazfloyViolet.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SazfloyViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Template",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportedMediaListItem(item: ExportedMediaItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SazfloySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isVideo) SazfloyCyan.copy(alpha = 0.2f) else SazfloyPink.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isVideo) Icons.Default.Movie else Icons.Default.Image,
                        contentDescription = null,
                        tint = if (item.isVideo) SazfloyCyan else SazfloyPink,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${if (item.isVideo) "Video MP4" else "Photo JPEG"} • ${item.sizeText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SazfloyTextMuted
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(item.uri, if (item.isVideo) "video/mp4" else "image/jpeg")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        runCatching { context.startActivity(intent) }
                    }
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Open", tint = SazfloyCyan)
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = if (item.isVideo) "video/mp4" else "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, item.uri)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = SazfloyTextSecondary)
                }
            }
        }
    }
}

private fun loadExportedMedia(context: Context): List<ExportedMediaItem> {
    val items = mutableListOf<ExportedMediaItem>()
    try {
        val moviesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "SAZFLOY")
        if (moviesDir.exists()) {
            moviesDir.listFiles()?.filter { it.extension.equals("mp4", ignoreCase = true) }?.forEach { f ->
                val sizeMb = "%.1f MB".format(f.length() / (1024f * 1024f))
                items.add(
                    ExportedMediaItem(
                        name = f.name,
                        uri = Uri.fromFile(f),
                        isVideo = true,
                        sizeText = sizeMb,
                        dateModified = f.lastModified()
                    )
                )
            }
        }
        val picsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SAZFLOY")
        if (picsDir.exists()) {
            picsDir.listFiles()?.filter { it.extension.equals("jpg", ignoreCase = true) || it.extension.equals("jpeg", ignoreCase = true) }?.forEach { f ->
                val sizeKb = "${f.length() / 1024} KB"
                items.add(
                    ExportedMediaItem(
                        name = f.name,
                        uri = Uri.fromFile(f),
                        isVideo = false,
                        sizeText = sizeKb,
                        dateModified = f.lastModified()
                    )
                )
            }
        }
    } catch (_: Exception) {}
    return items.sortedByDescending { it.dateModified }
}

@Composable
private fun ProjectListItem(
    project: EditingProject,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(project.updatedAt) { dateFormat.format(Date(project.updatedAt)) }
    val firstClip = project.clips.firstOrNull()
    val thumbnailModel = firstClip?.thumbnailUri ?: firstClip?.uri

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("project_item_${project.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SazfloySurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle Thumbnail box with real image preview
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF142426), Color(0xFF0F151C))
                        )
                    )
                    .border(1.dp, SazfloyBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!thumbnailModel.isNullOrBlank()) {
                    AsyncImage(
                        model = thumbnailModel,
                        contentDescription = project.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (project.type == com.example.model.ProjectType.VIDEO) Icons.Default.Movie else Icons.Default.Image,
                        contentDescription = null,
                        tint = SazfloyToska,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Ratio badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = project.aspectRatio.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyTextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${project.clips.size} klip • ${String.format(Locale.US, "%.1f", project.totalDurationMs / 1000f)} dtk • $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = SazfloyTextMuted,
                    maxLines = 1
                )
            }

            // Duplicate button
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SazfloySurfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplikat Proyek",
                    tint = SazfloyToska,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Rename button
            IconButton(
                onClick = onRename,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SazfloySurfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Ubah Nama",
                    tint = SazfloyTextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SazfloyPink.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = SazfloyPink,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: VideoTemplate,
    onUseTemplate: () -> Unit
) {
    Card(
        onClick = onUseTemplate,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("template_card_${template.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SazfloySurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SazfloyViolet.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = template.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SazfloyViolet
                        )
                    }
                    Text(
                        text = "• ${template.durationText} • ${template.aspectRatio.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SazfloyTextMuted
                    )
                }

                Button(
                    onClick = onUseTemplate,
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyToska, contentColor = SazfloyBackground),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Gunakan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SazfloyTextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = SazfloyTextSecondary
            )
        }
    }
}

@Composable
private fun EmptyProjectsPlaceholder(
    onNewProject: () -> Unit,
    onTryTemplate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Squircle halo icon
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(SazfloySurfaceElevated)
                .border(1.dp, SazfloyBorder, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = SazfloyToska,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Belum ada proyek",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            ),
            color = SazfloyTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Mulai proyek baru dengan memilih foto atau video dari perangkat kamu.",
            style = MaterialTheme.typography.bodyMedium,
            color = SazfloyTextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onNewProject,
            colors = ButtonDefaults.buttonColors(
                containerColor = SazfloyToska,
                contentColor = SazfloyBackground
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            modifier = Modifier.testTag("empty_state_new_project_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+ Buat Proyek Baru",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
private fun TutorialSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TutorialItem(
            step = "1",
            title = "Import & Arrange Clips",
            desc = "Tap '+ New Project' to select videos or photos. Drag to arrange on the timeline, tap a clip to adjust volume, speed (0.25x - 3.0x), and duration."
        )
        TutorialItem(
            step = "2",
            title = "Trim & Split with Precision",
            desc = "Move the playhead to any frame and tap 'Split' to divide a clip into two. Delete unwanted sections with a single tap."
        )
        TutorialItem(
            step = "3",
            title = "Color Grading & Filters",
            desc = "Select from 10 SAZFLOY cinematic presets. Fine-tune brightness, contrast, temperature, tint, and vignette in real time."
        )
        TutorialItem(
            step = "4",
            title = "Text, Stickers & Audio",
            desc = "Add title cards and subtitles with custom fonts, colors, and outline strokes. Add music tracks with smooth fade in/out."
        )
        TutorialItem(
            step = "5",
            title = "Portrait & Landscape Editing",
            desc = "Work horizontally in landscape mode for a wider timeline and bigger preview, or vertically for quick one-handed edits."
        )
        TutorialItem(
            step = "6",
            title = "Export to Gallery",
            desc = "Export in 720p or 1080p up to 60 FPS directly into your phone's Gallery (Movies/SAZFLOY), ready to share to TikTok, Reels, or YouTube."
        )
    }
}

@Composable
private fun TutorialItem(step: String, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SazfloySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SazfloyCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    step,
                    color = SazfloyCyan,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = SazfloyTextSecondary
                )
            }
        }
    }
}
