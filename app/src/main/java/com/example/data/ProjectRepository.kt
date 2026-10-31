package com.example.data

import android.content.Context
import androidx.room.Room
import com.example.model.AspectRatioMode
import com.example.model.AudioTrack
import com.example.model.ClipType
import com.example.model.ColorAdjustments
import com.example.model.EditingProject
import com.example.model.MediaClip
import com.example.model.ProjectType
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import com.example.model.TransitionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ProjectRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "sazfloy_projects.db"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.projectDao()

    val allProjects: Flow<List<EditingProject>> = dao.getAllProjectsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getProject(id: String): EditingProject? {
        return dao.getProjectById(id)?.toDomain()
    }

    suspend fun saveProject(project: EditingProject) {
        dao.insertOrUpdate(project.toEntity())
    }

    suspend fun deleteProject(id: String) {
        dao.deleteById(id)
    }

    suspend fun updateProjectTitle(id: String, newTitle: String) {
        dao.updateTitle(id, newTitle, System.currentTimeMillis())
    }

    // JSON serialization mappings
    private fun List<com.example.model.Keyframe>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { kf ->
            arr.put(JSONObject().apply {
                put("id", kf.id)
                put("timeMs", kf.timeMs)
                put("posXRatio", kf.posXRatio.toDouble())
                put("posYRatio", kf.posYRatio.toDouble())
                put("scale", kf.scale.toDouble())
                put("rotation", kf.rotation.toDouble())
                put("opacity", kf.opacity.toDouble())
                put("curve", kf.curve.name)
            })
        }
        return arr
    }

    private fun parseKeyframes(arrStr: String?): List<com.example.model.Keyframe> {
        if (arrStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<com.example.model.Keyframe>()
        try {
            val arr = JSONArray(arrStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    com.example.model.Keyframe(
                        id = obj.optString("id"),
                        timeMs = obj.optLong("timeMs", 0L),
                        posXRatio = obj.optDouble("posXRatio", 0.5).toFloat(),
                        posYRatio = obj.optDouble("posYRatio", 0.5).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        opacity = obj.optDouble("opacity", 1.0).toFloat(),
                        curve = runCatching { com.example.model.AnimationCurve.valueOf(obj.optString("curve")) }.getOrDefault(com.example.model.AnimationCurve.EASE_IN_OUT)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun ColorAdjustments.toJsonObject(): JSONObject {
        val wheels = colorWheels
        val wheelsObj = JSONObject().apply {
            put("liftHue", wheels.lift.hueDegrees.toDouble())
            put("liftSat", wheels.lift.saturation.toDouble())
            put("liftLum", wheels.lift.luminance.toDouble())
            put("gammaHue", wheels.gamma.hueDegrees.toDouble())
            put("gammaSat", wheels.gamma.saturation.toDouble())
            put("gammaLum", wheels.gamma.luminance.toDouble())
            put("gainHue", wheels.gain.hueDegrees.toDouble())
            put("gainSat", wheels.gain.saturation.toDouble())
            put("gainLum", wheels.gain.luminance.toDouble())
            put("offsetHue", wheels.offset.hueDegrees.toDouble())
            put("offsetSat", wheels.offset.saturation.toDouble())
            put("offsetLum", wheels.offset.luminance.toDouble())
        }
        return JSONObject().apply {
            put("brightness", brightness.toDouble())
            put("contrast", contrast.toDouble())
            put("saturation", saturation.toDouble())
            put("temperature", temperature.toDouble())
            put("tint", tint.toDouble())
            put("vignette", vignette.toDouble())
            put("sharpness", sharpness.toDouble())
            put("exposure", exposure.toDouble())
            put("highlights", highlights.toDouble())
            put("shadows", shadows.toDouble())
            put("blurRadius", blurRadius.toDouble())
            put("filterPresetId", filterPresetId)
            put("filterIntensity", filterIntensity.toDouble())
            put("activeLutId", activeLutId)
            put("lutIntensity", lutIntensity.toDouble())
            put("wheels", wheelsObj)
        }
    }

    private fun parseColorAdjustments(obj: JSONObject?): ColorAdjustments {
        if (obj == null) return ColorAdjustments()
        val wheelsObj = obj.optJSONObject("wheels")
        val wheels = if (wheelsObj != null) {
            com.example.model.ColorWheelsData(
                lift = com.example.model.ColorWheelTonal(
                    hueDegrees = wheelsObj.optDouble("liftHue", 0.0).toFloat(),
                    saturation = wheelsObj.optDouble("liftSat", 0.0).toFloat(),
                    luminance = wheelsObj.optDouble("liftLum", 0.0).toFloat()
                ),
                gamma = com.example.model.ColorWheelTonal(
                    hueDegrees = wheelsObj.optDouble("gammaHue", 0.0).toFloat(),
                    saturation = wheelsObj.optDouble("gammaSat", 0.0).toFloat(),
                    luminance = wheelsObj.optDouble("gammaLum", 0.0).toFloat()
                ),
                gain = com.example.model.ColorWheelTonal(
                    hueDegrees = wheelsObj.optDouble("gainHue", 0.0).toFloat(),
                    saturation = wheelsObj.optDouble("gainSat", 0.0).toFloat(),
                    luminance = wheelsObj.optDouble("gainLum", 0.0).toFloat()
                ),
                offset = com.example.model.ColorWheelTonal(
                    hueDegrees = wheelsObj.optDouble("offsetHue", 0.0).toFloat(),
                    saturation = wheelsObj.optDouble("offsetSat", 0.0).toFloat(),
                    luminance = wheelsObj.optDouble("offsetLum", 0.0).toFloat()
                )
            )
        } else com.example.model.ColorWheelsData()

        return ColorAdjustments(
            brightness = obj.optDouble("brightness", 0.0).toFloat(),
            contrast = obj.optDouble("contrast", 0.0).toFloat(),
            saturation = obj.optDouble("saturation", 0.0).toFloat(),
            temperature = obj.optDouble("temperature", 0.0).toFloat(),
            tint = obj.optDouble("tint", 0.0).toFloat(),
            vignette = obj.optDouble("vignette", 0.0).toFloat(),
            sharpness = obj.optDouble("sharpness", 0.0).toFloat(),
            exposure = obj.optDouble("exposure", 0.0).toFloat(),
            highlights = obj.optDouble("highlights", 0.0).toFloat(),
            shadows = obj.optDouble("shadows", 0.0).toFloat(),
            blurRadius = obj.optDouble("blurRadius", 0.0).toFloat(),
            filterPresetId = obj.optString("filterPresetId", "none"),
            filterIntensity = obj.optDouble("filterIntensity", 1.0).toFloat(),
            activeLutId = obj.optString("activeLutId", "none"),
            lutIntensity = obj.optDouble("lutIntensity", 1.0).toFloat(),
            colorWheels = wheels
        )
    }

    private fun EditingProject.toEntity(): ProjectEntity {
        val clipsArray = JSONArray()
        clips.forEach { clip ->
            val obj = JSONObject().apply {
                put("id", clip.id)
                put("uri", clip.uri)
                put("thumbnailUri", clip.thumbnailUri ?: "")
                put("name", clip.name)
                put("type", clip.type.name)
                put("durationMs", clip.durationMs)
                put("trimStartMs", clip.trimStartMs)
                put("trimEndMs", clip.trimEndMs)
                put("speed", clip.speed.toDouble())
                put("volume", clip.volume.toDouble())
                put("isMuted", clip.isMuted)
                put("rotationDegrees", clip.rotationDegrees)
                put("isFlippedHorizontal", clip.isFlippedHorizontal)
                put("isFlippedVertical", clip.isFlippedVertical)
                put("transitionType", clip.transitionType.name)
                put("transitionDurationMs", clip.transitionDurationMs)
                put("scale", clip.scale.toDouble())
                put("panX", clip.panX.toDouble())
                put("panY", clip.panY.toDouble())
                put("keyframes", clip.keyframes.toJsonArray())
                put("adjustments", clip.adjustments.toJsonObject())
            }
            clipsArray.put(obj)
        }

        val textsArray = JSONArray()
        textOverlays.forEach { text ->
            val obj = JSONObject().apply {
                put("id", text.id)
                put("text", text.text)
                put("startMs", text.startMs)
                put("durationMs", text.durationMs)
                put("posXRatio", text.posXRatio.toDouble())
                put("posYRatio", text.posYRatio.toDouble())
                put("fontSizeSp", text.fontSizeSp.toDouble())
                put("fontColorHex", text.fontColorHex)
                put("fontFamilyName", text.fontFamilyName)
                put("hasBackground", text.hasBackground)
                put("backgroundColorHex", text.backgroundColorHex)
                put("hasOutline", text.hasOutline)
                put("outlineColorHex", text.outlineColorHex)
                put("isBold", text.isBold)
                put("isItalic", text.isItalic)
                put("animationType", text.animationType)
                put("rotation", text.rotation.toDouble())
                put("textAlign", text.textAlign)
                put("keyframes", text.keyframes.toJsonArray())
            }
            textsArray.put(obj)
        }

        val stickersArray = JSONArray()
        stickerOverlays.forEach { sticker ->
            val obj = JSONObject().apply {
                put("id", sticker.id)
                put("emojiOrTag", sticker.emojiOrTag)
                put("startMs", sticker.startMs)
                put("durationMs", sticker.durationMs)
                put("posXRatio", sticker.posXRatio.toDouble())
                put("posYRatio", sticker.posYRatio.toDouble())
                put("scale", sticker.scale.toDouble())
                put("rotation", sticker.rotation.toDouble())
                put("keyframes", sticker.keyframes.toJsonArray())
            }
            stickersArray.put(obj)
        }

        val shapesArray = JSONArray()
        shapeOverlays.forEach { shape ->
            val obj = JSONObject().apply {
                put("id", shape.id)
                put("type", shape.type.name)
                put("startMs", shape.startMs)
                put("durationMs", shape.durationMs)
                put("posXRatio", shape.posXRatio.toDouble())
                put("posYRatio", shape.posYRatio.toDouble())
                put("widthRatio", shape.widthRatio.toDouble())
                put("heightRatio", shape.heightRatio.toDouble())
                put("fillColorHex", shape.fillColorHex)
                put("strokeColorHex", shape.strokeColorHex)
                put("strokeWidth", shape.strokeWidth.toDouble())
                put("opacity", shape.opacity.toDouble())
                put("rotation", shape.rotation.toDouble())
                put("keyframes", shape.keyframes.toJsonArray())
            }
            shapesArray.put(obj)
        }

        val audiosArray = JSONArray()
        audioTracks.forEach { audio ->
            val obj = JSONObject().apply {
                put("id", audio.id)
                put("uri", audio.uri)
                put("title", audio.title)
                put("startMs", audio.startMs)
                put("durationMs", audio.durationMs)
                put("volume", audio.volume.toDouble())
                put("fadeInMs", audio.fadeInMs)
                put("fadeOutMs", audio.fadeOutMs)
                put("isMuted", audio.isMuted)
                val beatsArr = JSONArray()
                audio.beatMarkersMs.forEach { beatsArr.put(it) }
                put("beatMarkersMs", beatsArr)
            }
            audiosArray.put(obj)
        }

        val adjObj = adjustments.toJsonObject().apply {
            val projectBeatsArr = JSONArray()
            beatMarkersMs.forEach { projectBeatsArr.put(it) }
            put("beatMarkersMs", projectBeatsArr)

            val effectsArray = JSONArray()
            effects.forEach { eff ->
                effectsArray.put(JSONObject().apply {
                    put("id", eff.id)
                    put("type", eff.type.id)
                    put("name", eff.name)
                    put("startMs", eff.startMs)
                    put("durationMs", eff.durationMs)
                    put("intensity", eff.intensity.toDouble())
                    put("isEnabled", eff.isEnabled)
                    put("targetClipId", eff.targetClipId ?: "")
                })
            }
            put("effects", effectsArray)
        }

        return ProjectEntity(
            id = id,
            title = title,
            type = type.name,
            aspectRatio = aspectRatio.name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            thumbnailUri = thumbnailUri,
            clipsJson = clipsArray.toString(),
            textOverlaysJson = textsArray.toString(),
            stickerOverlaysJson = stickersArray.toString(),
            shapeOverlaysJson = shapesArray.toString(),
            audioTracksJson = audiosArray.toString(),
            adjustmentsJson = adjObj.toString()
        )
    }

    private fun ProjectEntity.toDomain(): EditingProject {
        val clipsList = mutableListOf<MediaClip>()
        val effectsList = mutableListOf<com.example.model.VideoEffect>()
        try {
            val arr = JSONArray(clipsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                clipsList.add(
                    MediaClip(
                        id = obj.optString("id"),
                        uri = obj.optString("uri"),
                        thumbnailUri = obj.optString("thumbnailUri").takeIf { it.isNotBlank() },
                        name = obj.optString("name", "Clip ${i + 1}"),
                        type = runCatching { ClipType.valueOf(obj.optString("type")) }.getOrDefault(ClipType.VIDEO),
                        durationMs = obj.optLong("durationMs", 3000L),
                        trimStartMs = obj.optLong("trimStartMs", 0L),
                        trimEndMs = obj.optLong("trimEndMs", 3000L),
                        speed = obj.optDouble("speed", 1.0).toFloat(),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        isMuted = obj.optBoolean("isMuted", false),
                        rotationDegrees = obj.optInt("rotationDegrees", 0),
                        isFlippedHorizontal = obj.optBoolean("isFlippedHorizontal", false),
                        isFlippedVertical = obj.optBoolean("isFlippedVertical", false),
                        transitionType = runCatching { TransitionType.valueOf(obj.optString("transitionType")) }.getOrDefault(TransitionType.NONE),
                        transitionDurationMs = obj.optLong("transitionDurationMs", 500L),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        panX = obj.optDouble("panX", 0.0).toFloat(),
                        panY = obj.optDouble("panY", 0.0).toFloat(),
                        keyframes = parseKeyframes(obj.optString("keyframes")),
                        adjustments = parseColorAdjustments(obj.optJSONObject("adjustments"))
                    )
                )
            }
        } catch (_: Exception) {}

        val textsList = mutableListOf<TextOverlay>()
        try {
            val arr = JSONArray(textOverlaysJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                textsList.add(
                    TextOverlay(
                        id = obj.optString("id"),
                        text = obj.optString("text", ""),
                        startMs = obj.optLong("startMs", 0L),
                        durationMs = obj.optLong("durationMs", 3000L),
                        posXRatio = obj.optDouble("posXRatio", 0.5).toFloat(),
                        posYRatio = obj.optDouble("posYRatio", 0.75).toFloat(),
                        fontSizeSp = obj.optDouble("fontSizeSp", 24.0).toFloat(),
                        fontColorHex = obj.optString("fontColorHex", "#FFFFFF"),
                        fontFamilyName = obj.optString("fontFamilyName", "montserrat"),
                        hasBackground = obj.optBoolean("hasBackground", false),
                        backgroundColorHex = obj.optString("backgroundColorHex", "#00000000"),
                        hasOutline = obj.optBoolean("hasOutline", true),
                        outlineColorHex = obj.optString("outlineColorHex", "#000000"),
                        isBold = obj.optBoolean("isBold", true),
                        isItalic = obj.optBoolean("isItalic", false),
                        animationType = obj.optString("animationType", "Fade"),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        textAlign = obj.optString("textAlign", "center"),
                        keyframes = parseKeyframes(obj.optString("keyframes"))
                    )
                )
            }
        } catch (_: Exception) {}

        val stickersList = mutableListOf<StickerOverlay>()
        try {
            val arr = JSONArray(stickerOverlaysJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                stickersList.add(
                    StickerOverlay(
                        id = obj.optString("id"),
                        emojiOrTag = obj.optString("emojiOrTag", "✨"),
                        startMs = obj.optLong("startMs", 0L),
                        durationMs = obj.optLong("durationMs", 3000L),
                        posXRatio = obj.optDouble("posXRatio", 0.5).toFloat(),
                        posYRatio = obj.optDouble("posYRatio", 0.45).toFloat(),
                        scale = obj.optDouble("scale", 1.0).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        keyframes = parseKeyframes(obj.optString("keyframes"))
                    )
                )
            }
        } catch (_: Exception) {}

        val shapesList = mutableListOf<com.example.model.ShapeOverlay>()
        try {
            val arr = JSONArray(shapeOverlaysJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                shapesList.add(
                    com.example.model.ShapeOverlay(
                        id = obj.optString("id"),
                        type = runCatching { com.example.model.ShapeType.valueOf(obj.optString("type")) }.getOrDefault(com.example.model.ShapeType.ROUNDED_RECT),
                        startMs = obj.optLong("startMs", 0L),
                        durationMs = obj.optLong("durationMs", 3000L),
                        posXRatio = obj.optDouble("posXRatio", 0.5).toFloat(),
                        posYRatio = obj.optDouble("posYRatio", 0.5).toFloat(),
                        widthRatio = obj.optDouble("widthRatio", 0.35).toFloat(),
                        heightRatio = obj.optDouble("heightRatio", 0.2).toFloat(),
                        fillColorHex = obj.optString("fillColorHex", "#00E5BC"),
                        strokeColorHex = obj.optString("strokeColorHex", "#FFFFFF"),
                        strokeWidth = obj.optDouble("strokeWidth", 3.0).toFloat(),
                        opacity = obj.optDouble("opacity", 0.85).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        keyframes = parseKeyframes(obj.optString("keyframes"))
                    )
                )
            }
        } catch (_: Exception) {}

        val audiosList = mutableListOf<AudioTrack>()
        try {
            val arr = JSONArray(audioTracksJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val beatsList = mutableListOf<Long>()
                val beatsArr = obj.optJSONArray("beatMarkersMs")
                if (beatsArr != null) {
                    for (b in 0 until beatsArr.length()) {
                        beatsList.add(beatsArr.optLong(b))
                    }
                }
                audiosList.add(
                    AudioTrack(
                        id = obj.optString("id"),
                        uri = obj.optString("uri"),
                        title = obj.optString("title", "Audio"),
                        startMs = obj.optLong("startMs", 0L),
                        durationMs = obj.optLong("durationMs", 10000L),
                        volume = obj.optDouble("volume", 1.0).toFloat(),
                        fadeInMs = obj.optLong("fadeInMs", 500L),
                        fadeOutMs = obj.optLong("fadeOutMs", 500L),
                        isMuted = obj.optBoolean("isMuted", false),
                        beatMarkersMs = beatsList
                    )
                )
            }
        } catch (_: Exception) {}

        var parsedAdjustments = ColorAdjustments()
        val projectBeats = mutableListOf<Long>()
        try {
            val obj = JSONObject(adjustmentsJson)
            val pBeatsArr = obj.optJSONArray("beatMarkersMs")
            if (pBeatsArr != null) {
                for (b in 0 until pBeatsArr.length()) {
                    projectBeats.add(pBeatsArr.optLong(b))
                }
            }

            val wheelsObj = obj.optJSONObject("wheels")
            val wheels = if (wheelsObj != null) {
                com.example.model.ColorWheelsData(
                    lift = com.example.model.ColorWheelTonal(
                        hueDegrees = wheelsObj.optDouble("liftHue", 0.0).toFloat(),
                        saturation = wheelsObj.optDouble("liftSat", 0.0).toFloat(),
                        luminance = wheelsObj.optDouble("liftLum", 0.0).toFloat()
                    ),
                    gamma = com.example.model.ColorWheelTonal(
                        hueDegrees = wheelsObj.optDouble("gammaHue", 0.0).toFloat(),
                        saturation = wheelsObj.optDouble("gammaSat", 0.0).toFloat(),
                        luminance = wheelsObj.optDouble("gammaLum", 0.0).toFloat()
                    ),
                    gain = com.example.model.ColorWheelTonal(
                        hueDegrees = wheelsObj.optDouble("gainHue", 0.0).toFloat(),
                        saturation = wheelsObj.optDouble("gainSat", 0.0).toFloat(),
                        luminance = wheelsObj.optDouble("gainLum", 0.0).toFloat()
                    ),
                    offset = com.example.model.ColorWheelTonal(
                        hueDegrees = wheelsObj.optDouble("offsetHue", 0.0).toFloat(),
                        saturation = wheelsObj.optDouble("offsetSat", 0.0).toFloat(),
                        luminance = wheelsObj.optDouble("offsetLum", 0.0).toFloat()
                    )
                )
            } else com.example.model.ColorWheelsData()

            parsedAdjustments = parseColorAdjustments(obj)

            val effArr = obj.optJSONArray("effects")
            if (effArr != null) {
                for (i in 0 until effArr.length()) {
                    val effObj = effArr.getJSONObject(i)
                    val typeId = effObj.optString("type")
                    val effectType = com.example.model.EffectType.values().firstOrNull { it.id == typeId }
                        ?: com.example.model.EffectType.GAUSSIAN_BLUR
                    effectsList.add(
                        com.example.model.VideoEffect(
                            id = effObj.optString("id"),
                            type = effectType,
                            name = effObj.optString("name", effectType.displayName),
                            startMs = effObj.optLong("startMs", 0L),
                            durationMs = effObj.optLong("durationMs", 3000L),
                            intensity = effObj.optDouble("intensity", 0.8).toFloat(),
                            isEnabled = effObj.optBoolean("isEnabled", true),
                            targetClipId = effObj.optString("targetClipId").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        return EditingProject(
            id = id,
            title = title,
            type = runCatching { ProjectType.valueOf(type) }.getOrDefault(ProjectType.VIDEO),
            aspectRatio = runCatching { AspectRatioMode.valueOf(aspectRatio) }.getOrDefault(AspectRatioMode.RATIO_9_16),
            createdAt = createdAt,
            updatedAt = updatedAt,
            thumbnailUri = thumbnailUri,
            clips = clipsList,
            textOverlays = textsList,
            stickerOverlays = stickersList,
            shapeOverlays = shapesList,
            audioTracks = audiosList,
            effects = effectsList,
            beatMarkersMs = projectBeats,
            adjustments = parsedAdjustments
        )
    }
}
