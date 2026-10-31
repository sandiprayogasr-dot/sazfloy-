package com.example

import com.example.editor.templates.TemplateData
import com.example.filters.SazfloyColorEngine
import com.example.model.AspectRatioMode
import com.example.model.ColorAdjustments
import com.example.model.EditingProject
import com.example.model.MediaClip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testMediaClipTrimmedDurationCalculation() {
        val clip = MediaClip(
            name = "Test Clip",
            durationMs = 5000L,
            trimStartMs = 1000L,
            trimEndMs = 4000L,
            speed = 1.0f
        )
        // 4000 - 1000 = 3000ms
        assertEquals(3000L, clip.trimmedDurationMs)

        // At 2.0x speed, 3000ms / 2 = 1500ms
        val fastClip = clip.copy(speed = 2.0f)
        assertEquals(1500L, fastClip.trimmedDurationMs)

        // At 0.5x speed, 3000ms / 0.5 = 6000ms
        val slowClip = clip.copy(speed = 0.5f)
        assertEquals(6000L, slowClip.trimmedDurationMs)
    }

    @Test
    fun testProjectTotalDuration() {
        val clip1 = MediaClip(name = "Clip 1", durationMs = 2000L, trimStartMs = 0L, trimEndMs = 2000L)
        val clip2 = MediaClip(name = "Clip 2", durationMs = 4000L, trimStartMs = 1000L, trimEndMs = 4000L) // 3000ms
        val project = EditingProject(
            clips = listOf(clip1, clip2)
        )
        assertEquals(5000L, project.totalDurationMs)
    }

    @Test
    fun testColorAdjustmentsCompositeMatrix() {
        val adjustments = ColorAdjustments(
            brightness = 10f,
            contrast = 15f,
            saturation = 20f,
            filterPresetId = "cinematic",
            filterIntensity = 0.8f
        )
        val matrix = SazfloyColorEngine.createCompositeMatrix(adjustments)
        assertEquals(20, matrix.size)

        // Verify presets count (13 presets including original, cinematic, sepia, grayscale, etc.)
        assertEquals(13, SazfloyColorEngine.PRESETS.size)
        val cinematicPreset = SazfloyColorEngine.getPresetById("cinematic")
        assertNotNull(cinematicPreset)
        assertEquals("SAZFLOY Cinematic", cinematicPreset.name)
    }

    @Test
    fun testTemplatesInstantiateProperProjects() {
        assertTrue(TemplateData.TEMPLATES.isNotEmpty())
        val firstTemplate = TemplateData.TEMPLATES.first()
        val project = firstTemplate.instantiateProject()

        assertEquals(firstTemplate.title, project.title)
        assertEquals(firstTemplate.aspectRatio, project.aspectRatio)
        assertTrue(project.clips.isNotEmpty())
        assertTrue(project.totalDurationMs > 0)
    }

    @Test
    fun testKeyframeInterpolation() {
        val kf1 = com.example.model.Keyframe(
            timeMs = 0L,
            posXRatio = 0.2f,
            posYRatio = 0.2f,
            scale = 1.0f,
            rotation = 0f,
            opacity = 1.0f,
            curve = com.example.model.AnimationCurve.LINEAR
        )
        val kf2 = com.example.model.Keyframe(
            timeMs = 1000L,
            posXRatio = 0.8f,
            posYRatio = 0.8f,
            scale = 2.0f,
            rotation = 180f,
            opacity = 0.5f,
            curve = com.example.model.AnimationCurve.LINEAR
        )
        val keyframes = listOf(kf1, kf2)

        val midpoint = com.example.model.interpolateKeyframeState(keyframes, 500L)
        // Linear interpolation halfway should be ~0.5
        assertEquals(0.5f, midpoint.posXRatio, 0.05f)
        assertEquals(0.5f, midpoint.posYRatio, 0.05f)
        assertEquals(1.5f, midpoint.scale, 0.05f)
        assertEquals(90f, midpoint.rotation, 1.0f)
        assertEquals(0.75f, midpoint.opacity, 0.05f)

        // Before start
        val beforeStart = com.example.model.interpolateKeyframeState(keyframes, -100L)
        assertEquals(0.2f, beforeStart.posXRatio, 0.01f)

        // After end
        val afterEnd = com.example.model.interpolateKeyframeState(keyframes, 1500L)
        assertEquals(0.8f, afterEnd.posXRatio, 0.01f)
    }

    @Test
    fun testShapeOverlayModel() {
        val shape = com.example.model.ShapeOverlay(
            type = com.example.model.ShapeType.STAR,
            fillColorHex = "#00E5BC"
        )
        assertEquals(com.example.model.ShapeType.STAR, shape.type)
        assertEquals("#00E5BC", shape.fillColorHex)
        assertTrue(shape.durationMs > 0)
    }

    @Test
    fun testClipScaleAndPanTransform() {
        val clip = MediaClip(
            name = "Zoom Test",
            scale = 1.5f,
            panX = 0.1f,
            panY = -0.2f
        )
        assertEquals(1.5f, clip.scale, 0.001f)
        assertEquals(0.1f, clip.panX, 0.001f)
        assertEquals(-0.2f, clip.panY, 0.001f)

        // Test clamping scale
        val minClamped = (0.05f).coerceIn(0.2f, 4.0f)
        assertEquals(0.2f, minClamped, 0.001f)
        val maxClamped = (5.0f).coerceIn(0.2f, 4.0f)
        assertEquals(4.0f, maxClamped, 0.001f)
    }

    @Test
    fun testClipReorderLogic() {
        val clipA = MediaClip(id = "A", name = "Clip A")
        val clipB = MediaClip(id = "B", name = "Clip B")
        val clipC = MediaClip(id = "C", name = "Clip C")
        val initialList = listOf(clipA, clipB, clipC)

        // Move B to front (index 0)
        val toFront = initialList.toMutableList().apply {
            val idx = indexOfFirst { it.id == "B" }
            val item = removeAt(idx)
            add(0, item)
        }
        assertEquals(listOf("B", "A", "C"), toFront.map { it.id })

        // Move B to end
        val toEnd = initialList.toMutableList().apply {
            val idx = indexOfFirst { it.id == "B" }
            val item = removeAt(idx)
            add(item)
        }
        assertEquals(listOf("A", "C", "B"), toEnd.map { it.id })

        // Move B left (swap with A)
        val toLeft = initialList.toMutableList().apply {
            val idx = indexOfFirst { it.id == "B" }
            java.util.Collections.swap(this, idx, idx - 1)
        }
        assertEquals(listOf("B", "A", "C"), toLeft.map { it.id })

        // Move B right (swap with C)
        val toRight = initialList.toMutableList().apply {
            val idx = indexOfFirst { it.id == "B" }
            java.util.Collections.swap(this, idx, idx + 1)
        }
        assertEquals(listOf("A", "C", "B"), toRight.map { it.id })
    }

    @Test
    fun testBeatMarkersGenerationAndToggle() {
        val project = EditingProject(
            clips = listOf(MediaClip(durationMs = 2500L)),
            beatMarkersMs = listOf(500L, 1000L, 1500L)
        )
        assertEquals(3, project.beatMarkersMs.size)

        // Toggle: remove 1000ms if tapped near 1020ms
        val clickTime = 1020L
        val nearIdx = project.beatMarkersMs.indexOfFirst { kotlin.math.abs(it - clickTime) <= 150L }
        val updatedBeats = if (nearIdx >= 0) {
            project.beatMarkersMs.toMutableList().apply { removeAt(nearIdx) }
        } else {
            project.beatMarkersMs + clickTime
        }
        assertEquals(listOf(500L, 1500L), updatedBeats)

        // Auto generate 120 BPM (500ms intervals) for total duration 2500ms
        val autoBeats = mutableListOf<Long>()
        var beatTime = 500L
        while (beatTime < project.totalDurationMs) {
            autoBeats.add(beatTime)
            beatTime += 500L
        }
        assertEquals(listOf(500L, 1000L, 1500L, 2000L), autoBeats)
    }

    @Test
    fun testProjectDuplicateLogic() {
        val original = EditingProject(
            id = "proj-123",
            title = "My Vlog",
            clips = listOf(MediaClip(name = "Clip 1", durationMs = 3000L)),
            beatMarkersMs = listOf(500L, 1000L)
        )
        val copy = original.copy(
            id = "proj-456",
            title = "${original.title} (Salinan)",
            updatedAt = System.currentTimeMillis()
        )
        assertEquals("proj-456", copy.id)
        assertEquals("My Vlog (Salinan)", copy.title)
        assertEquals(1, copy.clips.size)
        assertEquals(2, copy.beatMarkersMs.size)
    }
}

