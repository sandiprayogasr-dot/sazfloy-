package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.editor.video.VideoExportEngine
import com.example.model.AspectRatioMode
import com.example.model.EditingProject
import com.example.model.ExportConfiguration
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
import com.example.ui.theme.SazfloyViolet

@Composable
fun ExportDialog(
    project: EditingProject,
    onDismiss: () -> Unit,
    onStartExport: (ExportConfiguration) -> Unit,
    exportState: VideoExportEngine.ExportProgress?,
    onCancelExport: () -> Unit
) {
    val context = LocalContext.current

    var selectedResolution by remember { mutableStateOf("1080p") }
    var selectedFps by remember { mutableIntStateOf(30) }
    var selectedQuality by remember { mutableStateOf("Premiere Pro Master") }

    val resolutions = listOf("4K", "2K", "1080p", "720p", "480p")
    val fpsOptions = listOf(24, 30, 60)
    val qualities = listOf("Premiere Pro Master", "Adobe Studio High", "Web & Social HQ")

    val bitrateMbps = remember(selectedResolution, selectedQuality) {
        when (selectedQuality) {
            "Premiere Pro Master" -> when (selectedResolution) {
                "4K" -> 16
                "2K" -> 12
                "1080p" -> 10
                "720p" -> 6
                else -> 4
            }
            "Adobe Studio High" -> when (selectedResolution) {
                "4K" -> 14
                "2K" -> 10
                "1080p" -> 8
                "720p" -> 5
                else -> 3
            }
            else -> when (selectedResolution) {
                "4K" -> 10
                "2K" -> 8
                "1080p" -> 6
                "720p" -> 4
                else -> 2
            }
        }
    }

    // Calculate dimensions based on aspect ratio & resolution
    val (width, height) = remember(selectedResolution, project.aspectRatio) {
        val (longSide, shortSide) = when (selectedResolution) {
            "4K" -> 3840 to 2160
            "2K" -> 2560 to 1440
            "720p" -> 1280 to 720
            "480p" -> 854 to 480
            else -> 1920 to 1080
        }
        val (w, h) = when (project.aspectRatio) {
            AspectRatioMode.RATIO_9_16 -> shortSide to longSide
            AspectRatioMode.RATIO_16_9 -> longSide to shortSide
            AspectRatioMode.RATIO_1_1 -> shortSide to shortSide
            AspectRatioMode.RATIO_4_5 -> ((shortSide * 4) / 5) to shortSide
            AspectRatioMode.RATIO_4_3 -> longSide to ((longSide * 3) / 4)
            AspectRatioMode.RATIO_3_4 -> ((shortSide * 3) / 4) to shortSide
            AspectRatioMode.RATIO_ORIGINAL -> longSide to shortSide
        }
        // Round to multiple of 16 for AVC hardware encoder
        ((w / 16) * 16).coerceAtLeast(320) to ((h / 16) * 16).coerceAtLeast(240)
    }

    val estimatedMb = remember(bitrateMbps, selectedFps, project.totalDurationMs) {
        val durSec = project.totalDurationMs / 1000f
        val mb = (durSec * (bitrateMbps / 8f) * (if (selectedFps == 60) 1.2f else 1.0f)).coerceAtLeast(1.5f)
        "%.1f MB".format(mb)
    }


    Dialog(onDismissRequest = {
        if (exportState !is VideoExportEngine.ExportProgress.Progress) onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SazfloySurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Export Video",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SazfloyTextPrimary
                        )
                        Text(
                            text = "${project.title} • $estimatedMb",
                            style = MaterialTheme.typography.bodySmall,
                            color = SazfloyCyan
                        )
                    }

                    if (exportState !is VideoExportEngine.ExportProgress.Progress) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SazfloyTextSecondary
                            )
                        }
                    }
                }

                // If currently exporting or finished
                when (exportState) {
                    is VideoExportEngine.ExportProgress.Progress -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { exportState.percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SazfloyCyan,
                                trackColor = SazfloySurfaceVariant
                            )

                            Text(
                                text = "Encoding ${(exportState.percent * 100).toInt()}% • Frame ${exportState.currentFrame}/${exportState.totalFrames}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SazfloyTextPrimary
                            )

                            Text(
                                text = "Applying trims, color matrix, stickers & audio tracks...",
                                style = MaterialTheme.typography.bodySmall,
                                color = SazfloyTextMuted
                            )

                            OutlinedButton(
                                onClick = onCancelExport,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SazfloyPink)
                            ) {
                                Text("Cancel Export")
                            }
                        }
                    }
                    is VideoExportEngine.ExportProgress.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SazfloySuccess.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SazfloySuccess,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = "Export Completed!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SazfloyTextPrimary
                            )
                            Text(
                                text = "Saved to Gallery (Movies/SAZFLOY)",
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
                                            setDataAndType(exportState.outputUri, "video/mp4")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        runCatching { context.startActivity(intent) }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open")
                                }

                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, exportState.outputUri)
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Video via"))
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SazfloyCyan,
                                        contentColor = SazfloyBackground
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share")
                                }
                            }

                            TextButton(onClick = onDismiss) {
                                Text("Done", color = SazfloyTextSecondary)
                            }
                        }
                    }
                    is VideoExportEngine.ExportProgress.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Export failed: ${exportState.message}",
                                color = SazfloyPink,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = SazfloySurfaceVariant)
                            ) {
                                Text("Dismiss", color = SazfloyTextPrimary)
                            }
                        }
                    }
                    else -> {
                        // Configuration Controls
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Resolution Selector
                            Text(
                                text = "Resolution",
                                style = MaterialTheme.typography.labelMedium,
                                color = SazfloyTextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                resolutions.forEach { res ->
                                    val isSel = selectedResolution == res
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) SazfloyCyan else SazfloySurfaceVariant)
                                            .clickable { selectedResolution = res }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = res,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) SazfloyBackground else SazfloyTextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            // Frame Rate Selector
                            Text(
                                text = "Frame Rate (FPS)",
                                style = MaterialTheme.typography.labelMedium,
                                color = SazfloyTextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                fpsOptions.forEach { fps ->
                                    val isSel = selectedFps == fps
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) SazfloyViolet else SazfloySurfaceVariant)
                                            .clickable { selectedFps = fps }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$fps FPS",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SazfloyTextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            // Premiere Pro High Bitrate Profile Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bitrate Profile (Adobe Premiere Pro)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SazfloyTextSecondary
                                )
                                Text(
                                    text = "$bitrateMbps Mbps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SazfloyCyan
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                qualities.forEach { q ->
                                    val isSel = selectedQuality == q
                                    val profileBitrate = when (q) {
                                        "Premiere Pro Master" -> when (selectedResolution) { "4K" -> 80; "2K" -> 45; "1080p" -> 25; else -> 14 }
                                        "Adobe Studio High" -> when (selectedResolution) { "4K" -> 55; "2K" -> 32; "1080p" -> 18; else -> 10 }
                                        else -> when (selectedResolution) { "4K" -> 35; "2K" -> 20; "1080p" -> 12; else -> 6 }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) SazfloySurface else SazfloySurfaceVariant)
                                            .border(
                                                if (isSel) 1.5.dp else 0.dp,
                                                if (isSel) SazfloyCyan else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedQuality = q }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = q,
                                                color = if (isSel) SazfloyCyan else SazfloyTextPrimary,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                            )
                                            Text(
                                                text = "$profileBitrate Mbps CBR/VBR",
                                                color = if (isSel) SazfloyTextPrimary else SazfloyTextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Start Export Button
                            Button(
                                onClick = {
                                    val config = ExportConfiguration(
                                        resolutionName = selectedResolution,
                                        width = width,
                                        height = height,
                                        fps = selectedFps,
                                        quality = selectedQuality,
                                        bitrateMbps = bitrateMbps,
                                        estimatedSizeMb = estimatedMb.replace(" MB", "").toFloatOrNull() ?: 20f
                                    )
                                    onStartExport(config)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("start_export_confirm_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SazfloyCyan,
                                    contentColor = SazfloyBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Export $selectedResolution ($width x $height @ $bitrateMbps Mbps)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}
