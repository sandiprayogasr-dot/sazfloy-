package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.editor.components.DaVinciColorWheelsComponent
import com.example.filters.LutEngine
import com.example.ui.theme.SazfloyFonts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ShapeOverlay
import com.example.model.ShapeType
import com.example.editor.photo.PhotoProcessingEngine
import com.example.filters.SazfloyColorEngine
import com.example.model.AspectRatioMode
import com.example.model.ClipType
import com.example.model.ColorAdjustments
import com.example.model.EditingProject
import com.example.model.MediaClip
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import com.example.ui.theme.SazfloyAmber
import com.example.ui.theme.SazfloyBackground
import com.example.ui.theme.SazfloyBorder
import com.example.ui.theme.SazfloyCyan
import com.example.ui.theme.SazfloyDivider
import com.example.ui.theme.SazfloyPink
import com.example.ui.theme.SazfloySurface
import com.example.ui.theme.SazfloySurfaceElevated
import com.example.ui.theme.SazfloySurfaceVariant
import com.example.ui.theme.SazfloyTextMuted
import com.example.ui.theme.SazfloyTextPrimary
import com.example.ui.theme.SazfloyTextSecondary
import com.example.ui.theme.SazfloyToska
import com.example.ui.theme.SazfloyViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PhotoEditorTab(val label: String) {
    COLOR_WHEELS("DaVinci Wheels"),
    LUTS("3D LUTs"),
    FILTERS("Filters"),
    ADJUST("Adjust"),
    CROP_ROTATE("Transform"),
    TEXT("Text"),
    SHAPES("Shapes"),
    LAYERS("Layers"),
    STICKERS("Stickers")
}


@Composable
fun PhotoEditorScreen(
    initialProject: EditingProject? = null,
    initialImageUri: Uri? = null,
    onSaveProject: (EditingProject) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Base Bitmap
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(true) }

    // State
    var adjustments by remember { mutableStateOf(initialProject?.adjustments ?: ColorAdjustments()) }
    var rotationDegrees by remember { mutableIntStateOf(initialProject?.clips?.firstOrNull()?.rotationDegrees ?: 0) }
    var isFlippedHorizontal by remember { mutableStateOf(initialProject?.clips?.firstOrNull()?.isFlippedHorizontal ?: false) }
    var isFlippedVertical by remember { mutableStateOf(initialProject?.clips?.firstOrNull()?.isFlippedVertical ?: false) }
    var textOverlays by remember { mutableStateOf<List<TextOverlay>>(initialProject?.textOverlays ?: emptyList()) }
    var stickerOverlays by remember { mutableStateOf<List<StickerOverlay>>(initialProject?.stickerOverlays ?: emptyList()) }
    var shapeOverlays by remember { mutableStateOf<List<ShapeOverlay>>(initialProject?.shapeOverlays ?: emptyList()) }
    var addWatermark by remember { mutableStateOf(false) }
    var isComparingBeforeAfter by remember { mutableStateOf(false) }

    // History for Undo/Redo
    var undoStack by remember { mutableStateOf<List<ColorAdjustments>>(emptyList()) }
    var redoStack by remember { mutableStateOf<List<ColorAdjustments>>(emptyList()) }

    var selectedTab by remember { mutableStateOf(PhotoEditorTab.FILTERS) }
    var isExporting by remember { mutableStateOf(false) }
    var exportedUri by remember { mutableStateOf<Uri?>(null) }
    var showExportChoiceDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("JPEG") }
    var exportQuality by remember { mutableIntStateOf(95) }

    // Text Editor Dialog
    var showTextDialog by remember { mutableStateOf(false) }
    var currentEditingText by remember { mutableStateOf("") }
    var textColorHex by remember { mutableStateOf("#FFFFFF") }
    var textHasOutline by remember { mutableStateOf(true) }
    var selectedFontFamilyId by remember { mutableStateOf("montserrat") }

    fun persistProjectChanges() {
        if (initialProject != null) {
            val updatedClip = initialProject.clips.firstOrNull()?.copy(
                rotationDegrees = rotationDegrees,
                isFlippedHorizontal = isFlippedHorizontal,
                isFlippedVertical = isFlippedVertical
            ) ?: MediaClip(type = ClipType.PHOTO, rotationDegrees = rotationDegrees, isFlippedHorizontal = isFlippedHorizontal, isFlippedVertical = isFlippedVertical)
            val updated = initialProject.copy(
                adjustments = adjustments,
                textOverlays = textOverlays,
                stickerOverlays = stickerOverlays,
                shapeOverlays = shapeOverlays,
                clips = listOf(updatedClip),
                updatedAt = System.currentTimeMillis()
            )
            onSaveProject(updated)
        }
    }

    // Helper for undo state update
    fun updateAdjustments(newAdj: ColorAdjustments) {
        undoStack = undoStack + adjustments
        redoStack = emptyList()
        adjustments = newAdj
        persistProjectChanges()
    }

    val lutFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val fileName = uri.lastPathSegment ?: "Custom_LUT.cube"
                    val parsed = LutEngine.parseCube(stream, fileName)
                    updateAdjustments(adjustments.copy(activeLutId = parsed.id, lutIntensity = 1.0f))
                    Toast.makeText(context, "3D LUT Berhasil Diimpor: ${parsed.name}", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { err ->
                Toast.makeText(context, "Gagal memproses file .cube: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler {
        persistProjectChanges()
        onNavigateBack()
    }

    // Load initial bitmap
    LaunchedEffect(initialImageUri) {
        isLoadingImage = true
        withContext(Dispatchers.IO) {
            val bmp = if (initialImageUri != null) {
                PhotoProcessingEngine.loadBitmapFromUri(context, initialImageUri, 1920)
                    ?: PhotoProcessingEngine.createSamplePhoto("SAZFLOY Studio")
            } else {
                PhotoProcessingEngine.createSamplePhoto("SAZFLOY Studio")
            }
            originalBitmap = bmp
            isLoadingImage = false
        }
    }

    Scaffold(
        containerColor = SazfloyBackground,
        topBar = {
            PhotoEditorTopBar(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                onUndo = {
                    if (undoStack.isNotEmpty()) {
                        val last = undoStack.last()
                        undoStack = undoStack.dropLast(1)
                        redoStack = redoStack + adjustments
                        adjustments = last
                    }
                },
                onRedo = {
                    if (redoStack.isNotEmpty()) {
                        val next = redoStack.last()
                        redoStack = redoStack.dropLast(1)
                        undoStack = undoStack + adjustments
                        adjustments = next
                    }
                },
                onReset = {
                    updateAdjustments(ColorAdjustments())
                    rotationDegrees = 0
                    isFlippedHorizontal = false
                    isFlippedVertical = false
                    textOverlays = emptyList()
                    stickerOverlays = emptyList()
                    shapeOverlays = emptyList()
                },
                isComparing = isComparingBeforeAfter,
                onToggleCompare = { isComparingBeforeAfter = !isComparingBeforeAfter },
                onExport = {
                    if (originalBitmap != null) {
                        showExportChoiceDialog = true
                    }
                },
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("photo_editor_root")
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // LANDSCAPE MODE: Image preview left, tool panel right
                Row(modifier = Modifier.fillMaxSize()) {
                    // Preview Pane
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PhotoPreviewArea(
                            bitmap = originalBitmap,
                            isLoading = isLoadingImage,
                            adjustments = adjustments,
                            isComparing = isComparingBeforeAfter,
                            rotationDegrees = rotationDegrees,
                            flipHorizontal = isFlippedHorizontal,
                            flipVertical = isFlippedVertical,
                            textOverlays = textOverlays,
                            stickerOverlays = stickerOverlays
                        )
                    }

                    // Tools Pane
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(SazfloySurface)
                            .border(1.dp, SazfloyBorder)
                    ) {
                        PhotoEditorTabsRow(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            PhotoEditorTabContent(
                                selectedTab = selectedTab,
                                adjustments = adjustments,
                                onUpdateAdjustments = { updateAdjustments(it) },
                                rotationDegrees = rotationDegrees,
                                onRotate = { rotationDegrees = (rotationDegrees + 90) % 360 },
                                isFlippedHorizontal = isFlippedHorizontal,
                                onToggleFlipH = { isFlippedHorizontal = !isFlippedHorizontal },
                                isFlippedVertical = isFlippedVertical,
                                onToggleFlipV = { isFlippedVertical = !isFlippedVertical },
                                onAddText = { showTextDialog = true },
                                textOverlays = textOverlays,
                                onRemoveText = { id -> textOverlays = textOverlays.filter { it.id != id } },
                                onUpdateText = { updated -> textOverlays = textOverlays.map { if (it.id == updated.id) updated else it } },
                                stickerOverlays = stickerOverlays,
                                onAddSticker = { tag ->
                                    stickerOverlays = stickerOverlays + StickerOverlay(emojiOrTag = tag)
                                },
                                onRemoveSticker = { id -> stickerOverlays = stickerOverlays.filter { it.id != id } },
                                shapeOverlays = shapeOverlays,
                                onAddShape = { shape -> shapeOverlays = shapeOverlays + shape },
                                onRemoveShape = { id -> shapeOverlays = shapeOverlays.filter { it.id != id } },
                                onUpdateShape = { updated -> shapeOverlays = shapeOverlays.map { if (it.id == updated.id) updated else it } },
                                onMoveLayerUp = { layerId ->
                                    // Move layer forward
                                    val shapeIdx = shapeOverlays.indexOfFirst { it.id == layerId }
                                    if (shapeIdx > 0) {
                                        val mutable = shapeOverlays.toMutableList()
                                        val item = mutable.removeAt(shapeIdx)
                                        mutable.add(shapeIdx - 1, item)
                                        shapeOverlays = mutable
                                    }
                                    val textIdx = textOverlays.indexOfFirst { it.id == layerId }
                                    if (textIdx > 0) {
                                        val mutable = textOverlays.toMutableList()
                                        val item = mutable.removeAt(textIdx)
                                        mutable.add(textIdx - 1, item)
                                        textOverlays = mutable
                                    }
                                },
                                onMoveLayerDown = { layerId ->
                                    // Move layer backward
                                    val shapeIdx = shapeOverlays.indexOfFirst { it.id == layerId }
                                    if (shapeIdx >= 0 && shapeIdx < shapeOverlays.lastIndex) {
                                        val mutable = shapeOverlays.toMutableList()
                                        val item = mutable.removeAt(shapeIdx)
                                        mutable.add(shapeIdx + 1, item)
                                        shapeOverlays = mutable
                                    }
                                    val textIdx = textOverlays.indexOfFirst { it.id == layerId }
                                    if (textIdx >= 0 && textIdx < textOverlays.lastIndex) {
                                        val mutable = textOverlays.toMutableList()
                                        val item = mutable.removeAt(textIdx)
                                        mutable.add(textIdx + 1, item)
                                        textOverlays = mutable
                                    }
                                },
                                addWatermark = addWatermark,
                                onToggleWatermark = { addWatermark = !addWatermark },
                                onImportLutClick = { lutFilePickerLauncher.launch("*/*") }
                            )
                        }
                    }
                }
            } else {
                // PORTRAIT MODE: Image preview top, tool tabs and controls bottom
                Column(modifier = Modifier.fillMaxSize()) {
                    // Preview Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.3f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PhotoPreviewArea(
                            bitmap = originalBitmap,
                            isLoading = isLoadingImage,
                            adjustments = adjustments,
                            isComparing = isComparingBeforeAfter,
                            rotationDegrees = rotationDegrees,
                            flipHorizontal = isFlippedHorizontal,
                            flipVertical = isFlippedVertical,
                            textOverlays = textOverlays,
                            stickerOverlays = stickerOverlays,
                            shapeOverlays = shapeOverlays
                        )
                    }

                    // Bottom Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SazfloySurface)
                            .border(1.dp, SazfloyBorder)
                    ) {
                        PhotoEditorTabsRow(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(12.dp)
                        ) {
                            PhotoEditorTabContent(
                                selectedTab = selectedTab,
                                adjustments = adjustments,
                                onUpdateAdjustments = { updateAdjustments(it) },
                                rotationDegrees = rotationDegrees,
                                onRotate = { rotationDegrees = (rotationDegrees + 90) % 360 },
                                isFlippedHorizontal = isFlippedHorizontal,
                                onToggleFlipH = { isFlippedHorizontal = !isFlippedHorizontal },
                                isFlippedVertical = isFlippedVertical,
                                onToggleFlipV = { isFlippedVertical = !isFlippedVertical },
                                onAddText = { showTextDialog = true },
                                textOverlays = textOverlays,
                                onRemoveText = { id -> textOverlays = textOverlays.filter { it.id != id } },
                                onUpdateText = { updated -> textOverlays = textOverlays.map { if (it.id == updated.id) updated else it } },
                                stickerOverlays = stickerOverlays,
                                onAddSticker = { tag ->
                                    stickerOverlays = stickerOverlays + StickerOverlay(emojiOrTag = tag)
                                },
                                onRemoveSticker = { id -> stickerOverlays = stickerOverlays.filter { it.id != id } },
                                shapeOverlays = shapeOverlays,
                                onAddShape = { shape -> shapeOverlays = shapeOverlays + shape },
                                onRemoveShape = { id -> shapeOverlays = shapeOverlays.filter { it.id != id } },
                                onUpdateShape = { updated -> shapeOverlays = shapeOverlays.map { if (it.id == updated.id) updated else it } },
                                onMoveLayerUp = { layerId ->
                                    val shapeIdx = shapeOverlays.indexOfFirst { it.id == layerId }
                                    if (shapeIdx > 0) {
                                        val mutable = shapeOverlays.toMutableList()
                                        val item = mutable.removeAt(shapeIdx)
                                        mutable.add(shapeIdx - 1, item)
                                        shapeOverlays = mutable
                                    }
                                    val textIdx = textOverlays.indexOfFirst { it.id == layerId }
                                    if (textIdx > 0) {
                                        val mutable = textOverlays.toMutableList()
                                        val item = mutable.removeAt(textIdx)
                                        mutable.add(textIdx - 1, item)
                                        textOverlays = mutable
                                    }
                                },
                                onMoveLayerDown = { layerId ->
                                    val shapeIdx = shapeOverlays.indexOfFirst { it.id == layerId }
                                    if (shapeIdx >= 0 && shapeIdx < shapeOverlays.lastIndex) {
                                        val mutable = shapeOverlays.toMutableList()
                                        val item = mutable.removeAt(shapeIdx)
                                        mutable.add(shapeIdx + 1, item)
                                        shapeOverlays = mutable
                                    }
                                    val textIdx = textOverlays.indexOfFirst { it.id == layerId }
                                    if (textIdx >= 0 && textIdx < textOverlays.lastIndex) {
                                        val mutable = textOverlays.toMutableList()
                                        val item = mutable.removeAt(textIdx)
                                        mutable.add(textIdx + 1, item)
                                        textOverlays = mutable
                                    }
                                },
                                addWatermark = addWatermark,
                                onToggleWatermark = { addWatermark = !addWatermark },
                                onImportLutClick = { lutFilePickerLauncher.launch("*/*") }
                            )
                        }
                    }
                }
            }

        }
    }

    // Text Overlay Add Dialog
    if (showTextDialog) {
        Dialog(onDismissRequest = { showTextDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SazfloySurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Text to Photo", color = SazfloyTextPrimary, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = currentEditingText,
                        onValueChange = { currentEditingText = it },
                        placeholder = { Text("Enter text caption...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Color swatches
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#FFFFFF", "#00E5FF", "#FFB300", "#FF2D75", "#00E676", "#000000").forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(if (textColorHex == hex) 2.dp else 1.dp, Color.White, CircleShape)
                                    .clickable { textColorHex = hex }
                            )
                        }
                    }

                    Text("Pilih Font Tipografi", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SazfloyFonts.AVAILABLE_FONTS) { fontItem ->
                            val isSel = selectedFontFamilyId == fontItem.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                    .clickable { selectedFontFamilyId = fontItem.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = fontItem.displayName,
                                    fontFamily = fontItem.family,
                                    color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTextDialog = false }) {
                            Text("Cancel", color = SazfloyTextSecondary)
                        }
                        Button(
                            onClick = {
                                if (currentEditingText.isNotBlank()) {
                                    textOverlays = textOverlays + TextOverlay(
                                        text = currentEditingText,
                                        fontColorHex = textColorHex,
                                        fontFamilyName = selectedFontFamilyId,
                                        posYRatio = 0.5f
                                    )
                                    currentEditingText = ""
                                }
                                showTextDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                        ) {
                            Text("Add Text")
                        }
                    }
                }
            }
        }
    }

    // Export Choice Format Dialog (PNG / JPEG with Quality setting)
    if (showExportChoiceDialog) {
        Dialog(onDismissRequest = { if (!isExporting) showExportChoiceDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SazfloySurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Ekspor Foto Sazfloy",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyTextPrimary
                    )
                    Text(
                        text = "Pilih format file dan kualitas output untuk hasil maksimal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SazfloyTextSecondary
                    )

                    // Format Selector: JPEG vs PNG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportFormat = "JPEG" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (exportFormat == "JPEG") SazfloyCyan else SazfloySurfaceVariant
                            )
                        ) {
                            Text(
                                "JPEG (Standar)",
                                color = if (exportFormat == "JPEG") SazfloyBackground else SazfloyTextPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = { exportFormat = "PNG" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (exportFormat == "PNG") SazfloyCyan else SazfloySurfaceVariant
                            )
                        ) {
                            Text(
                                "PNG (Lossless)",
                                color = if (exportFormat == "PNG") SazfloyBackground else SazfloyTextPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (exportFormat == "JPEG") {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kualitas Kompresi", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                                Text("$exportQuality%", style = MaterialTheme.typography.labelSmall, color = SazfloyCyan)
                            }
                            Slider(
                                value = exportQuality.toFloat(),
                                onValueChange = { exportQuality = it.toInt() },
                                valueRange = 50f..100f,
                                colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                            )
                        }
                    }

                    if (isExporting) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = SazfloyCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Memproses & Menyimpan...", color = SazfloyCyan, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showExportChoiceDialog = false },
                            enabled = !isExporting
                        ) {
                            Text("Batal", color = SazfloyTextSecondary)
                        }
                        Button(
                            onClick = {
                                val bmp = originalBitmap ?: return@Button
                                isExporting = true
                                coroutineScope.launch {
                                    val finalPhoto = PhotoProcessingEngine.renderFinalPhoto(
                                        sourceBitmap = bmp,
                                        adjustments = adjustments,
                                        rotationDegrees = rotationDegrees,
                                        flipHorizontal = isFlippedHorizontal,
                                        flipVertical = isFlippedVertical,
                                        textOverlays = textOverlays,
                                        stickerOverlays = stickerOverlays,
                                        shapeOverlays = shapeOverlays,
                                        addWatermark = addWatermark,
                                        context = context
                                    )
                                    val result = PhotoProcessingEngine.exportPhotoToGallery(
                                        context = context,
                                        bitmap = finalPhoto,
                                        format = exportFormat,
                                        quality = exportQuality
                                    )
                                    isExporting = false
                                    showExportChoiceDialog = false
                                    result.onSuccess { uri ->
                                        exportedUri = uri
                                        Toast.makeText(context, "Foto tersimpan di Galeri ($exportFormat)!", Toast.LENGTH_LONG).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Gagal mengekspor: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isExporting,
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                        ) {
                            Text("Ekspor Sekarang")
                        }
                    }
                }
            }
        }
    }

    // Export Success Dialog
    exportedUri?.let { uri ->
        Dialog(onDismissRequest = { exportedUri = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SazfloySurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Foto Berhasil Diekspor!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyTextPrimary
                    )
                    Text(
                        text = "Foto resolusi tinggi telah tersimpan permanen di Galeri.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SazfloyTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val mimeType = if (exportFormat.equals("PNG", true)) "image/png" else "image/jpeg"
                                    setDataAndType(uri, mimeType)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                runCatching { context.startActivity(intent) }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Buka")
                        }
                        Button(
                            onClick = {
                                val mimeType = if (exportFormat.equals("PNG", true)) "image/png" else "image/jpeg"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = mimeType
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Foto Sazfloy"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan")
                        }
                    }
                    TextButton(onClick = { exportedUri = null }) {
                        Text("Tutup", color = SazfloyTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoEditorTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    isComparing: Boolean,
    onToggleCompare: () -> Unit,
    onExport: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SazfloyBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("photo_editor_back")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SazfloyTextPrimary
                )
            }
            Text(
                text = "Studio Foto Sazfloy",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SazfloyTextPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            // Undo / Redo
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) SazfloyTextPrimary else SazfloyTextMuted
                )
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) SazfloyTextPrimary else SazfloyTextMuted
                )
            }

            // Before / After toggle
            IconButton(
                onClick = onToggleCompare,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isComparing) SazfloyCyan.copy(alpha = 0.2f) else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Compare Before/After",
                    tint = if (isComparing) SazfloyCyan else SazfloyTextSecondary
                )
            }

            // Reset
            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset All",
                    tint = SazfloyTextSecondary
                )
            }

            // Export Button
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.testTag("photo_export_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekspor", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun PhotoPreviewArea(
    bitmap: Bitmap?,
    isLoading: Boolean,
    adjustments: ColorAdjustments,
    isComparing: Boolean,
    rotationDegrees: Int,
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    textOverlays: List<TextOverlay>,
    stickerOverlays: List<StickerOverlay>,
    shapeOverlays: List<ShapeOverlay> = emptyList()
) {
    if (isLoading || bitmap == null) {
        CircularProgressIndicator(color = SazfloyCyan)
        return
    }

    val colorFilter = remember(adjustments, isComparing) {
        if (isComparing) {
            ColorFilter.colorMatrix(ColorMatrix(SazfloyColorEngine.IDENTITY_MATRIX))
        } else {
            val matrix = SazfloyColorEngine.createCompositeMatrix(adjustments)
            ColorFilter.colorMatrix(ColorMatrix(matrix))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, SazfloyBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Display base image with real color matrix filter
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Photo Preview",
            colorFilter = colorFilter,
            modifier = Modifier.fillMaxSize()
        )

        if (!isComparing) {
            // Render Shapes Preview
            shapeOverlays.forEach { shape ->
                val shapeColor = runCatching { Color(android.graphics.Color.parseColor(shape.fillColorHex)) }.getOrDefault(SazfloyToska)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(
                            width = (120 * shape.widthRatio * 2.5f).coerceIn(40f, 280f).dp,
                            height = (120 * shape.heightRatio * 2.5f).coerceIn(40f, 280f).dp
                        )
                        .clip(
                            when (shape.type) {
                                ShapeType.CIRCLE -> CircleShape
                                ShapeType.ROUNDED_RECT -> RoundedCornerShape(16.dp)
                                else -> RoundedCornerShape(4.dp)
                            }
                        )
                        .background(shapeColor.copy(alpha = shape.opacity.coerceIn(0.1f, 1f)))
                        .border(
                            width = shape.strokeWidth.dp.coerceAtLeast(1.dp),
                            color = runCatching { Color(android.graphics.Color.parseColor(shape.strokeColorHex)) }.getOrDefault(Color.White),
                            shape = when (shape.type) {
                                ShapeType.CIRCLE -> CircleShape
                                ShapeType.ROUNDED_RECT -> RoundedCornerShape(16.dp)
                                else -> RoundedCornerShape(4.dp)
                            }
                        )
                )
            }

            // Overlay Text Preview
            textOverlays.forEach { textItem ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 40.dp)
                        .background(
                            Color(android.graphics.Color.parseColor(textItem.backgroundColorHex)),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = textItem.text,
                        color = Color(android.graphics.Color.parseColor(textItem.fontColorHex)),
                        fontWeight = if (textItem.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = SazfloyFonts.getFontFamily(textItem.fontFamilyName),
                        fontSize = textItem.fontSizeSp.sp
                    )
                }
            }

            // Overlay Sticker Preview
            stickerOverlays.forEach { sticker ->
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = sticker.emojiOrTag, fontSize = 48.sp)
                }
            }
        }

        // Before/After indicator badge
        if (isComparing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SazfloyAmber)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ORIGINAL (BEFORE)",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}


@Composable
private fun PhotoEditorTabsRow(
    selectedTab: PhotoEditorTab,
    onSelectTab: (PhotoEditorTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(SazfloySurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(PhotoEditorTab.values()) { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) SazfloyCyan else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) SazfloyBackground else SazfloyTextSecondary
                )
            }
        }
    }
}

@Composable
private fun PhotoEditorTabContent(
    selectedTab: PhotoEditorTab,
    adjustments: ColorAdjustments,
    onUpdateAdjustments: (ColorAdjustments) -> Unit,
    rotationDegrees: Int,
    onRotate: () -> Unit,
    isFlippedHorizontal: Boolean,
    onToggleFlipH: () -> Unit,
    isFlippedVertical: Boolean,
    onToggleFlipV: () -> Unit,
    onAddText: () -> Unit,
    textOverlays: List<TextOverlay>,
    onRemoveText: (String) -> Unit,
    onUpdateText: (TextOverlay) -> Unit = {},
    stickerOverlays: List<StickerOverlay>,
    onAddSticker: (String) -> Unit,
    onRemoveSticker: (String) -> Unit,
    shapeOverlays: List<ShapeOverlay> = emptyList(),
    onAddShape: (ShapeOverlay) -> Unit = {},
    onRemoveShape: (String) -> Unit = {},
    onUpdateShape: (ShapeOverlay) -> Unit = {},
    onMoveLayerUp: (String) -> Unit = {},
    onMoveLayerDown: (String) -> Unit = {},
    addWatermark: Boolean,
    onToggleWatermark: () -> Unit,
    onImportLutClick: () -> Unit = {}
) {
    when (selectedTab) {
        PhotoEditorTab.COLOR_WHEELS -> {
            DaVinciColorWheelsComponent(
                colorWheels = adjustments.colorWheels,
                onUpdateWheels = { newWheels ->
                    onUpdateAdjustments(adjustments.copy(colorWheels = newWheels))
                }
            )
        }
        PhotoEditorTab.LUTS -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hollywood & Film 3D LUTs (.cube)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SazfloyTextSecondary
                    )
                    OutlinedButton(
                        onClick = onImportLutClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Import .CUBE", fontSize = 10.sp, color = SazfloyToska)
                    }
                }

                if (adjustments.activeLutId != "none") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intensitas LUT: ${(adjustments.lutIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = SazfloyCyan
                        )
                        Slider(
                            value = adjustments.lutIntensity,
                            onValueChange = { onUpdateAdjustments(adjustments.copy(lutIntensity = it)) },
                            valueRange = 0f..1f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        val isNone = adjustments.activeLutId == "none"
                        Box(
                            modifier = Modifier
                                .width(95.dp)
                                .height(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNone) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable { onUpdateAdjustments(adjustments.copy(activeLutId = "none")) }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Original\n(No LUT)",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNone) SazfloyBackground else SazfloyTextPrimary
                            )
                        }
                    }
                    items(LutEngine.getAllLuts()) { lut ->
                        val isSel = adjustments.activeLutId == lut.id
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .height(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SazfloySurfaceElevated else SazfloySurfaceVariant)
                                .border(if (isSel) 2.dp else 1.dp, if (isSel) SazfloyCyan else SazfloyBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateAdjustments(adjustments.copy(activeLutId = lut.id)) }
                                .padding(6.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Text(lut.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) SazfloyCyan else SazfloyTextPrimary, maxLines = 1)
                                Text(lut.category, fontSize = 8.sp, color = SazfloyTextMuted)
                                if (lut.isCustom) {
                                    Text(".cube custom", fontSize = 8.sp, color = SazfloyToska, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        PhotoEditorTab.FILTERS -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Filter intensity slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Filter Strength", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                    Text("${(adjustments.filterIntensity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = SazfloyCyan)
                }
                Slider(
                    value = adjustments.filterIntensity,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(filterIntensity = it)) },
                    colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                )

                // 10 SAZFLOY Presets Horizontal List
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SazfloyColorEngine.PRESETS) { preset ->
                        val isSelected = adjustments.filterPresetId == preset.id
                        Box(
                            modifier = Modifier
                                .width(78.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SazfloySurfaceElevated else SazfloySurfaceVariant)
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) SazfloyCyan else SazfloyBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onUpdateAdjustments(adjustments.copy(filterPresetId = preset.id))
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = preset.name.replace("SAZFLOY ", ""),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) SazfloyCyan else SazfloyTextPrimary,
                                    maxLines = 2
                                )
                                Text(
                                    text = preset.category,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = SazfloyTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
        PhotoEditorTab.ADJUST -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AdjustmentSlider(
                    label = "Brightness",
                    value = adjustments.brightness,
                    range = -100f..100f,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(brightness = it)) }
                )
                AdjustmentSlider(
                    label = "Contrast",
                    value = adjustments.contrast,
                    range = -100f..100f,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(contrast = it)) }
                )
                AdjustmentSlider(
                    label = "Saturation",
                    value = adjustments.saturation,
                    range = -100f..100f,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(saturation = it)) }
                )
                AdjustmentSlider(
                    label = "Blur Efek",
                    value = adjustments.blurRadius,
                    range = 0f..25f,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(blurRadius = it)) }
                )
                AdjustmentSlider(
                    label = "Warmth / Temp",
                    value = adjustments.temperature,
                    range = -100f..100f,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(temperature = it)) }
                )
                AdjustmentSlider(
                    label = "Tint",
                    value = adjustments.tint,
                    range = -100f..100f,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(tint = it)) }
                )
                AdjustmentSlider(
                    label = "Vignette",
                    value = adjustments.vignette,
                    range = 0f..100f,
                    onValueChange = { onUpdateAdjustments(adjustments.copy(vignette = it)) }
                )
            }
        }
        PhotoEditorTab.CROP_ROTATE -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Transform Geometry", style = MaterialTheme.typography.labelMedium, color = SazfloyTextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRotate,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotate 90°", color = SazfloyTextPrimary)
                    }

                    Button(
                        onClick = onToggleFlipH,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFlippedHorizontal) SazfloyCyan else SazfloySurfaceVariant
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flip H", color = if (isFlippedHorizontal) SazfloyBackground else SazfloyTextPrimary)
                    }

                    Button(
                        onClick = onToggleFlipV,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFlippedVertical) SazfloyCyan else SazfloySurfaceVariant
                        )
                    ) {
                        Text("Flip V", color = if (isFlippedVertical) SazfloyBackground else SazfloyTextPrimary)
                    }
                }
            }
        }
        PhotoEditorTab.TEXT -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAddText,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Text Overlay")
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(textOverlays) { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SazfloySurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.text, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "✕",
                                    color = SazfloyPink,
                                    modifier = Modifier.clickable { onRemoveText(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        PhotoEditorTab.SHAPES -> {
            var selectedShapeType by remember { mutableStateOf(ShapeType.RECTANGLE) }
            var shapeColorHex by remember { mutableStateOf("#00E5BC") }
            var shapeStrokeWidth by remember { mutableFloatStateOf(2f) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Pilih Bentuk Geometris", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                // Shape Type selector buttons
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ShapeType.values()) { st ->
                        val isSel = selectedShapeType == st
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable { selectedShapeType = st }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = st.name.replace("_", " "),
                                color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                // Color choices
                Text("Warna Bentuk", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#00E5BC", "#FFFFFF", "#FFB300", "#FF2D75", "#7C4DFF", "#000000").forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(if (shapeColorHex == hex) 2.dp else 1.dp, Color.White, CircleShape)
                                .clickable { shapeColorHex = hex }
                        )
                    }
                }

                // Add button
                Button(
                    onClick = {
                        onAddShape(
                            ShapeOverlay(
                                type = selectedShapeType,
                                fillColorHex = shapeColorHex,
                                strokeWidth = shapeStrokeWidth
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambahkan Bentuk ke Foto")
                }

                // Placed Shapes List
                if (shapeOverlays.isNotEmpty()) {
                    Text("Bentuk Aktif (${shapeOverlays.size})", style = MaterialTheme.typography.labelSmall, color = SazfloyTextMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(shapeOverlays) { shape ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SazfloySurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(shape.type.name, color = SazfloyCyan, style = MaterialTheme.typography.labelSmall)
                                    Text("✕", color = SazfloyPink, modifier = Modifier.clickable { onRemoveShape(shape.id) })
                                }
                            }
                        }
                    }
                }
            }
        }
        PhotoEditorTab.LAYERS -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pengelola Layer (Susunan & Opasitas)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyCyan
                )

                // List shape layers
                shapeOverlays.forEachIndexed { idx, shape ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SazfloySurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Bentuk: ${shape.type.name}", color = SazfloyTextPrimary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Opasitas: ${(shape.opacity * 100).toInt()}%", color = SazfloyTextMuted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { onMoveLayerUp(shape.id) }, modifier = Modifier.size(28.dp)) {
                                Text("▲", color = SazfloyCyan, fontSize = 12.sp)
                            }
                            IconButton(onClick = { onMoveLayerDown(shape.id) }, modifier = Modifier.size(28.dp)) {
                                Text("▼", color = SazfloyCyan, fontSize = 12.sp)
                            }
                            IconButton(onClick = { onRemoveShape(shape.id) }, modifier = Modifier.size(28.dp)) {
                                Text("✕", color = SazfloyPink, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // List text layers
                textOverlays.forEachIndexed { idx, textItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SazfloySurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Teks: ${textItem.text.take(15)}", color = SazfloyTextPrimary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text("${textItem.fontSizeSp.toInt()}sp", color = SazfloyTextMuted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { onMoveLayerUp(textItem.id) }, modifier = Modifier.size(28.dp)) {
                                Text("▲", color = SazfloyAmber, fontSize = 12.sp)
                            }
                            IconButton(onClick = { onMoveLayerDown(textItem.id) }, modifier = Modifier.size(28.dp)) {
                                Text("▼", color = SazfloyAmber, fontSize = 12.sp)
                            }
                            IconButton(onClick = { onRemoveText(textItem.id) }, modifier = Modifier.size(28.dp)) {
                                Text("✕", color = SazfloyPink, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // List sticker layers
                stickerOverlays.forEach { sticker ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SazfloySurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Stiker: ${sticker.emojiOrTag}", color = SazfloyTextPrimary, style = MaterialTheme.typography.labelSmall)
                        IconButton(onClick = { onRemoveSticker(sticker.id) }, modifier = Modifier.size(28.dp)) {
                            Text("✕", color = SazfloyPink, fontSize = 12.sp)
                        }
                    }
                }

                if (shapeOverlays.isEmpty() && textOverlays.isEmpty() && stickerOverlays.isEmpty()) {
                    Text(
                        "Belum ada layer tambahan. Tambahkan Teks, Bentuk, atau Stiker untuk mengelola urutan.",
                        color = SazfloyTextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        PhotoEditorTab.STICKERS -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tap emoji to add sticker", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                val emojis = listOf("✨", "🔥", "❤️", "⚡", "🎬", "📸", "🌟", "🌸", "👑", "🌈", "💯", "🎉")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SazfloySurfaceVariant)
                                .clickable { onAddSticker(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SazfloyTextSecondary,
            modifier = Modifier.width(100.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
        )
        Text(
            text = "${value.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = SazfloyCyan,
            modifier = Modifier.width(36.dp)
        )
    }
}

