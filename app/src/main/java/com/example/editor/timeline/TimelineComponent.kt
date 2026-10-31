package com.example.editor.timeline

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.model.ShapeOverlay
import com.example.model.AudioTrack
import com.example.model.MediaClip
import com.example.model.TextOverlay
import com.example.model.TransitionType
import com.example.model.VideoEffect
import com.example.ui.theme.SazfloyAmber
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
import com.example.ui.theme.SazfloyVioletLight
import kotlin.math.roundToInt

@Composable
fun TimelineComponent(
    totalDurationMs: Long,
    currentPlayheadMs: Long,
    clips: List<MediaClip>,
    selectedClipId: String?,
    textOverlays: List<TextOverlay>,
    selectedTextId: String?,
    audioTracks: List<AudioTrack>,
    selectedAudioId: String?,
    shapeOverlays: List<ShapeOverlay> = emptyList(),
    selectedShapeId: String? = null,
    effects: List<VideoEffect> = emptyList(),
    selectedEffectId: String? = null,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectText: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectShape: (String) -> Unit = {},
    onSelectEffect: (String) -> Unit = {},
    onSelectTransition: (String) -> Unit = {},
    onSplitClip: (String, Long) -> Unit,
    onDeleteClip: (String) -> Unit,
    onDuplicateClip: (String) -> Unit,
    onMoveClipLeft: ((String) -> Unit)? = null,
    onMoveClipRight: ((String) -> Unit)? = null,
    onAddKeyframe: ((String) -> Unit)? = null,
    onAddMediaClick: (() -> Unit)? = null,
    beatMarkersMs: List<Long> = emptyList(),
    onToggleBeatMarker: ((Long) -> Unit)? = null,
    onAutoBeats: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) } // 0.6x to 2.5x
    val pixelsPerSecond = 80.dp * zoomScale
    val scrollState = rememberScrollState()

    val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(5f)
    val timelineWidthDp = (totalSeconds * (80f * zoomScale)).dp + 300.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SazfloySurface)
            .border(1.dp, SazfloyDivider)
            .testTag("timeline_root")
    ) {
        // Quick Action Bar above timeline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SazfloySurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Split Action
                IconButton(
                    onClick = {
                        selectedClipId?.let { clipId ->
                            onSplitClip(clipId, currentPlayheadMs)
                        }
                    },
                    enabled = selectedClipId != null,
                    modifier = Modifier.size(36.dp).testTag("action_split_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "Split Clip",
                        tint = if (selectedClipId != null) SazfloyCyan else SazfloyTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Move Clip Left / Right with position index badge
                val clipIdx = clips.indexOfFirst { it.id == selectedClipId }
                if (clipIdx >= 0 && (onMoveClipLeft != null || onMoveClipRight != null)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier
                            .background(Color(0xFF1E222D), RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { selectedClipId?.let { onMoveClipLeft?.invoke(it) } },
                            enabled = onMoveClipLeft != null && clipIdx > 0,
                            modifier = Modifier.size(26.dp).testTag("action_move_left")
                        ) {
                            Text("◀", color = if (clipIdx > 0) SazfloyCyan else SazfloyTextMuted, fontSize = 11.sp)
                        }
                        Text(
                            text = "${clipIdx + 1}/${clips.size}",
                            color = SazfloyCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        IconButton(
                            onClick = { selectedClipId?.let { onMoveClipRight?.invoke(it) } },
                            enabled = onMoveClipRight != null && clipIdx < clips.lastIndex,
                            modifier = Modifier.size(26.dp).testTag("action_move_right")
                        ) {
                            Text("▶", color = if (clipIdx < clips.lastIndex) SazfloyCyan else SazfloyTextMuted, fontSize = 11.sp)
                        }
                    }
                }

                // CapCut-Style Beat Marker ("Irama") Button
                if (onToggleBeatMarker != null) {
                    val isAtBeat = beatMarkersMs.any { kotlin.math.abs(it - currentPlayheadMs) <= 100L }
                    Surface(
                        onClick = { onToggleBeatMarker(currentPlayheadMs) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isAtBeat) SazfloyAmber else Color(0xFF2C2415),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyAmber.copy(alpha = 0.7f)),
                        modifier = Modifier.height(28.dp).testTag("action_beat_marker")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text("🟡", fontSize = 9.sp)
                            Text(
                                text = if (isAtBeat) "Hapus Irama" else "+ Irama",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAtBeat) Color.Black else SazfloyAmber
                            )
                        }
                    }
                }

                if (onAutoBeats != null) {
                    Surface(
                        onClick = { onAutoBeats() },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E222D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.height(28.dp).testTag("action_auto_beats")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text("⚡", fontSize = 11.sp)
                            Text("Auto", fontSize = 10.sp, color = SazfloyAmber, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Duplicate Action
                IconButton(
                    onClick = { selectedClipId?.let { onDuplicateClip(it) } },
                    enabled = selectedClipId != null,
                    modifier = Modifier.size(36.dp).testTag("action_duplicate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Clip",
                        tint = if (selectedClipId != null) SazfloyTextPrimary else SazfloyTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Action
                IconButton(
                    onClick = { selectedClipId?.let { onDeleteClip(it) } },
                    enabled = selectedClipId != null && clips.size > 1,
                    modifier = Modifier.size(36.dp).testTag("action_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Clip",
                        tint = if (selectedClipId != null && clips.size > 1) SazfloyPink else SazfloyTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add Keyframe button
                if (onAddKeyframe != null && (selectedClipId != null || selectedTextId != null || selectedShapeId != null)) {
                    val targetId = selectedShapeId ?: selectedTextId ?: selectedClipId
                    if (targetId != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SazfloyToska.copy(alpha = 0.2f))
                                .clickable { onAddKeyframe(targetId) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "◆ Keyframe",
                                color = SazfloyToska,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }


            // Zoom In / Out Controls
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(
                    onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(32.dp).testTag("timeline_zoom_out")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = SazfloyTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SazfloyTextMuted
                )
                IconButton(
                    onClick = { zoomScale = (zoomScale + 0.25f).coerceIn(0.5f, 2.5f) },
                    modifier = Modifier.size(32.dp).testTag("timeline_zoom_in")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = SazfloyTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Horizontal Multi-Track Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .horizontalScroll(scrollState)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val clickedSec = offset.x / (80f * zoomScale)
                        val targetMs = (clickedSec * 1000L).toLong().coerceIn(0L, totalDurationMs)
                        onSeek(targetMs)
                    }
                }
        ) {
            Box(modifier = Modifier.width(timelineWidthDp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 60.dp, top = 4.dp, bottom = 8.dp)) {
                    // 1. Time Ruler
                    TimelineRuler(
                        totalDurationMs = totalDurationMs,
                        pixelsPerSecond = pixelsPerSecond,
                        zoomScale = zoomScale
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Video Clips Track
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(SazfloySurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        clips.forEachIndexed { index, clip ->
                            // Transition button between clips
                            if (index > 0) {
                                val hasTransition = clip.transitionType != TransitionType.NONE
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (hasTransition) SazfloyViolet else SazfloySurfaceElevated)
                                        .border(1.dp, if (hasTransition) SazfloyVioletLight else SazfloyBorder, CircleShape)
                                        .clickable { onSelectTransition(clip.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Transisi",
                                        tint = if (hasTransition) Color.White else SazfloyTextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(3.dp))
                            }

                            val clipWidthDp = ((clip.trimmedDurationMs / 1000f) * (80f * zoomScale)).dp.coerceAtLeast(54.dp)
                            val isSelected = clip.id == selectedClipId
                            val borderColor by animateColorAsState(
                                if (isSelected) SazfloyCyan else SazfloyBorder,
                                label = "clip_border"
                            )

                            Box(
                                modifier = Modifier
                                    .width(clipWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) SazfloySurfaceElevated else SazfloySurface)
                                    .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(6.dp))
                                    .clickable { onSelectClip(clip.id) },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                // Thumbnail Background
                                val thumbModel = clip.thumbnailUri ?: clip.uri
                                if (thumbModel.isNotBlank()) {
                                    AsyncImage(
                                        model = thumbModel,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(if (isSelected) 0.55f else 0.38f)
                                    )
                                }

                                // Dark overlay for text legibility
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Black.copy(alpha = 0.75f),
                                                    Color.Black.copy(alpha = 0.45f)
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = if (isSelected) SazfloyCyan else SazfloyTextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = clip.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            color = SazfloyTextPrimary,
                                            maxLines = 1
                                        )
                                        if (clip.keyframes.isNotEmpty()) {
                                            Text(
                                                text = "◆ ${clip.keyframes.size}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = SazfloyToska
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${"%.1f".format(clip.trimmedDurationMs / 1000f)}s • ${clip.speed}x",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (isSelected) SazfloyCyan else SazfloyTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(3.dp))
                        }

                        if (onAddMediaClick != null) {
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SazfloySurfaceElevated)
                                    .border(1.dp, SazfloyToska.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable { onAddMediaClick() }
                                    .padding(4.dp)
                                    .testTag("timeline_add_media_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Media",
                                        tint = SazfloyToska,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Add",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = SazfloyToska
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Text Overlays Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color(0xFF191B24), RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        textOverlays.forEach { textItem ->
                            val offsetDp = ((textItem.startMs / 1000f) * (80f * zoomScale)).dp
                            val textWidthDp = ((textItem.durationMs / 1000f) * (80f * zoomScale)).dp.coerceAtLeast(36.dp)
                            val isSelected = textItem.id == selectedTextId

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                    .width(textWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) SazfloyAmber else Color(0xFF5D4037))
                                    .clickable { onSelectText(textItem.id) }
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.TextFields,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = textItem.text.ifBlank { "Text" },
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        maxLines = 1
                                    )
                                    if (textItem.keyframes.isNotEmpty()) {
                                        Text("◆", color = SazfloyToska, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3.5 Shapes Track
                    if (shapeOverlays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(Color(0xFF142422), RoundedCornerShape(6.dp))
                                .padding(2.dp)
                        ) {
                            shapeOverlays.forEach { shape ->
                                val offsetDp = ((shape.startMs / 1000f) * (80f * zoomScale)).dp
                                val shapeWidthDp = ((shape.durationMs / 1000f) * (80f * zoomScale)).dp.coerceAtLeast(36.dp)
                                val isSelected = shape.id == selectedShapeId

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                        .width(shapeWidthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) SazfloyToska else Color(0xFF004D40))
                                        .clickable { onSelectShape(shape.id) }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = shape.type.name.take(4),
                                            color = if (isSelected) Color.Black else Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        if (shape.keyframes.isNotEmpty()) {
                                            Text("◆", color = if (isSelected) Color.Black else SazfloyToska, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3.8 Video Effects Track
                    if (effects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(Color(0xFF201328), RoundedCornerShape(6.dp))
                                .padding(2.dp)
                        ) {
                            effects.forEach { effect ->
                                val offsetDp = ((effect.startMs / 1000f) * (80f * zoomScale)).dp
                                val effectWidthDp = ((effect.durationMs / 1000f) * (80f * zoomScale)).dp.coerceAtLeast(42.dp)
                                val isSelected = effect.id == selectedEffectId

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                        .width(effectWidthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) SazfloyViolet else Color(0xFF4A154B))
                                        .border(
                                            1.dp,
                                            if (isSelected) SazfloyVioletLight else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { onSelectEffect(effect.id) }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = effect.name.ifBlank { effect.type.displayName },
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                            maxLines = 1
                                        )
                                        if (!effect.isEnabled) {
                                            Text(
                                                text = "(Off)",
                                                color = Color.White.copy(alpha = 0.6f),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. Audio Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color(0xFF151822), RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        audioTracks.forEach { audio ->
                            val offsetDp = ((audio.startMs / 1000f) * (80f * zoomScale)).dp
                            val audioWidthDp = ((audio.durationMs / 1000f) * (80f * zoomScale)).dp.coerceAtLeast(48.dp)
                            val isSelected = audio.id == selectedAudioId

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                    .width(audioWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) SazfloyViolet else Color(0xFF311B92))
                                    .clickable { onSelectAudio(audio.id) }
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = audio.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Render Beat Markers (Titik Irama CapCut)
                        beatMarkersMs.forEach { beatMs ->
                            val beatOffsetDp = ((beatMs / 1000f) * (80f * zoomScale)).dp
                            val isNearPlayhead = kotlin.math.abs(beatMs - currentPlayheadMs) <= 45L
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(beatOffsetDp.roundToPx() - 4.dp.roundToPx(), 7.dp.roundToPx()) }
                                    .size(if (isNearPlayhead) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(if (isNearPlayhead) Color.White else SazfloyAmber)
                                    .border(1.dp, if (isNearPlayhead) SazfloyCyan else Color.Black, CircleShape)
                            )
                        }
                    }
                }

                // 5. Draggable Playhead Scrubber Line
                val playheadOffsetDp = 16.dp + ((currentPlayheadMs / 1000f) * (80f * zoomScale)).dp
                val isPlayheadOnBeat = beatMarkersMs.any { kotlin.math.abs(it - currentPlayheadMs) <= 45L }
                val playheadColor = if (isPlayheadOnBeat) SazfloyAmber else SazfloyCyan

                Box(
                    modifier = Modifier
                        .offset { IntOffset(playheadOffsetDp.roundToPx(), 0) }
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaSec = dragAmount.x / (80f * zoomScale)
                                val newTimeMs = (currentPlayheadMs + (deltaSec * 1000L).toLong()).coerceIn(0L, totalDurationMs)
                                onSeek(newTimeMs)
                            }
                        }
                ) {
                    // Playhead Top Pointer Cap
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .offset(x = (-8).dp, y = 0.dp)
                            .clip(CircleShape)
                            .background(playheadColor)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    // Playhead Vertical Line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(playheadColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineRuler(
    totalDurationMs: Long,
    pixelsPerSecond: androidx.compose.ui.unit.Dp,
    zoomScale: Float
) {
    val maxSeconds = (totalDurationMs / 1000f).toInt() + 4
    val step = when {
        zoomScale < 0.8f -> 2
        zoomScale > 1.8f -> 1
        else -> 1
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        for (sec in 0..maxSeconds step step) {
            val width = (pixelsPerSecond * step.toFloat())
            Column(
                modifier = Modifier.width(width),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${sec}s",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = SazfloyTextMuted
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(6.dp)
                        .background(SazfloyDivider)
                )
            }
        }
    }
}
