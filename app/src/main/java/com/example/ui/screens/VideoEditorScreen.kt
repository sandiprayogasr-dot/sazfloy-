package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.editor.timeline.TimelineComponent
import com.example.editor.video.VideoExportEngine
import com.example.filters.SazfloyColorEngine
import com.example.model.AspectRatioMode
import com.example.model.AudioTrack
import com.example.model.ClipType
import com.example.model.ColorAdjustments
import com.example.model.EditingProject
import com.example.model.ExportConfiguration
import com.example.model.MediaClip
import com.example.model.ScreenOrientationMode
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import com.example.model.TitleTemplate
import com.example.model.TransitionType
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
import com.example.ui.theme.SazfloyViolet
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.example.model.AnimationCurve
import com.example.model.Keyframe
import com.example.model.ShapeOverlay
import com.example.model.ShapeType
import com.example.ui.theme.SazfloyToska
import com.example.editor.components.DaVinciColorWheelsComponent
import com.example.editor.templates.TitleTemplates
import com.example.editor.audio.AutoCaptionEngine
import com.example.editor.audio.CaptionStylePreset
import com.example.filters.LutEngine
import com.example.filters.LutProfile
import com.example.ui.theme.SazfloyFonts
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.model.EffectCategory
import com.example.model.EffectType
import com.example.model.VideoEffect
import com.example.ui.theme.SazfloyVioletLight

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import com.example.editor.audio.TextToSpeechEngine

enum class VideoEditorToolTab(val label: String) {
    CLIP_TRIM("Clip & Trim"),
    COLOR_WHEELS("DaVinci Wheels"),
    LUTS("3D LUTs"),
    EFFECTS("Efek Video"),
    TITLES("Judul Lengkap"),
    AUTO_CAPTIONS("Auto Text"),
    TTS_AUDIO("Audio AI / TTS"),
    TRANSITION("Transisi"),
    TEXT("Teks & Font"),
    FILTERS("Filters"),
    ADJUST("Color Adj"),
    SHAPES("Bentuk"),
    KEYFRAMES("Keyframes"),
    INSPECTOR("Panel Properti"),
    STICKERS("Stickers"),
    AUDIO("Audio"),
    SPEED_VOL("Speed & Vol"),
    CANVAS_RATIO("Ratio")
}

@Composable
fun VideoEditorScreen(
    project: EditingProject,
    orientationMode: ScreenOrientationMode,
    onToggleOrientation: (ScreenOrientationMode) -> Unit,
    onSaveProject: (EditingProject) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // AndroidX Media3 ExoPlayer for real hardware-accelerated playback
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    // Working Project State
    var currentProject by remember { mutableStateOf(project) }
    var selectedClipId by remember { mutableStateOf(project.clips.firstOrNull()?.id) }
    var selectedTextId by remember { mutableStateOf<String?>(null) }
    var selectedAudioId by remember { mutableStateOf<String?>(null) }
    var selectedShapeId by remember { mutableStateOf<String?>(null) }
    var selectedEffectId by remember { mutableStateOf<String?>(null) }

    // Media Import Dialog State
    var showAddMediaDialog by remember { mutableStateOf(false) }
    var isImportingMedia by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf("") }

    // Playback State
    var isPlaying by remember { mutableStateOf(false) }
    var currentPlayheadMs by remember { mutableLongStateOf(0L) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Active tool tab
    var activeTab by remember { mutableStateOf(VideoEditorToolTab.CLIP_TRIM) }

    // Export Dialog State
    var showExportDialog by remember { mutableStateOf(false) }
    var exportProgressState by remember { mutableStateOf<VideoExportEngine.ExportProgress?>(null) }
    var exportJob by remember { mutableStateOf<Job?>(null) }

    // Dialog for adding Text
    var showAddTextDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var textInputFontId by remember { mutableStateOf("montserrat") }

    // Undo / Redo history stacks
    var undoStack by remember { mutableStateOf(listOf<EditingProject>()) }
    var redoStack by remember { mutableStateOf(listOf<EditingProject>()) }

    fun updateProject(transform: (EditingProject) -> EditingProject) {
        val next = transform(currentProject).copy(updatedAt = System.currentTimeMillis())
        if (next != currentProject) {
            undoStack = (undoStack + currentProject).takeLast(25)
            redoStack = emptyList()
            currentProject = next
        }
    }

    fun performUndo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.last()
            redoStack = (redoStack + currentProject).takeLast(25)
            undoStack = undoStack.dropLast(1)
            currentProject = prev
        }
    }

    fun performRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.last()
            undoStack = (undoStack + currentProject).takeLast(25)
            redoStack = redoStack.dropLast(1)
            currentProject = next
        }
    }

    // .cube 3D LUT File Picker Launcher
    val lutFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val fileName = uri.lastPathSegment ?: "Custom_LUT.cube"
                    val parsed = LutEngine.parseCube(stream, fileName)
                    val updatedAdj = currentProject.adjustments.copy(activeLutId = parsed.id, lutIntensity = 1.0f)
                    updateProject { it.copy(adjustments = updatedAdj) }
                    Toast.makeText(context, "3D LUT Berhasil Diimpor: ${parsed.name}", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { err ->
                Toast.makeText(context, "Gagal memproses file .cube: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importMediaUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        coroutineScope.launch {
            isImportingMedia = true
            importStatusMessage = "Mengimpor ${uris.size} media..."
            try {
                val newClips = uris.mapIndexed { idx, uri ->
                    importStatusMessage = "Memproses berkas ${idx + 1} dari ${uris.size}..."
                    com.example.media.MediaManager.importMediaAuto(context, uri)
                }
                updateProject { proj ->
                    proj.copy(clips = proj.clips + newClips)
                }
                Toast.makeText(context, "${newClips.size} media berhasil ditambahkan ke timeline", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengimpor media: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isImportingMedia = false
                importStatusMessage = ""
                showAddMediaDialog = false
            }
        }
    }

    // Media Picker Launchers for Photo, Video, and All
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> importMediaUris(uris) }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> importMediaUris(uris) }

    val pickAllLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> importMediaUris(uris) }

    // Audio Picker Launcher to add custom soundtrack
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val track = com.example.media.MediaManager.importAudio(context, uri)
                    updateProject { proj ->
                        proj.copy(audioTracks = proj.audioTracks + track)
                    }
                    Toast.makeText(context, "Audio \"${track.title}\" berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Gagal mengimpor audio: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Auto-save on exit
    BackHandler {
        onSaveProject(currentProject)
        onNavigateBack()
    }

    fun seekToStart() {
        currentPlayheadMs = 0L
        val firstClip = currentProject.clips.firstOrNull()
        if (firstClip != null && firstClip.type == ClipType.VIDEO) {
            exoPlayer.seekTo(firstClip.trimStartMs)
        } else {
            exoPlayer.seekTo(0L)
        }
    }

    fun togglePlayback() {
        val total = currentProject.totalDurationMs.coerceAtLeast(1000L)
        if (!isPlaying && currentPlayheadMs >= total - 100L) {
            seekToStart()
            isPlaying = true
        } else {
            isPlaying = !isPlaying
        }
    }

    // Playback ticking loop synchronized with ExoPlayer
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val total = currentProject.totalDurationMs.coerceAtLeast(1000L)
            if (currentPlayheadMs >= total) {
                currentPlayheadMs = 0L
                exoPlayer.seekTo(0L)
            }
            exoPlayer.play()
            while (isActive && isPlaying) {
                delay(33L) // ~30 fps ticking
                currentPlayheadMs += 33L
                if (currentPlayheadMs >= total) {
                    currentPlayheadMs = total
                    isPlaying = false
                }
            }
        } else {
            exoPlayer.pause()
        }
    }

    // Selected Clip
    val selectedClip = remember(currentProject.clips, selectedClipId) {
        currentProject.clips.firstOrNull { it.id == selectedClipId }
    }

    fun moveClipLeft(clipId: String) {
        updateProject { proj ->
            val idx = proj.clips.indexOfFirst { it.id == clipId }
            if (idx > 0) {
                val list = proj.clips.toMutableList()
                val item = list.removeAt(idx)
                list.add(idx - 1, item)
                proj.copy(clips = list)
            } else proj
        }
    }

    fun moveClipRight(clipId: String) {
        updateProject { proj ->
            val idx = proj.clips.indexOfFirst { it.id == clipId }
            if (idx >= 0 && idx < proj.clips.lastIndex) {
                val list = proj.clips.toMutableList()
                val item = list.removeAt(idx)
                list.add(idx + 1, item)
                proj.copy(clips = list)
            } else proj
        }
    }

    fun moveClipToFront(clipId: String) {
        updateProject { proj ->
            val idx = proj.clips.indexOfFirst { it.id == clipId }
            if (idx > 0) {
                val list = proj.clips.toMutableList()
                val item = list.removeAt(idx)
                list.add(0, item)
                proj.copy(clips = list)
            } else proj
        }
    }

    fun moveClipToEnd(clipId: String) {
        updateProject { proj ->
            val idx = proj.clips.indexOfFirst { it.id == clipId }
            if (idx >= 0 && idx < proj.clips.lastIndex) {
                val list = proj.clips.toMutableList()
                val item = list.removeAt(idx)
                list.add(item)
                proj.copy(clips = list)
            } else proj
        }
    }

    fun toggleBeatMarker(timeMs: Long) {
        updateProject { proj ->
            val existing = proj.beatMarkersMs.firstOrNull { kotlin.math.abs(it - timeMs) <= 120L }
            val newList = if (existing != null) {
                proj.beatMarkersMs.filter { it != existing }
            } else {
                (proj.beatMarkersMs + timeMs).sorted()
            }
            proj.copy(beatMarkersMs = newList)
        }
    }

    fun autoGenerateBeats() {
        updateProject { proj ->
            val totalMs = proj.totalDurationMs.coerceAtLeast(1000L)
            val beats = (500L..totalMs step 500L).toList()
            proj.copy(beatMarkersMs = beats)
        }
    }

    fun addKeyframeAtPlayhead(targetId: String) {
        updateProject { proj ->
            val shape = proj.shapeOverlays.firstOrNull { it.id == targetId }
            if (shape != null) {
                val newKf = Keyframe(
                    timeMs = currentPlayheadMs,
                    posXRatio = shape.posXRatio,
                    posYRatio = shape.posYRatio,
                    scale = 1f,
                    rotation = shape.rotation,
                    opacity = shape.opacity
                )
                proj.copy(
                    shapeOverlays = proj.shapeOverlays.map {
                        if (it.id == targetId) it.copy(keyframes = it.keyframes + newKf) else it
                    }
                )
            } else {
                val text = proj.textOverlays.firstOrNull { it.id == targetId }
                if (text != null) {
                    val newKf = Keyframe(
                        timeMs = currentPlayheadMs,
                        posXRatio = text.posXRatio,
                        posYRatio = text.posYRatio
                    )
                    proj.copy(
                        textOverlays = proj.textOverlays.map {
                            if (it.id == targetId) it.copy(keyframes = it.keyframes + newKf) else it
                        }
                    )
                } else {
                    val clip = proj.clips.firstOrNull { it.id == targetId }
                    if (clip != null) {
                        val newKf = Keyframe(timeMs = currentPlayheadMs)
                        proj.copy(
                            clips = proj.clips.map {
                                if (it.id == targetId) it.copy(keyframes = it.keyframes + newKf) else it
                            }
                        )
                    } else proj
                }
            }
        }
    }


    Scaffold(
        containerColor = SazfloyBackground,
        topBar = {
            if (!isFullscreen) {
                VideoEditorTopBar(
                    title = currentProject.title,
                    aspectRatio = currentProject.aspectRatio,
                    orientationMode = orientationMode,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty(),
                    onUndo = { performUndo() },
                    onRedo = { performRedo() },
                    onToggleOrientation = onToggleOrientation,
                    onAddMedia = { showAddMediaDialog = true },
                    onExport = { showExportDialog = true },
                    onNavigateBack = {
                        onSaveProject(currentProject)
                        onNavigateBack()
                    }
                )
            }
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("video_editor_root")
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // LANDSCAPE MODE: Preview left, Timeline & Tools stacked on right
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left: Large Preview
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VideoPreviewBox(
                            project = currentProject,
                            currentPlayheadMs = currentPlayheadMs,
                            isPlaying = isPlaying,
                            isMuted = isMuted,
                            isFullscreen = isFullscreen,
                            exoPlayer = exoPlayer,
                            selectedTextId = selectedTextId,
                            onSelectText = {
                                selectedTextId = it
                                activeTab = VideoEditorToolTab.TEXT
                            },
                            onUpdateText = { updated ->
                                updateProject { proj ->
                                    proj.copy(textOverlays = proj.textOverlays.map { if (it.id == updated.id) updated else it })
                                }
                            },
                            onDeleteText = { textId ->
                                updateProject { proj ->
                                    proj.copy(textOverlays = proj.textOverlays.filter { it.id != textId })
                                }
                                if (selectedTextId == textId) selectedTextId = null
                            },
                            onSeekToStart = { seekToStart() },
                            onTogglePlay = { togglePlayback() },
                            onToggleMute = { isMuted = !isMuted },
                            onToggleFullscreen = { isFullscreen = !isFullscreen },
                            onUpdateClip = { updated ->
                                updateProject { proj ->
                                    proj.copy(clips = proj.clips.map { if (it.id == updated.id) updated else it })
                                }
                            }
                        )
                    }

                    // Right: Multi-track Timeline + Editing Panels
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .background(SazfloySurface)
                            .border(1.dp, SazfloyBorder)
                    ) {
                        // Timeline
                        TimelineComponent(
                            totalDurationMs = currentProject.totalDurationMs,
                            currentPlayheadMs = currentPlayheadMs,
                            clips = currentProject.clips,
                            selectedClipId = selectedClipId,
                            textOverlays = currentProject.textOverlays,
                            selectedTextId = selectedTextId,
                            audioTracks = currentProject.audioTracks,
                            selectedAudioId = selectedAudioId,
                            shapeOverlays = currentProject.shapeOverlays,
                            selectedShapeId = selectedShapeId,
                            effects = currentProject.effects,
                            selectedEffectId = selectedEffectId,
                            onSeek = { currentPlayheadMs = it },
                            onSelectClip = {
                                selectedClipId = it
                                activeTab = VideoEditorToolTab.CLIP_TRIM
                            },
                            onSelectText = {
                                selectedTextId = it
                                activeTab = VideoEditorToolTab.TEXT
                            },
                            onSelectAudio = {
                                selectedAudioId = it
                                activeTab = VideoEditorToolTab.AUDIO
                            },
                            onSelectShape = {
                                selectedShapeId = it
                                activeTab = VideoEditorToolTab.SHAPES
                            },
                            onSelectEffect = {
                                selectedEffectId = it
                                activeTab = VideoEditorToolTab.EFFECTS
                            },
                            onSelectTransition = {
                                selectedClipId = it
                                activeTab = VideoEditorToolTab.TRANSITION
                            },
                            onSplitClip = { clipId, atMs ->
                                updateProject { it.splitClip(clipId, atMs) }
                            },
                            onDeleteClip = { clipId ->
                                updateProject { it.deleteClip(clipId) }
                                selectedClipId = currentProject.clips.firstOrNull()?.id
                            },
                            onDuplicateClip = { clipId ->
                                updateProject { it.duplicateClip(clipId) }
                            },
                            onMoveClipLeft = { moveClipLeft(it) },
                            onMoveClipRight = { moveClipRight(it) },
                            onAddKeyframe = { addKeyframeAtPlayhead(it) },
                            onAddMediaClick = {
                                showAddMediaDialog = true
                            },
                            beatMarkersMs = currentProject.beatMarkersMs,
                            onToggleBeatMarker = { toggleBeatMarker(it) },
                            onAutoBeats = { autoGenerateBeats() },
                            modifier = Modifier.weight(1f)
                        )

                        // Tool Tabs & Panels
                        VideoToolTabsRow(
                            activeTab = activeTab,
                            onSelectTab = { activeTab = it }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(SazfloySurfaceElevated)
                                .padding(8.dp)
                        ) {
                            VideoToolPanelContent(
                                activeTab = activeTab,
                                project = currentProject,
                                selectedClip = selectedClip,
                                selectedTextId = selectedTextId,
                                selectedShapeId = selectedShapeId,
                                selectedEffectId = selectedEffectId,
                                currentPlayheadMs = currentPlayheadMs,
                                onUpdateClip = { updatedClip ->
                                    updateProject { proj ->
                                        proj.copy(clips = proj.clips.map { if (it.id == updatedClip.id) updatedClip else it })
                                    }
                                },
                                onUpdateProject = { updateProject { it } },
                                onUpdateAdjustments = { newAdj ->
                                    updateProject { it.copy(adjustments = newAdj) }
                                },
                                onAddTextClick = { showAddTextDialog = true },
                                onUpdateText = { updated ->
                                    updateProject { proj ->
                                        proj.copy(textOverlays = proj.textOverlays.map { if (it.id == updated.id) updated else it })
                                    }
                                },
                                onAddShape = { shape ->
                                    updateProject { it.copy(shapeOverlays = it.shapeOverlays + shape) }
                                },
                                onRemoveShape = { shapeId ->
                                    updateProject { it.copy(shapeOverlays = it.shapeOverlays.filter { it.id != shapeId }) }
                                },
                                onUpdateShape = { updated ->
                                    updateProject { proj ->
                                        proj.copy(shapeOverlays = proj.shapeOverlays.map { if (it.id == updated.id) updated else it })
                                    }
                                },
                                onAddEffect = { eff ->
                                    updateProject { it.copy(effects = it.effects + eff) }
                                    selectedEffectId = eff.id
                                },
                                onSelectEffect = { selectedEffectId = it },
                                onUpdateEffect = { updated ->
                                    updateProject { proj ->
                                        proj.copy(effects = proj.effects.map { if (it.id == updated.id) updated else it })
                                    }
                                },
                                onRemoveEffect = { effId ->
                                    updateProject { it.copy(effects = it.effects.filter { it.id != effId }) }
                                    if (selectedEffectId == effId) selectedEffectId = null
                                },
                                onAddSticker = { emoji ->
                                    updateProject {
                                        it.copy(stickerOverlays = it.stickerOverlays + StickerOverlay(emojiOrTag = emoji, startMs = currentPlayheadMs))
                                    }
                                },
                                onImportLutClick = { lutFilePickerLauncher.launch("*/*") },
                                onApplyTitleTemplate = { template ->
                                    val newOverlays = TitleTemplates.createTitleOverlays(template, currentPlayheadMs)
                                    updateProject { it.copy(textOverlays = it.textOverlays + newOverlays) }
                                    Toast.makeText(context, "Judul \"${template.name}\" berhasil dipasang!", Toast.LENGTH_SHORT).show()
                                },
                                onGenerateAutoCaptions = { style ->
                                    val generated = AutoCaptionEngine.generateAutoCaptions(currentProject, style)
                                    updateProject { it.copy(textOverlays = it.textOverlays + generated) }
                                    Toast.makeText(context, "${generated.size} Subtitle otomatis berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                },
                                onImportAudioClick = { audioPickerLauncher.launch("audio/*") },
                                onMoveClipLeft = { moveClipLeft(it) },
                                onMoveClipRight = { moveClipRight(it) },
                                onMoveClipToFront = { moveClipToFront(it) },
                                onMoveClipToEnd = { moveClipToEnd(it) },
                                onToggleBeatMarker = { toggleBeatMarker(it) },
                                onAutoBeats = { autoGenerateBeats() },
                                onSeek = { currentPlayheadMs = it; exoPlayer.seekTo(it) }
                            )
                        }

                    }
                }
            } else {
                // PORTRAIT MODE: Preview top, Timeline middle, Editing tools bottom
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.1f)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VideoPreviewBox(
                            project = currentProject,
                            currentPlayheadMs = currentPlayheadMs,
                            isPlaying = isPlaying,
                            isMuted = isMuted,
                            isFullscreen = isFullscreen,
                            exoPlayer = exoPlayer,
                            selectedTextId = selectedTextId,
                            onSelectText = {
                                selectedTextId = it
                                activeTab = VideoEditorToolTab.TEXT
                            },
                            onUpdateText = { updated ->
                                updateProject { proj ->
                                    proj.copy(textOverlays = proj.textOverlays.map { if (it.id == updated.id) updated else it })
                                }
                            },
                            onDeleteText = { textId ->
                                updateProject { proj ->
                                    proj.copy(textOverlays = proj.textOverlays.filter { it.id != textId })
                                }
                                if (selectedTextId == textId) selectedTextId = null
                            },
                            onSeekToStart = { seekToStart() },
                            onTogglePlay = { togglePlayback() },
                            onToggleMute = { isMuted = !isMuted },
                            onToggleFullscreen = { isFullscreen = !isFullscreen },
                            onUpdateClip = { updated ->
                                updateProject { proj ->
                                    proj.copy(clips = proj.clips.map { if (it.id == updated.id) updated else it })
                                }
                            }
                        )
                    }

                    // 2. Multi-track Timeline
                    TimelineComponent(
                        totalDurationMs = currentProject.totalDurationMs,
                        currentPlayheadMs = currentPlayheadMs,
                        clips = currentProject.clips,
                        selectedClipId = selectedClipId,
                        textOverlays = currentProject.textOverlays,
                        selectedTextId = selectedTextId,
                        audioTracks = currentProject.audioTracks,
                        selectedAudioId = selectedAudioId,
                        shapeOverlays = currentProject.shapeOverlays,
                        selectedShapeId = selectedShapeId,
                        effects = currentProject.effects,
                        selectedEffectId = selectedEffectId,
                        onSeek = { currentPlayheadMs = it },
                        onSelectClip = {
                            selectedClipId = it
                            activeTab = VideoEditorToolTab.CLIP_TRIM
                        },
                        onSelectText = {
                            selectedTextId = it
                            activeTab = VideoEditorToolTab.TEXT
                        },
                        onSelectAudio = {
                            selectedAudioId = it
                            activeTab = VideoEditorToolTab.AUDIO
                        },
                        onSelectShape = {
                            selectedShapeId = it
                            activeTab = VideoEditorToolTab.SHAPES
                        },
                        onSelectEffect = {
                            selectedEffectId = it
                            activeTab = VideoEditorToolTab.EFFECTS
                        },
                        onSelectTransition = {
                            selectedClipId = it
                            activeTab = VideoEditorToolTab.TRANSITION
                        },
                        onSplitClip = { clipId, atMs ->
                            updateProject { it.splitClip(clipId, atMs) }
                        },
                        onDeleteClip = { clipId ->
                            updateProject { it.deleteClip(clipId) }
                            selectedClipId = currentProject.clips.firstOrNull()?.id
                        },
                        onDuplicateClip = { clipId ->
                            updateProject { it.duplicateClip(clipId) }
                        },
                        onMoveClipLeft = { moveClipLeft(it) },
                        onMoveClipRight = { moveClipRight(it) },
                        onAddKeyframe = { addKeyframeAtPlayhead(it) },
                        onAddMediaClick = {
                            showAddMediaDialog = true
                        },
                        beatMarkersMs = currentProject.beatMarkersMs,
                        onToggleBeatMarker = { toggleBeatMarker(it) },
                        onAutoBeats = { autoGenerateBeats() },
                        modifier = Modifier.height(180.dp)
                    )

                    // 3. Tool Tabs & Bottom Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SazfloySurface)
                            .border(1.dp, SazfloyBorder)
                    ) {
                        VideoToolTabsRow(
                            activeTab = activeTab,
                            onSelectTab = { activeTab = it }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(SazfloySurfaceElevated)
                                .padding(8.dp)
                        ) {
                            VideoToolPanelContent(
                                activeTab = activeTab,
                                project = currentProject,
                                selectedClip = selectedClip,
                                selectedTextId = selectedTextId,
                                selectedShapeId = selectedShapeId,
                                selectedEffectId = selectedEffectId,
                                currentPlayheadMs = currentPlayheadMs,
                                onUpdateClip = { updatedClip ->
                                    updateProject { proj ->
                                        proj.copy(clips = proj.clips.map { if (it.id == updatedClip.id) updatedClip else it })
                                    }
                                },
                                onUpdateProject = { updateProject { it } },
                                onUpdateAdjustments = { newAdj ->
                                    updateProject { it.copy(adjustments = newAdj) }
                                },
                                onAddTextClick = { showAddTextDialog = true },
                                onUpdateText = { updated ->
                                    updateProject { proj ->
                                        proj.copy(textOverlays = proj.textOverlays.map { if (it.id == updated.id) updated else it })
                                    }
                                },
                                onAddShape = { shape ->
                                    updateProject { it.copy(shapeOverlays = it.shapeOverlays + shape) }
                                },
                                onRemoveShape = { shapeId ->
                                    updateProject { it.copy(shapeOverlays = it.shapeOverlays.filter { it.id != shapeId }) }
                                },
                                onUpdateShape = { updated ->
                                    updateProject { proj ->
                                        proj.copy(shapeOverlays = proj.shapeOverlays.map { if (it.id == updated.id) updated else it })
                                    }
                                },
                                onAddEffect = { eff ->
                                    updateProject { it.copy(effects = it.effects + eff) }
                                    selectedEffectId = eff.id
                                },
                                onSelectEffect = { selectedEffectId = it },
                                onUpdateEffect = { updated ->
                                    updateProject { proj ->
                                        proj.copy(effects = proj.effects.map { if (it.id == updated.id) updated else it })
                                    }
                                },
                                onRemoveEffect = { effId ->
                                    updateProject { it.copy(effects = it.effects.filter { it.id != effId }) }
                                    if (selectedEffectId == effId) selectedEffectId = null
                                },
                                onAddSticker = { emoji ->
                                    updateProject {
                                        it.copy(stickerOverlays = it.stickerOverlays + StickerOverlay(emojiOrTag = emoji, startMs = currentPlayheadMs))
                                    }
                                },
                                onImportLutClick = { lutFilePickerLauncher.launch("*/*") },
                                onApplyTitleTemplate = { template ->
                                    val newOverlays = TitleTemplates.createTitleOverlays(template, currentPlayheadMs)
                                    updateProject { it.copy(textOverlays = it.textOverlays + newOverlays) }
                                    Toast.makeText(context, "Judul \"${template.name}\" berhasil dipasang!", Toast.LENGTH_SHORT).show()
                                },
                                onGenerateAutoCaptions = { style ->
                                    val generated = AutoCaptionEngine.generateAutoCaptions(currentProject, style)
                                    updateProject { it.copy(textOverlays = it.textOverlays + generated) }
                                    Toast.makeText(context, "${generated.size} Subtitle otomatis berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                },
                                onImportAudioClick = { audioPickerLauncher.launch("audio/*") },
                                onMoveClipLeft = { moveClipLeft(it) },
                                onMoveClipRight = { moveClipRight(it) },
                                onMoveClipToFront = { moveClipToFront(it) },
                                onMoveClipToEnd = { moveClipToEnd(it) },
                                onToggleBeatMarker = { toggleBeatMarker(it) },
                                onAutoBeats = { autoGenerateBeats() },
                                onSeek = { currentPlayheadMs = it; exoPlayer.seekTo(it) }
                            )
                        }

                    }
                }
            }

        }
    }

    // Add Text Dialog
    if (showAddTextDialog) {
        Dialog(onDismissRequest = { showAddTextDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SazfloySurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Text to Video", color = SazfloyTextPrimary, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Title or Subtitle...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Font Typography", color = SazfloyTextSecondary, style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SazfloyFonts.AVAILABLE_FONTS) { fontItem ->
                            val isSel = textInputFontId == fontItem.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                    .clickable { textInputFontId = fontItem.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fontItem.displayName,
                                    fontFamily = fontItem.family,
                                    color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddTextDialog = false }) {
                            Text("Cancel", color = SazfloyTextSecondary)
                        }
                        Button(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    updateProject {
                                        it.copy(
                                            textOverlays = it.textOverlays + TextOverlay(
                                                text = textInput,
                                                startMs = currentPlayheadMs,
                                                durationMs = 3000L,
                                                fontFamilyName = textInputFontId
                                            )
                                        )
                                    }
                                    textInput = ""
                                }
                                showAddTextDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah Media (Foto, Video, Foto & Video, Audio)
    if (showAddMediaDialog) {
        Dialog(onDismissRequest = { showAddMediaDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SazfloySurfaceElevated),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Tambah Media ke Timeline",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyTextPrimary
                    )
                    Text(
                        text = "Pilih jenis media yang ingin diimpor ke dalam proyek:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SazfloyTextSecondary
                    )

                    // Option 1: Foto
                    Button(
                        onClick = {
                            showAddMediaDialog = false
                            pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("📷", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Foto Saja", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SazfloyTextPrimary)
                                Text("JPG, PNG, WEBP dengan color grading & efek", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SazfloyTextMuted)
                            }
                        }
                    }

                    // Option 2: Video
                    Button(
                        onClick = {
                            showAddMediaDialog = false
                            pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("🎬", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Video Saja", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SazfloyTextPrimary)
                                Text("MP4, MOV, MKV dengan playback hardware & trim", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SazfloyTextMuted)
                            }
                        }
                    }

                    // Option 3: Foto & Video
                    Button(
                        onClick = {
                            showAddMediaDialog = false
                            pickAllLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloyToska.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyToska),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("🔀", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Foto & Video (Campuran)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SazfloyToska)
                                Text("Pilih beberapa foto dan video sekaligus", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SazfloyTextMuted)
                            }
                        }
                    }

                    // Option 4: Audio
                    Button(
                        onClick = {
                            showAddMediaDialog = false
                            audioPickerLauncher.launch("audio/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("🎵", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Audio / Musik", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SazfloyTextPrimary)
                                Text("MP3, WAV, AAC untuk latar musik timeline", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SazfloyTextMuted)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddMediaDialog = false }) {
                            Text("Tutup", color = SazfloyTextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Loading Dialog during Media Import & Thumbnail Generation
    if (isImportingMedia) {
        Dialog(onDismissRequest = { /* Cannot cancel during file I/O */ }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SazfloySurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = SazfloyToska,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "Memproses Media...",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = SazfloyTextPrimary
                        )
                        Text(
                            text = importStatusMessage.ifBlank { "Membuat thumbnail & membaca durasi..." },
                            style = MaterialTheme.typography.bodySmall,
                            color = SazfloyTextSecondary
                        )
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            project = currentProject,
            onDismiss = {
                showExportDialog = false
                exportProgressState = null
            },
            onStartExport = { config ->
                exportJob = coroutineScope.launch {
                    VideoExportEngine.exportVideo(context, currentProject, config) { progress ->
                        exportProgressState = progress
                    }
                }
            },
            exportState = exportProgressState,
            onCancelExport = {
                exportJob?.cancel()
                exportProgressState = null
                showExportDialog = false
            }
        )
    }
}

// Helper project extensions for timeline operations
private fun EditingProject.splitClip(clipId: String, atMs: Long): EditingProject {
    var accumulated = 0L
    val newClips = mutableListOf<MediaClip>()
    for (clip in clips) {
        val dur = clip.trimmedDurationMs
        if (clip.id == clipId && atMs > accumulated && atMs < accumulated + dur) {
            val localOffset = ((atMs - accumulated) * clip.speed).toLong()
            val clip1 = clip.copy(
                id = UUID.randomUUID().toString(),
                name = "${clip.name} (Part 1)",
                trimEndMs = clip.trimStartMs + localOffset
            )
            val clip2 = clip.copy(
                id = UUID.randomUUID().toString(),
                name = "${clip.name} (Part 2)",
                trimStartMs = clip.trimStartMs + localOffset
            )
            newClips.add(clip1)
            newClips.add(clip2)
        } else {
            newClips.add(clip)
        }
        accumulated += dur
    }
    return copy(clips = newClips)
}

private fun EditingProject.deleteClip(clipId: String): EditingProject {
    if (clips.size <= 1) return this
    return copy(clips = clips.filter { it.id != clipId })
}

private fun EditingProject.duplicateClip(clipId: String): EditingProject {
    val clip = clips.firstOrNull { it.id == clipId } ?: return this
    val copyClip = clip.copy(id = UUID.randomUUID().toString(), name = "${clip.name} Copy")
    val index = clips.indexOfFirst { it.id == clipId }
    val newClips = clips.toMutableList().apply { add(index + 1, copyClip) }
    return copy(clips = newClips)
}

@Composable
private fun VideoEditorTopBar(
    title: String,
    aspectRatio: AspectRatioMode,
    orientationMode: ScreenOrientationMode,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleOrientation: (ScreenOrientationMode) -> Unit,
    onAddMedia: () -> Unit,
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
            IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("video_editor_back")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SazfloyTextPrimary
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "Ratio: ${aspectRatio.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SazfloyCyan
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Undo Button
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(34.dp).testTag("video_editor_undo")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) SazfloyCyan else SazfloyTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Redo Button
            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(34.dp).testTag("video_editor_redo")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) SazfloyCyan else SazfloyTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Orientation Toggle
            IconButton(onClick = {
                val next = when (orientationMode) {
                    ScreenOrientationMode.AUTO -> ScreenOrientationMode.PORTRAIT
                    ScreenOrientationMode.PORTRAIT -> ScreenOrientationMode.LANDSCAPE
                    ScreenOrientationMode.LANDSCAPE -> ScreenOrientationMode.AUTO
                }
                onToggleOrientation(next)
            }) {
                Icon(
                    imageVector = Icons.Default.ScreenRotation,
                    contentDescription = "Orientation",
                    tint = SazfloyCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Prominent Add Media Button
            Button(
                onClick = onAddMedia,
                colors = ButtonDefaults.buttonColors(containerColor = SazfloyToska, contentColor = SazfloyBackground),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("video_editor_add_media_topbar")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("+ Media", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Export Button
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("video_editor_export_button")
            ) {
                Text("Export", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun VideoPreviewBox(
    project: EditingProject,
    currentPlayheadMs: Long,
    isPlaying: Boolean,
    isMuted: Boolean,
    isFullscreen: Boolean,
    exoPlayer: ExoPlayer,
    selectedTextId: String? = null,
    onSelectText: (String?) -> Unit = {},
    onUpdateText: (TextOverlay) -> Unit = {},
    onDeleteText: (String) -> Unit = {},
    onSeekToStart: () -> Unit = {},
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onUpdateClip: (MediaClip) -> Unit = {}
) {
    // Determine active clip at playhead and its start time in the timeline
    var accumulated = 0L
    var activeClip: MediaClip? = null
    var activeClipTimelineStart = 0L
    for (clip in project.clips) {
        val dur = clip.trimmedDurationMs
        if (currentPlayheadMs >= accumulated && currentPlayheadMs < accumulated + dur) {
            activeClip = clip
            activeClipTimelineStart = accumulated
            break
        }
        accumulated += dur
    }
    if (activeClip == null) {
        activeClip = project.clips.lastOrNull()
        activeClipTimelineStart = (project.totalDurationMs - (activeClip?.trimmedDurationMs ?: 0L)).coerceAtLeast(0L)
    }

    // Sync active clip to ExoPlayer (videos only)
    LaunchedEffect(activeClip?.id, activeClip?.uri, activeClip?.type) {
        val clip = activeClip
        if (clip != null && clip.type == ClipType.VIDEO && clip.uri.isNotBlank()) {
            runCatching {
                val mediaItem = MediaItem.fromUri(clip.uri)
                exoPlayer.setMediaItem(mediaItem)
                val localOffset = ((currentPlayheadMs - activeClipTimelineStart).coerceAtLeast(0L) * clip.speed).toLong()
                exoPlayer.seekTo(clip.trimStartMs + localOffset)
                exoPlayer.prepare()
                if (isPlaying) {
                    exoPlayer.play()
                }
            }
        } else {
            exoPlayer.stop()
        }
    }

    // Sync scrubbing and playhead seeking ONLY when paused to avoid stuttering during playback
    LaunchedEffect(currentPlayheadMs) {
        val clip = activeClip
        if (!isPlaying && clip != null && clip.type == ClipType.VIDEO && clip.uri.isNotBlank()) {
            val localOffset = ((currentPlayheadMs - activeClipTimelineStart).coerceAtLeast(0L) * clip.speed).toLong()
            val targetMs = clip.trimStartMs + localOffset
            val currentPos = exoPlayer.currentPosition
            if (kotlin.math.abs(currentPos - targetMs) > 100L) {
                exoPlayer.seekTo(targetMs)
            }
        }
    }

    // Sync volume and playback speed
    LaunchedEffect(isMuted, activeClip?.volume, activeClip?.speed) {
        exoPlayer.volume = if (isMuted) 0f else (activeClip?.volume ?: 1f)
        activeClip?.speed?.let { spd ->
            if (spd > 0f) {
                exoPlayer.playbackParameters = androidx.media3.common.PlaybackParameters(spd)
            }
        }
    }

    // Aspect ratio float with full standard support (including 4:3 and 3:4)
    val ratio = when (project.aspectRatio) {
        AspectRatioMode.RATIO_9_16 -> 9f / 16f
        AspectRatioMode.RATIO_16_9 -> 16f / 9f
        AspectRatioMode.RATIO_1_1 -> 1f
        AspectRatioMode.RATIO_4_5 -> 4f / 5f
        AspectRatioMode.RATIO_4_3 -> 4f / 3f
        AspectRatioMode.RATIO_3_4 -> 3f / 4f
        AspectRatioMode.RATIO_ORIGINAL -> 16f / 9f
    }

    // Per-clip or project Color Matrix Filter
    val clipAdjustments = activeClip?.adjustments?.takeIf { !it.isDefault } ?: project.adjustments
    val colorFilter = remember(clipAdjustments) {
        val matrix = SazfloyColorEngine.createCompositeMatrix(clipAdjustments)
        ColorFilter.colorMatrix(ColorMatrix(matrix))
    }

    // Active texts, stickers & shapes
    val activeTexts = project.textOverlays.filter {
        currentPlayheadMs in it.startMs..(it.startMs + it.durationMs)
    }
    val activeStickers = project.stickerOverlays.filter {
        currentPlayheadMs in it.startMs..(it.startMs + it.durationMs)
    }
    val activeShapes = project.shapeOverlays.filter {
        currentPlayheadMs in it.startMs..(it.startMs + it.durationMs)
    }
    val activeEffects = project.effects.filter {
        it.isEnabled && currentPlayheadMs in it.startMs..(it.startMs + it.durationMs) &&
        (it.targetClipId == null || it.targetClipId == activeClip?.id)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Video Aspect Box
        val clipScale = activeClip?.scale ?: 1.0f
        val clipPanX = activeClip?.panX ?: 0f
        val clipPanY = activeClip?.panY ?: 0f
        val clipRotation = activeClip?.rotationDegrees?.toFloat() ?: 0f

        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(ratio, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, SazfloyBorder, RoundedCornerShape(8.dp))
                .pointerInput(activeClip?.id) {
                    if (activeClip != null) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (activeClip.scale * zoom).coerceIn(0.2f, 4.0f)
                            val newPanX = (activeClip.panX + (pan.x / size.width.toFloat())).coerceIn(-1.5f, 1.5f)
                            val newPanY = (activeClip.panY + (pan.y / size.height.toFloat())).coerceIn(-1.5f, 1.5f)
                            onUpdateClip(activeClip.copy(scale = newScale, panX = newPanX, panY = newPanY))
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val isRealVideo = activeClip != null &&
                    activeClip.type == ClipType.VIDEO &&
                    activeClip.uri.isNotBlank() &&
                    (activeClip.uri.startsWith("file://") || activeClip.uri.startsWith("content://"))

            val isRealPhoto = activeClip != null &&
                    activeClip.type == ClipType.PHOTO &&
                    activeClip.uri.isNotBlank()

            // Transformed Media Layer (Scale / Zoom & Pan Position)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = clipScale
                        scaleY = clipScale
                        translationX = clipPanX * size.width
                        translationY = clipPanY * size.height
                        rotationZ = clipRotation
                    }
            ) {
                if (isRealVideo) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (isRealPhoto) {
                    AsyncImage(
                        model = activeClip.uri,
                        contentDescription = activeClip.name,
                        contentScale = ContentScale.Fit,
                        colorFilter = colorFilter,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    activeClip?.let { clip ->
                        val sampleFrame = remember(clip.id, clip.name) {
                            VideoExportEngine.createClipSampleFrame(clip, 360, 480, 0f)
                        }
                        Image(
                            bitmap = sampleFrame.asImageBitmap(),
                            contentDescription = null,
                            colorFilter = colorFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Floating Zoom Badge (when scaled or panned)
            if (activeClip != null && (clipScale != 1.0f || clipPanX != 0f || clipPanY != 0f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyCyan.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Zoom ${(clipScale * 100).toInt()}%",
                            color = SazfloyCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reset",
                            color = SazfloyAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                onUpdateClip(activeClip.copy(scale = 1.0f, panX = 0f, panY = 0f))
                            }
                        )
                    }
                }
            }

            // Render Transition Effect between clips
            if (activeClip != null && activeClip.transitionType != TransitionType.NONE) {
                val timeIntoClip = (currentPlayheadMs - activeClipTimelineStart).coerceAtLeast(0L)
                val transDur = activeClip.transitionDurationMs.coerceAtLeast(100L)
                if (timeIntoClip < transDur) {
                    val progress = 1f - (timeIntoClip.toFloat() / transDur) // 1.0 down to 0.0
                    when (activeClip.transitionType) {
                        TransitionType.DIP_TO_BLACK -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = progress)))
                        }
                        TransitionType.DIP_TO_WHITE, TransitionType.FLASH_WHITE -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = progress)))
                        }
                        TransitionType.CROSS_DISSOLVE, TransitionType.DISSOLVE, TransitionType.FADE -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = progress * 0.7f)))
                        }
                        TransitionType.GLITCH -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Cyan.copy(alpha = progress * 0.4f),
                                                Color.Magenta.copy(alpha = progress * 0.4f),
                                                Color.Black.copy(alpha = progress * 0.5f)
                                            )
                                        )
                                    )
                            )
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = progress * 0.5f)))
                        }
                    }
                }
            }

            // Render Active Video Effects Visual Overlays
            activeEffects.forEach { effect ->
                when (effect.type) {
                    EffectType.VHS_TAPE, EffectType.DIGITAL_NOISE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.08f * effect.intensity),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.18f * effect.intensity)
                                        )
                                    )
                                )
                        )
                    }
                    EffectType.GOLDEN_HOUR, EffectType.LIGHT_LEAK -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFFFFB300).copy(alpha = 0.28f * effect.intensity),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    EffectType.NEON_CYAN -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF00E5BC).copy(alpha = 0.16f * effect.intensity),
                                            Color.Transparent,
                                            Color(0xFF8A3FFC).copy(alpha = 0.16f * effect.intensity)
                                        )
                                    )
                                )
                        )
                    }
                    EffectType.VIGNETTE_FOCUS -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f * effect.intensity)
                                        )
                                    )
                                )
                        )
                    }
                    EffectType.SEPIA_VINTAGE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF704214).copy(alpha = 0.25f * effect.intensity))
                        )
                    }
                    else -> {}
                }
            }

            // Render Active Shapes
            activeShapes.forEach { shape ->
                val shapeColor = runCatching { Color(android.graphics.Color.parseColor(shape.fillColorHex)) }.getOrDefault(SazfloyToska)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(
                            width = (100 * shape.widthRatio * 2f).coerceIn(30f, 220f).dp,
                            height = (100 * shape.heightRatio * 2f).coerceIn(30f, 220f).dp
                        )
                        .clip(
                            when (shape.type) {
                                ShapeType.CIRCLE -> CircleShape
                                ShapeType.ROUNDED_RECT -> RoundedCornerShape(12.dp)
                                else -> RoundedCornerShape(2.dp)
                            }
                        )
                        .background(shapeColor.copy(alpha = shape.opacity.coerceIn(0.1f, 1f)))
                        .border(
                            width = shape.strokeWidth.dp.coerceAtLeast(1.dp),
                            color = runCatching { Color(android.graphics.Color.parseColor(shape.strokeColorHex)) }.getOrDefault(Color.White),
                            shape = when (shape.type) {
                                ShapeType.CIRCLE -> CircleShape
                                ShapeType.ROUNDED_RECT -> RoundedCornerShape(12.dp)
                                else -> RoundedCornerShape(2.dp)
                            }
                        )
                )
            }

            // Render Active Text Overlays with Direct Touch Drag & Selection
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                activeTexts.forEach { textItem ->
                    val isSel = textItem.id == selectedTextId
                    val pixelX = (textItem.posXRatio * boxWidthPx)
                    val pixelY = (textItem.posYRatio * boxHeightPx)

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (pixelX - 60f).toInt().coerceIn(0, (constraints.maxWidth - 120).coerceAtLeast(0)),
                                    (pixelY - 24f).toInt().coerceIn(0, (constraints.maxHeight - 50).coerceAtLeast(0))
                                )
                            }
                            .pointerInput(textItem.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newXRatio = ((textItem.posXRatio * boxWidthPx) + dragAmount.x) / boxWidthPx
                                    val newYRatio = ((textItem.posYRatio * boxHeightPx) + dragAmount.y) / boxHeightPx
                                    onUpdateText(
                                        textItem.copy(
                                            posXRatio = newXRatio.coerceIn(0.05f, 0.95f),
                                            posYRatio = newYRatio.coerceIn(0.05f, 0.95f)
                                        )
                                    )
                                }
                            }
                            .clickable {
                                onSelectText(textItem.id)
                            }
                            .then(
                                if (isSel) {
                                    Modifier
                                        .border(1.5.dp, SazfloyToska, RoundedCornerShape(8.dp))
                                        .background(SazfloyToska.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                } else Modifier
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = when (textItem.textAlign.lowercase()) {
                            "left" -> Alignment.Start
                            "right" -> Alignment.End
                            else -> Alignment.CenterHorizontally
                        }) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (textItem.hasBackground) Color(android.graphics.Color.parseColor(textItem.backgroundColorHex)) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = textItem.text,
                                    color = Color(android.graphics.Color.parseColor(textItem.fontColorHex)),
                                    fontFamily = SazfloyFonts.getFontFamily(textItem.fontFamilyName),
                                    fontWeight = if (textItem.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = (textItem.fontSizeSp * 0.9f).sp
                                )
                            }
                        }

                        // Quick Delete Button (✕) when selected
                        if (isSel) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-8).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(SazfloyPink)
                                    .clickable { onDeleteText(textItem.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Teks",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Render Active Stickers
            activeStickers.forEach { sticker ->
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = sticker.emojiOrTag, fontSize = 42.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Transport Controls Row (Jump to Start, Play/Pause, Mute, Timecode, Fullscreen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Jump to Start (⏮️)
                IconButton(
                    onClick = onSeekToStart,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SazfloySurfaceVariant)
                        .testTag("preview_seek_to_start")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Kembali ke Awal",
                        tint = SazfloyCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SazfloyCyan)
                        .testTag("preview_play_toggle")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = SazfloyBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Timecode
                val currentSec = "%.1f".format(currentPlayheadMs / 1000f)
                val totalSec = "%.1f".format(project.totalDurationMs / 1000f)
                Text(
                    text = "00:${currentSec.padStart(4, '0')} / 00:${totalSec.padStart(4, '0')}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyTextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onToggleMute, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = if (isMuted) SazfloyPink else SazfloyTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onToggleFullscreen, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = SazfloyTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoToolTabsRow(
    activeTab: VideoEditorToolTab,
    onSelectTab: (VideoEditorToolTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(SazfloySurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(VideoEditorToolTab.values()) { tab ->
            val isSelected = activeTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) SazfloyCyan else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) SazfloyBackground else SazfloyTextSecondary
                )
            }
        }
    }
}

@Composable
private fun VideoToolPanelContent(
    activeTab: VideoEditorToolTab,
    project: EditingProject,
    selectedClip: MediaClip?,
    selectedTextId: String? = null,
    selectedShapeId: String? = null,
    selectedEffectId: String? = null,
    currentPlayheadMs: Long = 0L,
    onUpdateClip: (MediaClip) -> Unit,
    onUpdateProject: (EditingProject) -> Unit,
    onUpdateAdjustments: (ColorAdjustments) -> Unit,
    onAddTextClick: () -> Unit,
    onUpdateText: (TextOverlay) -> Unit = {},
    onAddShape: (ShapeOverlay) -> Unit = {},
    onRemoveShape: (String) -> Unit = {},
    onUpdateShape: (ShapeOverlay) -> Unit = {},
    onAddEffect: (VideoEffect) -> Unit = {},
    onSelectEffect: (String) -> Unit = {},
    onUpdateEffect: (VideoEffect) -> Unit = {},
    onRemoveEffect: (String) -> Unit = {},
    onAddSticker: (String) -> Unit,
    onImportLutClick: () -> Unit = {},
    onApplyTitleTemplate: (TitleTemplate) -> Unit = {},
    onGenerateAutoCaptions: (CaptionStylePreset) -> Unit = {},
    onImportAudioClick: () -> Unit = {},
    onMoveClipLeft: (String) -> Unit = {},
    onMoveClipRight: (String) -> Unit = {},
    onMoveClipToFront: (String) -> Unit = {},
    onMoveClipToEnd: (String) -> Unit = {},
    onToggleBeatMarker: (Long) -> Unit = {},
    onAutoBeats: () -> Unit = {},
    onSeek: (Long) -> Unit = {}
) {
    when (activeTab) {
        VideoEditorToolTab.EFFECTS -> {
            var selectedCategory by remember { mutableStateOf(EffectCategory.BASIC) }
            val activeEffect = project.effects.firstOrNull { it.id == selectedEffectId }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Filter Chips
                Text("Kategori Efek Video (20 Efek Nyata)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SazfloyVioletLight)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(EffectCategory.values()) { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) SazfloyViolet else SazfloySurfaceVariant)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                color = if (isSel) Color.White else SazfloyTextSecondary,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Effects in selected category
                val effectsInCat = EffectType.values().filter { it.category == selectedCategory }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(effectsInCat) { effType ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SazfloySurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(165.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = SazfloyViolet, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = effType.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SazfloyVioletLight,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = effType.description,
                                    fontSize = 9.sp,
                                    color = SazfloyTextMuted,
                                    maxLines = 2,
                                    lineHeight = 12.sp
                                )
                                Button(
                                    onClick = {
                                        val newEffect = VideoEffect(
                                            type = effType,
                                            name = effType.displayName,
                                            startMs = currentPlayheadMs,
                                            durationMs = 3000L,
                                            intensity = 0.8f
                                        )
                                        onAddEffect(newEffect)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyViolet, contentColor = Color.White),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 2.dp)
                                ) {
                                    Text("+ Pasang Efek", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // If an effect is selected on timeline, show controls for it
                if (activeEffect != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SazfloyDivider))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Atur Efek: ${activeEffect.name}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SazfloyVioletLight
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Toggle enable
                            Button(
                                onClick = { onUpdateEffect(activeEffect.copy(isEnabled = !activeEffect.isEnabled)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeEffect.isEnabled) SazfloyToska else SazfloySurfaceVariant
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = if (activeEffect.isEnabled) "Aktif" else "Mati",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeEffect.isEnabled) SazfloyBackground else SazfloyTextMuted
                                )
                            }
                            // Delete
                            Button(
                                onClick = { onRemoveEffect(activeEffect.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = SazfloyPink.copy(alpha = 0.2f)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Hapus", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SazfloyPink)
                            }
                        }
                    }

                    // Intensity slider
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Intensitas: ${(activeEffect.intensity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary, modifier = Modifier.width(90.dp))
                        Slider(
                            value = activeEffect.intensity,
                            onValueChange = { onUpdateEffect(activeEffect.copy(intensity = it)) },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyViolet, activeTrackColor = SazfloyViolet)
                        )
                    }

                    // Duration slider
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Durasi: ${"%.1f".format(activeEffect.durationMs / 1000f)}s", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary, modifier = Modifier.width(90.dp))
                        Slider(
                            value = activeEffect.durationMs.toFloat(),
                            onValueChange = { onUpdateEffect(activeEffect.copy(durationMs = it.toLong())) },
                            valueRange = 500f..10000f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyVioletLight, activeTrackColor = SazfloyVioletLight)
                        )
                    }
                }
            }
        }
        VideoEditorToolTab.COLOR_WHEELS -> {
            val clipAdj = selectedClip?.adjustments?.takeIf { !it.isDefault } ?: project.adjustments
            Column(modifier = Modifier.fillMaxSize()) {
                if (selectedClip != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grading: ${selectedClip.name}", color = SazfloyCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                val currentWheels = clipAdj.colorWheels
                                onUpdateProject(project.copy(
                                    adjustments = project.adjustments.copy(colorWheels = currentWheels),
                                    clips = project.clips.map { it.copy(adjustments = it.adjustments.copy(colorWheels = currentWheels)) }
                                ))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Terapkan ke Semua Klip", fontSize = 9.sp, color = SazfloyToska)
                        }
                    }
                }
                DaVinciColorWheelsComponent(
                    colorWheels = clipAdj.colorWheels,
                    onUpdateWheels = { newWheels ->
                        val updated = clipAdj.copy(colorWheels = newWheels)
                        if (selectedClip != null) {
                            onUpdateClip(selectedClip.copy(adjustments = updated))
                        } else {
                            onUpdateAdjustments(updated)
                        }
                    }
                )
            }
        }
        VideoEditorToolTab.LUTS -> {
            val clipAdj = selectedClip?.adjustments?.takeIf { !it.isDefault } ?: project.adjustments
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3D LUT Grading Engine",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SazfloyCyan
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (selectedClip != null) {
                            Button(
                                onClick = {
                                    val lutId = clipAdj.activeLutId
                                    val lutInt = clipAdj.lutIntensity
                                    onUpdateProject(project.copy(
                                        adjustments = project.adjustments.copy(activeLutId = lutId, lutIntensity = lutInt),
                                        clips = project.clips.map { it.copy(adjustments = it.adjustments.copy(activeLutId = lutId, lutIntensity = lutInt)) }
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Semua Klip", fontSize = 9.sp, color = SazfloyToska)
                            }
                        }
                        Button(
                            onClick = onImportLutClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloyViolet, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Import .cube", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (clipAdj.activeLutId != "none") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intensitas LUT: ${(clipAdj.lutIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = SazfloyTextSecondary
                        )
                        Slider(
                            value = clipAdj.lutIntensity,
                            onValueChange = {
                                val updated = clipAdj.copy(lutIntensity = it)
                                if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                            },
                            valueRange = 0f..1f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        val isNone = clipAdj.activeLutId == "none"
                        Box(
                            modifier = Modifier
                                .width(95.dp)
                                .height(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNone) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable {
                                    val updated = clipAdj.copy(activeLutId = "none")
                                    if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                                }
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
                        val isSel = clipAdj.activeLutId == lut.id
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .height(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SazfloySurfaceElevated else SazfloySurfaceVariant)
                                .border(if (isSel) 2.dp else 1.dp, if (isSel) SazfloyCyan else SazfloyBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val updated = clipAdj.copy(activeLutId = lut.id)
                                    if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                                }
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
        VideoEditorToolTab.TITLES -> {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Template Judul & Lower-Third Lengkap",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyCyan
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TitleTemplates.CATALOG) { tpl ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SazfloySurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(170.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(tpl.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SazfloyCyan)
                                Text(
                                    text = tpl.primaryText,
                                    fontFamily = SazfloyFonts.getFontFamily(tpl.fontFamilyName),
                                    fontSize = 12.sp,
                                    color = Color(android.graphics.Color.parseColor(tpl.fontColorHex)),
                                    maxLines = 1
                                )
                                Text("Font: ${tpl.fontFamilyName.replaceFirstChar { it.uppercase() }} • ${tpl.category}", fontSize = 9.sp, color = SazfloyTextMuted)
                                Button(
                                    onClick = { onApplyTitleTemplate(tpl) },
                                    modifier = Modifier.fillMaxWidth().height(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyToska, contentColor = SazfloyBackground),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 2.dp)
                                ) {
                                    Text("Pasang Judul", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        VideoEditorToolTab.AUTO_CAPTIONS -> {
            var selectedCaptionStyle by remember { mutableStateOf(CaptionStylePreset.TIKTOK_VIRAL) }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Auto Text dari Audio (Speech-to-Text Subtitle)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyCyan
                )
                Text("Pilih Gaya Teks Subtitle:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CaptionStylePreset.values()) { capStyle ->
                        val isSel = selectedCaptionStyle == capStyle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable { selectedCaptionStyle = capStyle }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = capStyle.title,
                                color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Button(
                    onClick = { onGenerateAutoCaptions(selectedCaptionStyle) },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("✨ Generate Auto Text dari Audio Timeline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        VideoEditorToolTab.CLIP_TRIM -> {
            if (selectedClip != null) {
                val clipIdx = project.clips.indexOfFirst { it.id == selectedClip.id }
                val totalClips = project.clips.size

                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Klip: ${selectedClip.name} • Trim: ${"%.1f".format(selectedClip.trimmedDurationMs / 1000f)}s (Asli: ${"%.1f".format(selectedClip.durationMs / 1000f)}s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SazfloyCyan
                    )

                    // 1. Geser Urutan Klip (Depan / Belakang)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SazfloySurfaceVariant.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Geser Urutan Klip",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SazfloyCyan
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SazfloyCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Posisi ${clipIdx + 1} dari $totalClips",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SazfloyCyan
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onMoveClipToFront(selectedClip.id) },
                                enabled = clipIdx > 0,
                                modifier = Modifier.weight(1f).height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SazfloySurfaceVariant,
                                    contentColor = SazfloyTextPrimary,
                                    disabledContainerColor = SazfloySurfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = SazfloyTextMuted
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("⏮ Awal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onMoveClipLeft(selectedClip.id) },
                                enabled = clipIdx > 0,
                                modifier = Modifier.weight(1f).height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SazfloySurfaceVariant,
                                    contentColor = SazfloyCyan,
                                    disabledContainerColor = SazfloySurfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = SazfloyTextMuted
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("◀ Depan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onMoveClipRight(selectedClip.id) },
                                enabled = clipIdx < totalClips - 1,
                                modifier = Modifier.weight(1f).height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SazfloySurfaceVariant,
                                    contentColor = SazfloyCyan,
                                    disabledContainerColor = SazfloySurfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = SazfloyTextMuted
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Belakang ▶", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onMoveClipToEnd(selectedClip.id) },
                                enabled = clipIdx < totalClips - 1,
                                modifier = Modifier.weight(1f).height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SazfloySurfaceVariant,
                                    contentColor = SazfloyTextPrimary,
                                    disabledContainerColor = SazfloySurfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = SazfloyTextMuted
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Akhir ⏭", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 2. Ukuran & Zoom Video (Scale & Posisi)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SazfloySurfaceVariant.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ukuran & Zoom Video (${(selectedClip.scale * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SazfloyToska
                            )
                            if (selectedClip.scale != 1.0f || selectedClip.panX != 0f || selectedClip.panY != 0f) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SazfloySurfaceElevated)
                                        .clickable { onUpdateClip(selectedClip.copy(scale = 1.0f, panX = 0f, panY = 0f)) }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Reset 100%", fontSize = 9.sp, color = SazfloyCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Zoom", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary, modifier = Modifier.width(42.dp))
                            Slider(
                                value = selectedClip.scale,
                                onValueChange = { onUpdateClip(selectedClip.copy(scale = ((it * 100).toInt() / 100f).coerceIn(0.2f, 4.0f))) },
                                valueRange = 0.2f..3.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = SazfloyToska, activeTrackColor = SazfloyToska)
                            )
                        }

                        // Preset zoom chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val zoomPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                            items(zoomPresets) { zVal ->
                                val isSel = kotlin.math.abs(selectedClip.scale - zVal) < 0.05f
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) SazfloyToska else SazfloySurfaceElevated)
                                        .clickable { onUpdateClip(selectedClip.copy(scale = zVal)) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${(zVal * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) SazfloyBackground else SazfloyTextPrimary
                                    )
                                }
                            }
                        }

                        // Pan position sliders (Horizontal & Vertical)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Posisi X: ${"%.2f".format(selectedClip.panX)}", fontSize = 9.sp, color = SazfloyTextMuted)
                                Slider(
                                    value = selectedClip.panX,
                                    onValueChange = { onUpdateClip(selectedClip.copy(panX = it)) },
                                    valueRange = -0.5f..0.5f,
                                    colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Posisi Y: ${"%.2f".format(selectedClip.panY)}", fontSize = 9.sp, color = SazfloyTextMuted)
                                Slider(
                                    value = selectedClip.panY,
                                    onValueChange = { onUpdateClip(selectedClip.copy(panY = it)) },
                                    valueRange = -0.5f..0.5f,
                                    colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                                )
                            }
                        }
                    }

                    // 3. Trimming Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Trim Start: ${"%.2f".format(selectedClip.trimStartMs / 1000f)}s", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                            Text("Trim End: ${"%.2f".format(selectedClip.trimEndMs / 1000f)}s", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Trim Start Slider
                            Slider(
                                value = selectedClip.trimStartMs.toFloat(),
                                onValueChange = { newStart ->
                                    val safeStart = newStart.toLong().coerceIn(0L, (selectedClip.trimEndMs - 500L).coerceAtLeast(0L))
                                    onUpdateClip(selectedClip.copy(trimStartMs = safeStart))
                                },
                                valueRange = 0f..(selectedClip.durationMs - 500L).coerceAtLeast(0L).toFloat(),
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                            )
                            // Trim End Slider
                            Slider(
                                value = selectedClip.trimEndMs.toFloat(),
                                onValueChange = { newEnd ->
                                    val safeEnd = newEnd.toLong().coerceIn(selectedClip.trimStartMs + 500L, selectedClip.durationMs)
                                    onUpdateClip(selectedClip.copy(trimEndMs = safeEnd))
                                },
                                valueRange = 500f..selectedClip.durationMs.coerceAtLeast(500L).toFloat(),
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = SazfloyToska, activeTrackColor = SazfloyToska)
                            )
                        }
                    }

                    // Quick Trim Buttons
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val offset = currentPlayheadMs.coerceIn(0L, selectedClip.durationMs)
                                val safeStart = offset.coerceIn(0L, (selectedClip.trimEndMs - 500L).coerceAtLeast(0L))
                                onUpdateClip(selectedClip.copy(trimStartMs = safeStart))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text("✂ Potong Kiri", fontSize = 10.sp, color = SazfloyTextPrimary)
                        }

                        Button(
                            onClick = {
                                val offset = currentPlayheadMs.coerceIn(0L, selectedClip.durationMs)
                                val safeEnd = offset.coerceIn(selectedClip.trimStartMs + 500L, selectedClip.durationMs)
                                onUpdateClip(selectedClip.copy(trimEndMs = safeEnd))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text("✂ Potong Kanan", fontSize = 10.sp, color = SazfloyTextPrimary)
                        }

                        Button(
                            onClick = {
                                onUpdateClip(selectedClip.copy(trimStartMs = 0L, trimEndMs = selectedClip.durationMs))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text("Reset Trim", fontSize = 10.sp, color = SazfloyCyan)
                        }
                    }

                    // 4. Rotate & Flip Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onUpdateClip(selectedClip.copy(rotationDegrees = (selectedClip.rotationDegrees + 90) % 360))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rotate", color = SazfloyTextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                onUpdateClip(selectedClip.copy(isFlippedHorizontal = !selectedClip.isFlippedHorizontal))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedClip.isFlippedHorizontal) SazfloyCyan else SazfloySurfaceVariant
                            )
                        ) {
                            Text("Flip H", color = if (selectedClip.isFlippedHorizontal) SazfloyBackground else SazfloyTextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                onUpdateClip(selectedClip.copy(isFlippedVertical = !selectedClip.isFlippedVertical))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedClip.isFlippedVertical) SazfloyCyan else SazfloySurfaceVariant
                            )
                        ) {
                            Text("Flip V", color = if (selectedClip.isFlippedVertical) SazfloyBackground else SazfloyTextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pilih klip di timeline untuk memotong, mengubah ukuran, atau menggeser posisi", color = SazfloyTextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        VideoEditorToolTab.SPEED_VOL -> {
            if (selectedClip != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Kecepatan Klip (${selectedClip.speed}x)", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f).forEach { spd ->
                            val isSel = selectedClip.speed == spd
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                    .clickable { onUpdateClip(selectedClip.copy(speed = spd)) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${spd}x",
                                    color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Volume Audio Klip (${(selectedClip.volume * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                    Slider(
                        value = selectedClip.volume,
                        onValueChange = { onUpdateClip(selectedClip.copy(volume = it)) },
                        valueRange = 0f..2f,
                        colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                    )
                }
            }
        }
        VideoEditorToolTab.FILTERS -> {
            val clipAdj = selectedClip?.adjustments?.takeIf { !it.isDefault } ?: project.adjustments
            var selectedFilterCategory by remember { mutableStateOf("Semua") }
            val mediaModel = selectedClip?.thumbnailUri ?: selectedClip?.uri ?: com.example.R.drawable.sazfloy_logo_1793348390433

            val allCategories = listOf("Semua", "Daily", "Cinema", "Analog", "Tone", "Beauty")
            val filteredPresets = if (selectedFilterCategory == "Semua") {
                SazfloyColorEngine.PRESETS
            } else {
                SazfloyColorEngine.PRESETS.filter { it.category.equals(selectedFilterCategory, ignoreCase = true) || it.id == "none" }
            }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Action Bar: Active filter info & Apply to all clips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activePreset = SazfloyColorEngine.PRESETS.firstOrNull { it.id == clipAdj.filterPresetId }
                    Text(
                        text = "Filter: ${activePreset?.name ?: "Original"}",
                        color = SazfloyCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedClip != null) {
                        Button(
                            onClick = {
                                val currentPreset = clipAdj.filterPresetId
                                onUpdateProject(project.copy(
                                    adjustments = project.adjustments.copy(filterPresetId = currentPreset),
                                    clips = project.clips.map { it.copy(adjustments = it.adjustments.copy(filterPresetId = currentPreset)) }
                                ))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Terapkan ke Semua Klip", fontSize = 9.sp, color = SazfloyToska, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Category Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(allCategories) { cat ->
                        val isSel = selectedFilterCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable { selectedFilterCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) SazfloyBackground else SazfloyTextPrimary
                            )
                        }
                    }
                }

                // CapCut-Style Filter Cards with live Coil AsyncImage
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredPresets) { preset ->
                        val isSel = clipAdj.filterPresetId == preset.id
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) SazfloySurfaceElevated else SazfloySurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSel) 2.dp else 1.dp,
                                color = if (isSel) SazfloyCyan else SazfloyBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(78.dp)
                                .height(112.dp)
                                .clickable {
                                    val updated = clipAdj.copy(filterPresetId = preset.id)
                                    if (selectedClip != null) {
                                        onUpdateClip(selectedClip.copy(adjustments = updated))
                                    } else {
                                        onUpdateAdjustments(updated)
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                ) {
                                    // Coil live filter thumbnail image
                                    AsyncImage(
                                        model = mediaModel,
                                        contentDescription = preset.name,
                                        contentScale = ContentScale.Crop,
                                        colorFilter = ColorFilter.colorMatrix(ColorMatrix(preset.colorMatrix)),
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // CapCut-style selected checkmark badge
                                    if (isSel) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(3.dp)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(SazfloyCyan),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✓",
                                                color = SazfloyBackground,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Preset Name
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSel) SazfloyCyan.copy(alpha = 0.2f) else Color.Transparent)
                                        .padding(horizontal = 4.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset.name.replace("SAZFLOY ", ""),
                                        fontSize = 9.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) SazfloyCyan else SazfloyTextPrimary,
                                        maxLines = 1,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter Intensity Slider when a filter is active
                if (clipAdj.filterPresetId != "none") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SazfloySurfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intensitas Filter: ${(clipAdj.contrast + 100).toInt() / 2}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = SazfloyCyan,
                            modifier = Modifier.width(130.dp)
                        )
                        Slider(
                            value = clipAdj.contrast,
                            onValueChange = { newContrast ->
                                val updated = clipAdj.copy(contrast = newContrast)
                                if (selectedClip != null) {
                                    onUpdateClip(selectedClip.copy(adjustments = updated))
                                } else {
                                    onUpdateAdjustments(updated)
                                }
                            },
                            valueRange = -50f..50f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                    }
                }
            }
        }
        VideoEditorToolTab.ADJUST -> {
            val clipAdj = selectedClip?.adjustments?.takeIf { !it.isDefault } ?: project.adjustments
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (selectedClip != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Penyesuaian: ${selectedClip.name}", color = SazfloyCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                onUpdateProject(project.copy(
                                    adjustments = clipAdj,
                                    clips = project.clips.map { it.copy(adjustments = clipAdj) }
                                ))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Terapkan ke Semua Klip", fontSize = 9.sp, color = SazfloyToska)
                        }
                    }
                }
                AdjustmentRow("Brightness", clipAdj.brightness, -100f..100f) {
                    val updated = clipAdj.copy(brightness = it)
                    if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                }
                AdjustmentRow("Contrast", clipAdj.contrast, -100f..100f) {
                    val updated = clipAdj.copy(contrast = it)
                    if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                }
                AdjustmentRow("Saturation", clipAdj.saturation, -100f..100f) {
                    val updated = clipAdj.copy(saturation = it)
                    if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                }
                AdjustmentRow("Blur Efek", clipAdj.blurRadius, 0f..25f) {
                    val updated = clipAdj.copy(blurRadius = it)
                    if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                }
                AdjustmentRow("Warmth", clipAdj.temperature, -100f..100f) {
                    val updated = clipAdj.copy(temperature = it)
                    if (selectedClip != null) onUpdateClip(selectedClip.copy(adjustments = updated)) else onUpdateAdjustments(updated)
                }
            }
        }
        VideoEditorToolTab.TTS_AUDIO -> {
            var ttsText by remember { mutableStateOf("") }
            var selectedVoiceIndex by remember { mutableIntStateOf(0) }
            var pitch by remember { mutableFloatStateOf(1.0f) }
            var speechRate by remember { mutableFloatStateOf(1.0f) }
            var isSynthesizing by remember { mutableStateOf(false) }
            val ttsScope = rememberCoroutineScope()
            val panelContext = LocalContext.current

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Text-to-Speech (AI Voiceover & Dubbing)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyCyan
                )

                OutlinedTextField(
                    value = ttsText,
                    onValueChange = { ttsText = it },
                    placeholder = { Text("Ketik kalimat narasi atau voiceover di sini...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text("Pilih Bahasa & Suara:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TextToSpeechEngine.AVAILABLE_VOICES.indices.toList()) { idx ->
                        val voice = TextToSpeechEngine.AVAILABLE_VOICES[idx]
                        val isSel = selectedVoiceIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable { selectedVoiceIndex = idx }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = voice.displayName,
                                color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pitch (${"%.1f".format(pitch)}x)", style = MaterialTheme.typography.labelSmall, color = SazfloyTextMuted)
                        Slider(
                            value = pitch,
                            onValueChange = { pitch = it },
                            valueRange = 0.5f..1.8f,
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kecepatan (${"%.1f".format(speechRate)}x)", style = MaterialTheme.typography.labelSmall, color = SazfloyTextMuted)
                        Slider(
                            value = speechRate,
                            onValueChange = { speechRate = it },
                            valueRange = 0.6f..1.8f,
                            colors = SliderDefaults.colors(thumbColor = SazfloyToska, activeTrackColor = SazfloyToska)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (ttsText.isNotBlank() && !isSynthesizing) {
                            ttsScope.launch {
                                isSynthesizing = true
                                val voice = TextToSpeechEngine.AVAILABLE_VOICES[selectedVoiceIndex]
                                val result = TextToSpeechEngine.synthesizeToAudioTrack(
                                    context = panelContext,
                                    text = ttsText,
                                    voiceOption = voice,
                                    pitch = pitch,
                                    speechRate = speechRate,
                                    startMs = currentPlayheadMs
                                )
                                isSynthesizing = false
                                result.onSuccess { audioTrack ->
                                    onUpdateProject(project.copy(audioTracks = project.audioTracks + audioTrack))
                                    Toast.makeText(panelContext, "Voiceover AI berhasil dipasang di timeline!", Toast.LENGTH_SHORT).show()
                                    ttsText = ""
                                }.onFailure { err ->
                                    Toast.makeText(panelContext, "Gagal membuat audio TTS: ${err.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = ttsText.isNotBlank() && !isSynthesizing,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSynthesizing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SazfloyBackground, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sedang Sintesis Suara AI...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🔊 Buat Voiceover AI & Pasang di Timeline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
        VideoEditorToolTab.TEXT -> {
            val activeText = project.textOverlays.firstOrNull { it.id == selectedTextId }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddTextClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambahkan Teks Baru di Playhead")
                }

                if (activeText != null) {
                    Text("Edit Isi Teks:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SazfloyAmber)

                    OutlinedTextField(
                        value = activeText.text,
                        onValueChange = { onUpdateText(activeText.copy(text = it)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pilihan Font Typography:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SazfloyFonts.AVAILABLE_FONTS) { fontItem ->
                            val isSel = activeText.fontFamilyName.equals(fontItem.id, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SazfloyAmber else SazfloySurfaceVariant)
                                    .clickable { onUpdateText(activeText.copy(fontFamilyName = fontItem.id)) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fontItem.displayName,
                                    fontFamily = fontItem.family,
                                    color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ukuran: ${activeText.fontSizeSp.toInt()}sp", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                        Slider(
                            value = activeText.fontSizeSp,
                            onValueChange = { onUpdateText(activeText.copy(fontSizeSp = it)) },
                            valueRange = 12f..72f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(thumbColor = SazfloyAmber, activeTrackColor = SazfloyAmber)
                        )
                    }

                    // Alignment and Style Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onUpdateText(activeText.copy(textAlign = "left")) },
                            modifier = Modifier.size(34.dp).background(if (activeText.textAlign == "left") SazfloyCyan else SazfloySurfaceVariant, RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.FormatAlignLeft, contentDescription = "Rata Kiri", tint = if (activeText.textAlign == "left") SazfloyBackground else SazfloyTextPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { onUpdateText(activeText.copy(textAlign = "center")) },
                            modifier = Modifier.size(34.dp).background(if (activeText.textAlign == "center") SazfloyCyan else SazfloySurfaceVariant, RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.FormatAlignCenter, contentDescription = "Rata Tengah", tint = if (activeText.textAlign == "center") SazfloyBackground else SazfloyTextPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { onUpdateText(activeText.copy(textAlign = "right")) },
                            modifier = Modifier.size(34.dp).background(if (activeText.textAlign == "right") SazfloyCyan else SazfloySurfaceVariant, RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.FormatAlignRight, contentDescription = "Rata Kanan", tint = if (activeText.textAlign == "right") SazfloyBackground else SazfloyTextPrimary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { onUpdateText(activeText.copy(isBold = !activeText.isBold)) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (activeText.isBold) SazfloyCyan else SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("B", fontWeight = FontWeight.Bold, color = if (activeText.isBold) SazfloyBackground else SazfloyTextPrimary)
                        }

                        Button(
                            onClick = { onUpdateText(activeText.copy(hasBackground = !activeText.hasBackground, backgroundColorHex = "#B3000000")) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (activeText.hasBackground) SazfloyCyan else SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Box", fontSize = 10.sp, color = if (activeText.hasBackground) SazfloyBackground else SazfloyTextPrimary)
                        }

                        Button(
                            onClick = { onUpdateText(activeText.copy(hasOutline = !activeText.hasOutline)) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (activeText.hasOutline) SazfloyToska else SazfloySurfaceVariant),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Outline", fontSize = 10.sp, color = if (activeText.hasOutline) SazfloyBackground else SazfloyTextPrimary)
                        }
                    }

                    // Text Color Row
                    Text("Pilih Warna Teks:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("#FFFFFF", "#FFFF00", "#00E5BC", "#00E5FF", "#FF2D75", "#FFB300", "#8A3FFC", "#000000").forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(if (activeText.fontColorHex.equals(hex, ignoreCase = true)) 2.dp else 1.dp, Color.White, CircleShape)
                                    .clickable { onUpdateText(activeText.copy(fontColorHex = hex)) }
                            )
                        }
                    }

                    // Rotation Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rotasi: ${activeText.rotation.toInt()}°", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                        Slider(
                            value = activeText.rotation,
                            onValueChange = { onUpdateText(activeText.copy(rotation = it)) },
                            valueRange = -180f..180f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                    }
                } else {
                    Text(
                        text = "Ketuk teks pada pratinjau video di atas untuk menggeser, memperbesar, atau mengubah font dan warna.",
                        color = SazfloyTextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        VideoEditorToolTab.SHAPES -> {
            var selectedType by remember { mutableStateOf(ShapeType.RECTANGLE) }
            var shapeColorHex by remember { mutableStateOf("#00E5BC") }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Pilih Bentuk Geometri", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ShapeType.values()) { st ->
                        val isSel = selectedType == st
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable { selectedType = st }
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            onAddShape(
                                ShapeOverlay(
                                    type = selectedType,
                                    fillColorHex = shapeColorHex,
                                    startMs = currentPlayheadMs,
                                    durationMs = 4000L
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloyCyan, contentColor = SazfloyBackground)
                    ) {
                        Text("Tambah")
                    }
                }

                if (project.shapeOverlays.isNotEmpty()) {
                    Text("Bentuk di Timeline (${project.shapeOverlays.size})", style = MaterialTheme.typography.labelSmall, color = SazfloyTextMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(project.shapeOverlays) { shape ->
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
        VideoEditorToolTab.KEYFRAMES -> {
            val targetShape = project.shapeOverlays.firstOrNull { it.id == selectedShapeId }
            val targetText = project.textOverlays.firstOrNull { it.id == selectedTextId }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Kurva & Interpolasi Animasi Keyframe",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyCyan
                )

                Text("Pilih Kurva Transisi:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AnimationCurve.values()) { curve ->
                        val currentCurve = targetShape?.keyframes?.lastOrNull()?.curve ?: targetText?.keyframes?.lastOrNull()?.curve ?: AnimationCurve.EASE_IN_OUT
                        val isSel = currentCurve == curve
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                .clickable {
                                    if (targetShape != null && targetShape.keyframes.isNotEmpty()) {
                                        val updatedKfs = targetShape.keyframes.map { it.copy(curve = curve) }
                                        onUpdateShape(targetShape.copy(keyframes = updatedKfs))
                                    } else if (targetText != null && targetText.keyframes.isNotEmpty()) {
                                        val updatedKfs = targetText.keyframes.map { it.copy(curve = curve) }
                                        onUpdateText(targetText.copy(keyframes = updatedKfs))
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = curve.label,
                                color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                if (targetShape != null) {
                    Text("Posisi Shape: (${"%.2f".format(targetShape.posXRatio)}, ${"%.2f".format(targetShape.posYRatio)})", style = MaterialTheme.typography.labelSmall, color = SazfloyTextMuted)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = targetShape.posXRatio,
                            onValueChange = { onUpdateShape(targetShape.copy(posXRatio = it)) },
                            valueRange = 0.1f..0.9f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                        Slider(
                            value = targetShape.posYRatio,
                            onValueChange = { onUpdateShape(targetShape.copy(posYRatio = it)) },
                            valueRange = 0.1f..0.9f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                    }
                } else if (targetText != null) {
                    Text("Posisi Teks: (${"%.2f".format(targetText.posXRatio)}, ${"%.2f".format(targetText.posYRatio)})", style = MaterialTheme.typography.labelSmall, color = SazfloyTextMuted)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = targetText.posXRatio,
                            onValueChange = { onUpdateText(targetText.copy(posXRatio = it)) },
                            valueRange = 0.1f..0.9f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                        Slider(
                            value = targetText.posYRatio,
                            onValueChange = { onUpdateText(targetText.copy(posYRatio = it)) },
                            valueRange = 0.1f..0.9f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
                        )
                    }
                } else {
                    Text(
                        "Pilih Teks, Bentuk, atau Klip pada timeline lalu ketuk tombol '◆ Keyframe' di atas timeline.",
                        color = SazfloyTextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        VideoEditorToolTab.INSPECTOR -> {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Panel Properti Sazfloy",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SazfloyCyan
                )

                if (selectedClip != null) {
                    Text("Properti Klip: ${selectedClip.name}", color = SazfloyTextPrimary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Text("Durasi: ${"%.1f".format(selectedClip.trimmedDurationMs / 1000f)}s • Rotasi: ${selectedClip.rotationDegrees}° • Kecepatan: ${selectedClip.speed}x", color = SazfloyTextMuted, style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onUpdateClip(selectedClip.copy(rotationDegrees = (selectedClip.rotationDegrees + 90) % 360)) },
                            colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant)
                        ) {
                            Text("Rotasi 90°", color = SazfloyTextPrimary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onUpdateClip(selectedClip.copy(isFlippedHorizontal = !selectedClip.isFlippedHorizontal)) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedClip.isFlippedHorizontal) SazfloyCyan else SazfloySurfaceVariant)
                        ) {
                            Text("Flip Horizontal", color = if (selectedClip.isFlippedHorizontal) SazfloyBackground else SazfloyTextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                val activeTextObj = project.textOverlays.firstOrNull { it.id == selectedTextId }
                if (activeTextObj != null) {
                    Text("Properti Teks: \"${activeTextObj.text}\"", color = SazfloyAmber, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Ukuran Font: ${activeTextObj.fontSizeSp.toInt()}sp", color = SazfloyTextSecondary, style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = activeTextObj.fontSizeSp,
                            onValueChange = { onUpdateText(activeTextObj.copy(fontSizeSp = it)) },
                            valueRange = 14f..64f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(thumbColor = SazfloyAmber, activeTrackColor = SazfloyAmber)
                        )
                    }
                }

                val activeShapeObj = project.shapeOverlays.firstOrNull { it.id == selectedShapeId }
                if (activeShapeObj != null) {
                    Text("Properti Bentuk: ${activeShapeObj.type.name}", color = SazfloyToska, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Opasitas: ${(activeShapeObj.opacity * 100).toInt()}%", color = SazfloyTextSecondary, style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = activeShapeObj.opacity,
                            onValueChange = { onUpdateShape(activeShapeObj.copy(opacity = it)) },
                            valueRange = 0.1f..1.0f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(thumbColor = SazfloyToska, activeTrackColor = SazfloyToska)
                        )
                    }
                }

                if (selectedClip == null && activeTextObj == null && activeShapeObj == null) {
                    Text(
                        "Pilih salah satu item di timeline untuk melihat dan mengubah parameter di Panel Properti.",
                        color = SazfloyTextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        VideoEditorToolTab.STICKERS -> {
            val stickers = listOf("✨", "🔥", "❤️", "⚡", "🎬", "📸", "🌟", "🌸", "👑", "🌈", "💯", "🎉")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(stickers) { emoji ->
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
        VideoEditorToolTab.AUDIO -> {
            val beats = project.beatMarkersMs.sorted()
            val isCurrentAtBeat = beats.any { kotlin.math.abs(it - currentPlayheadMs) <= 150L }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. CapCut-Style Irama / Beat Markers Control Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SazfloySurfaceVariant.copy(alpha = 0.6f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SazfloyAmber)
                            )
                            Text(
                                text = "Irama Musik & Penanda Ketukan",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SazfloyAmber
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SazfloyAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${beats.size} Ketukan",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SazfloyAmber
                            )
                        }
                    }

                    // Action buttons for Beat Markers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onToggleBeatMarker(currentPlayheadMs) },
                            modifier = Modifier.weight(1.3f).height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentAtBeat) SazfloyPink.copy(alpha = 0.3f) else SazfloyAmber,
                                contentColor = if (isCurrentAtBeat) SazfloyPink else SazfloyBackground
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isCurrentAtBeat) "✕ Hapus Irama" else "+ Irama (${"%.2f".format(currentPlayheadMs / 1000f)}s)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onAutoBeats,
                            modifier = Modifier.weight(1f).height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SazfloySurfaceElevated,
                                contentColor = SazfloyAmber
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyAmber.copy(alpha = 0.5f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ Auto 120BPM", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        if (beats.isNotEmpty()) {
                            Button(
                                onClick = { onUpdateProject(project.copy(beatMarkersMs = emptyList())) },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SazfloySurfaceElevated,
                                    contentColor = SazfloyTextMuted
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Bersihkan", fontSize = 9.sp)
                            }
                        }
                    }

                    // Clickable Beat Chips to jump playhead
                    if (beats.isNotEmpty()) {
                        Text(
                            text = "Ketuk untuk lompat ke titik irama (Cut/Split/Sync):",
                            fontSize = 9.sp,
                            color = SazfloyTextSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(beats.indices.toList()) { idx ->
                                val beatMs = beats[idx]
                                val isSelected = kotlin.math.abs(beatMs - currentPlayheadMs) <= 100L
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) SazfloyAmber else SazfloySurfaceElevated)
                                        .border(1.dp, if (isSelected) SazfloyAmber else SazfloyBorder, RoundedCornerShape(6.dp))
                                        .clickable { onSeek(beatMs) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#${idx + 1} (${"%.2f".format(beatMs / 1000f)}s)",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SazfloyBackground else SazfloyAmber
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Soundtracks & Audio Import
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Soundtracks & Audio", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SazfloyCyan)
                    Button(
                        onClick = onImportAudioClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SazfloyToska, contentColor = SazfloyBackground),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Lagu (.mp3)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (project.audioTracks.isNotEmpty()) {
                    Text("Track Audio Aktif:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                    project.audioTracks.forEach { track ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SazfloySurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.title, color = SazfloyCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("${"%.1f".format(track.durationMs / 1000f)}s • Vol: ${(track.volume * 100).toInt()}%", fontSize = 10.sp, color = SazfloyTextMuted)
                                }
                                IconButton(
                                    onClick = {
                                        onUpdateProject(project.copy(audioTracks = project.audioTracks.filter { it.id != track.id }))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Track", tint = SazfloyPink, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Text("Preset BGM Sazfloy:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf("Midnight Beats", "Tropical Breeze", "Acoustic Morning", "Party Anthem")
                    items(presets) { name ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SazfloySurfaceVariant)
                                .clickable {
                                    onUpdateProject(
                                        project.copy(
                                            audioTracks = listOf(
                                                AudioTrack(
                                                    uri = name.lowercase().replace(" ", "_"),
                                                    title = name,
                                                    durationMs = project.totalDurationMs
                                                )
                                            )
                                        )
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(name, color = SazfloyTextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        VideoEditorToolTab.TRANSITION -> {
            if (selectedClip != null) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transisi Masuk: ${selectedClip.transitionType.displayName}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SazfloyViolet
                        )
                        Text(
                            text = "${selectedClip.transitionDurationMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = SazfloyCyan
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Durasi:", style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary, modifier = Modifier.width(50.dp))
                        Slider(
                            value = selectedClip.transitionDurationMs.toFloat(),
                            onValueChange = { onUpdateClip(selectedClip.copy(transitionDurationMs = it.toLong())) },
                            valueRange = 200f..2000f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SazfloyViolet, activeTrackColor = SazfloyViolet)
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TransitionType.values()) { trans ->
                            val isSel = selectedClip.transitionType == trans
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SazfloyViolet else SazfloySurfaceVariant)
                                    .clickable { onUpdateClip(selectedClip.copy(transitionType = trans)) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = trans.displayName,
                                    color = if (isSel) Color.White else SazfloyTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pilih sebuah klip pada timeline untuk mengatur efek transisi masuk.", color = SazfloyTextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        VideoEditorToolTab.CANVAS_RATIO -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AspectRatioMode.values().forEach { mode ->
                    val isSel = project.aspectRatio == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                            .clickable { onUpdateProject(project.copy(aspectRatio = mode)) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SazfloyTextSecondary, modifier = Modifier.width(80.dp))
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = SazfloyCyan, activeTrackColor = SazfloyCyan)
        )
        Text("${value.toInt()}", style = MaterialTheme.typography.labelSmall, color = SazfloyCyan, modifier = Modifier.width(32.dp))
    }
}

