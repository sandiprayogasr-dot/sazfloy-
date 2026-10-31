package com.example.model

import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

enum class ProjectType {
    VIDEO,
    PHOTO
}

enum class AspectRatioMode(val label: String, val ratio: Float, val subtitle: String) {
    RATIO_9_16("9:16", 9f / 16f, "TikTok / Reels / Shorts"),
    RATIO_16_9("16:9", 16f / 9f, "YouTube / Cinema"),
    RATIO_1_1("1:1", 1f, "Instagram Feed"),
    RATIO_4_5("4:5", 4f / 5f, "Social Portrait"),
    RATIO_4_3("4:3", 4f / 3f, "Classic TV / iPad"),
    RATIO_3_4("3:4", 3f / 4f, "Vertical Social 3:4"),
    RATIO_ORIGINAL("Original", 0f, "Fit Source Media")
}

enum class ScreenOrientationMode(val label: String) {
    AUTO("Auto Rotate"),
    PORTRAIT("Portrait Only"),
    LANDSCAPE("Landscape Only")
}

enum class ClipType {
    VIDEO,
    PHOTO
}

enum class TransitionType(val displayName: String) {
    NONE("None"),
    CROSS_DISSOLVE("Cross Dissolve"),
    DIP_TO_BLACK("Dip to Black"),
    DIP_TO_WHITE("Dip to White"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    PUSH_UP("Push Up"),
    SLIDE_DOWN("Slide Down"),
    ZOOM_IN("Zoom In"),
    ZOOM_OUT("Zoom Out"),
    GLITCH("Glitch VHS"),
    BLUR_DISSOLVE("Blur Dissolve"),
    WHIP_PAN("Whip Pan"),
    WIPE_RIGHT("Wipe Right"),
    SPIN("Spin 360"),
    // Backward compatibility aliases
    FADE("Fade"),
    DISSOLVE("Dissolve"),
    FLASH_WHITE("Flash White")
}

data class ColorWheelTonal(
    val hueDegrees: Float = 0f,      // 0 to 360
    val saturation: Float = 0f,      // 0.0 to 1.0 (tint intensity)
    val luminance: Float = 0f        // -1.0 (darker) to +1.0 (brighter)
)

data class ColorWheelsData(
    val lift: ColorWheelTonal = ColorWheelTonal(),    // Shadows
    val gamma: ColorWheelTonal = ColorWheelTonal(),   // Midtones
    val gain: ColorWheelTonal = ColorWheelTonal(),    // Highlights
    val offset: ColorWheelTonal = ColorWheelTonal()   // Global Master
) {
    val isDefault: Boolean
        get() = lift.saturation == 0f && lift.luminance == 0f &&
                gamma.saturation == 0f && gamma.luminance == 0f &&
                gain.saturation == 0f && gain.luminance == 0f &&
                offset.saturation == 0f && offset.luminance == 0f
}


enum class AnimationCurve(val displayName: String) {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In-Out"),
    BOUNCE("Bounce");

    val label: String get() = displayName
}

data class Keyframe(
    val id: String = UUID.randomUUID().toString(),
    val timeMs: Long,
    val posXRatio: Float = 0.5f,
    val posYRatio: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val curve: AnimationCurve = AnimationCurve.EASE_IN_OUT
)

data class KeyframeState(
    val posXRatio: Float,
    val posYRatio: Float,
    val scale: Float,
    val rotation: Float,
    val opacity: Float
)

fun interpolateKeyframeState(
    keyframes: List<Keyframe>,
    currentTimeMs: Long,
    defaultPosX: Float = 0.5f,
    defaultPosY: Float = 0.5f,
    defaultScale: Float = 1.0f,
    defaultRotation: Float = 0f,
    defaultOpacity: Float = 1.0f
): KeyframeState {
    if (keyframes.isEmpty()) {
        return KeyframeState(defaultPosX, defaultPosY, defaultScale, defaultRotation, defaultOpacity)
    }

    val sorted = keyframes.sortedBy { it.timeMs }
    if (currentTimeMs <= sorted.first().timeMs) {
        val first = sorted.first()
        return KeyframeState(first.posXRatio, first.posYRatio, first.scale, first.rotation, first.opacity)
    }
    if (currentTimeMs >= sorted.last().timeMs) {
        val last = sorted.last()
        return KeyframeState(last.posXRatio, last.posYRatio, last.scale, last.rotation, last.opacity)
    }

    // Find interval [k1, k2]
    var k1 = sorted.first()
    var k2 = sorted.last()
    for (i in 0 until sorted.size - 1) {
        if (currentTimeMs >= sorted[i].timeMs && currentTimeMs <= sorted[i + 1].timeMs) {
            k1 = sorted[i]
            k2 = sorted[i + 1]
            break
        }
    }

    val duration = (k2.timeMs - k1.timeMs).coerceAtLeast(1L).toFloat()
    val linearT = ((currentTimeMs - k1.timeMs).toFloat() / duration).coerceIn(0f, 1f)

    val easedT = when (k1.curve) {
        AnimationCurve.LINEAR -> linearT
        AnimationCurve.EASE_IN -> linearT * linearT
        AnimationCurve.EASE_OUT -> 1f - (1f - linearT) * (1f - linearT)
        AnimationCurve.EASE_IN_OUT -> if (linearT < 0.5f) 2f * linearT * linearT else 1f - (-2f * linearT + 2f).pow(2) / 2f
        AnimationCurve.BOUNCE -> {
            val n1 = 7.5625f
            val d1 = 2.75f
            var t = linearT
            when {
                t < 1f / d1 -> n1 * t * t
                t < 2f / d1 -> { t -= 1.5f / d1; n1 * t * t + 0.75f }
                t < 2.5f / d1 -> { t -= 2.25f / d1; n1 * t * t + 0.9375f }
                else -> { t -= 2.625f / d1; n1 * t * t + 0.984375f }
            }
        }
    }

    return KeyframeState(
        posXRatio = k1.posXRatio + (k2.posXRatio - k1.posXRatio) * easedT,
        posYRatio = k1.posYRatio + (k2.posYRatio - k1.posYRatio) * easedT,
        scale = k1.scale + (k2.scale - k1.scale) * easedT,
        rotation = k1.rotation + (k2.rotation - k1.rotation) * easedT,
        opacity = k1.opacity + (k2.opacity - k1.opacity) * easedT
    )
}

enum class ShapeType(val displayName: String) {
    RECTANGLE("Rectangle"),
    ROUNDED_RECT("Rounded Box"),
    CIRCLE("Circle"),
    STAR("Star Badge"),
    BADGE("Banner")
}

data class ShapeOverlay(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType = ShapeType.ROUNDED_RECT,
    val startMs: Long = 0L,
    val durationMs: Long = 3000L,
    val posXRatio: Float = 0.5f,
    val posYRatio: Float = 0.5f,
    val widthRatio: Float = 0.35f,
    val heightRatio: Float = 0.2f,
    val fillColorHex: String = "#00E5BC",
    val strokeColorHex: String = "#FFFFFF",
    val strokeWidth: Float = 3f,
    val opacity: Float = 0.85f,
    val rotation: Float = 0f,
    val keyframes: List<Keyframe> = emptyList()
)

data class MediaClip(
    val id: String = UUID.randomUUID().toString(),
    val uri: String = "",
    val thumbnailUri: String? = null,
    val name: String = "Clip",
    val type: ClipType = ClipType.VIDEO,
    val durationMs: Long = 3000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val rotationDegrees: Int = 0,
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false,
    val transitionType: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 500L,
    val scale: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val keyframes: List<Keyframe> = emptyList(),
    val adjustments: ColorAdjustments = ColorAdjustments()
) {
    val trimmedDurationMs: Long
        get() = ((trimEndMs - trimStartMs).coerceAtLeast(100L) / speed).toLong()
}

data class TextOverlay(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "Sazfloy Title",
    val startMs: Long = 0L,
    val durationMs: Long = 3000L,
    val posXRatio: Float = 0.5f,
    val posYRatio: Float = 0.75f,
    val fontSizeSp: Float = 24f,
    val fontColorHex: String = "#FFFFFF",
    val fontFamilyName: String = "montserrat",
    val hasBackground: Boolean = false,
    val backgroundColorHex: String = "#00000000",
    val hasOutline: Boolean = true,
    val outlineColorHex: String = "#000000",
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val animationType: String = "Fade",
    val rotation: Float = 0f,
    val textAlign: String = "center",
    val keyframes: List<Keyframe> = emptyList()
)

data class StickerOverlay(
    val id: String = UUID.randomUUID().toString(),
    val emojiOrTag: String = "✨",
    val startMs: Long = 0L,
    val durationMs: Long = 3000L,
    val posXRatio: Float = 0.5f,
    val posYRatio: Float = 0.45f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val keyframes: List<Keyframe> = emptyList()
)

data class AudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val title: String = "Soundtrack",
    val startMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 500L,
    val fadeOutMs: Long = 500L,
    val isMuted: Boolean = false,
    val beatMarkersMs: List<Long> = emptyList()
)

data class ColorAdjustments(
    val brightness: Float = 0f,      // -100 to +100
    val contrast: Float = 0f,        // -100 to +100
    val saturation: Float = 0f,      // -100 to +100
    val temperature: Float = 0f,     // -100 (Cool) to +100 (Warm)
    val tint: Float = 0f,            // -100 (Green) to +100 (Magenta)
    val vignette: Float = 0f,        // 0 to 100
    val sharpness: Float = 0f,       // 0 to 100
    val exposure: Float = 0f,        // -100 to +100
    val highlights: Float = 0f,      // -100 to +100
    val shadows: Float = 0f,         // -100 to +100
    val blurRadius: Float = 0f,      // 0 to 25
    val filterPresetId: String = "none",
    val filterIntensity: Float = 1.0f, // 0.0 to 1.0
    val colorWheels: ColorWheelsData = ColorWheelsData(),
    val activeLutId: String = "none",
    val lutIntensity: Float = 1.0f
) {
    val isDefault: Boolean
        get() = brightness == 0f && contrast == 0f && saturation == 0f &&
                temperature == 0f && tint == 0f && vignette == 0f &&
                sharpness == 0f && exposure == 0f && highlights == 0f &&
                shadows == 0f && blurRadius == 0f && filterPresetId == "none" &&
                colorWheels.isDefault && activeLutId == "none"
}

data class FilterPreset(
    val id: String,
    val name: String,
    val category: String,
    val colorMatrix: FloatArray,
    val defaultAdjustments: ColorAdjustments = ColorAdjustments()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FilterPreset
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

enum class EffectCategory(val displayName: String) {
    BASIC("Basic"),
    BLUR("Blur"),
    GLITCH("Glitch"),
    RETRO("Retro"),
    CINEMATIC("Cinematic"),
    COLOR("Color"),
    LIGHT("Light"),
    DISTORTION("Distortion"),
    MOTION("Motion"),
    VINTAGE("Vintage")
}

enum class EffectType(
    val id: String,
    val displayName: String,
    val category: EffectCategory,
    val description: String
) {
    // Basic
    SHARPEN("sharpen", "Sharpen Focus", EffectCategory.BASIC, "Ketajaman garis & tekstur media"),
    VIGNETTE_FOCUS("vignette_focus", "Vignette Dark", EffectCategory.BASIC, "Fokus tengah dengan pinggiran gelap"),

    // Blur
    GAUSSIAN_BLUR("gaussian_blur", "Gaussian Blur", EffectCategory.BLUR, "Kelembutan optik halus memukau"),
    RADIAL_BLUR("radial_blur", "Radial Zoom Blur", EffectCategory.BLUR, "Efek gerak zoom dramatis dinamis"),

    // Glitch
    RGB_SPLIT("rgb_split", "RGB Split VHS", EffectCategory.GLITCH, "Pemisahan kanal warna chromatic aberration"),
    DIGITAL_NOISE("digital_noise", "Digital Scanlines", EffectCategory.GLITCH, "Scanlines sinyal interferensi TV digital"),

    // Retro
    VHS_TAPE("vhs_tape", "VHS 1980s", EffectCategory.RETRO, "Nuansa pita kaset video analog retro"),
    MONOCHROME_GRAIN("monochrome_grain", "Film Noir B&W", EffectCategory.RETRO, "Hitam putih kontras dengan butiran film"),

    // Cinematic
    ANAMORPHIC_TEAL("anamorphic_teal", "Teal & Orange", EffectCategory.CINEMATIC, "Grading Hollywood blockbuster terkenal"),
    BLEACH_BYPASS("bleach_bypass", "Bleach Bypass", EffectCategory.CINEMATIC, "Kontras tajam dan desaturasi sinematik"),

    // Color
    NEON_CYAN("neon_cyan", "Cyberpunk Neon", EffectCategory.COLOR, "Peningkatan rona cyan & magenta futuristik"),
    GOLDEN_HOUR("golden_hour", "Golden Sunset", EffectCategory.COLOR, "Cahaya keemasan sore hari yang hangat"),

    // Light
    LIGHT_LEAK("light_leak", "Warm Light Leak", EffectCategory.LIGHT, "Bocoran cahaya hangat organik kamera klasik"),
    LENS_FLARE("lens_flare", "Optic Lens Flare", EffectCategory.LIGHT, "Kilauan cahaya lensa kamera bioskop"),

    // Distortion
    MIRROR_SPLIT("mirror_split", "Mirror Split", EffectCategory.DISTORTION, "Pembalikan cermin simetris artistik"),
    FISHEYE("fisheye", "Fisheye Lens", EffectCategory.DISTORTION, "Kelengkungan lensa lebar cembung ekstrem"),

    // Motion
    SHAKE("shake", "Camera Shake", EffectCategory.MOTION, "Getaran kamera genggam realistis"),
    ZOOM_PULSE("zoom_pulse", "Rhythm Pulse", EffectCategory.MOTION, "Denyutan pembesaran ritmis dinamis"),

    // Vintage
    SEPIA_VINTAGE("sepia_vintage", "Sepia 1920", EffectCategory.VINTAGE, "Nuansa nostalgia cokelat antik lawas"),
    FADED_POLAROID("faded_polaroid", "Faded Polaroid", EffectCategory.VINTAGE, "Warna pudar foto instan matte klasik")
}

data class VideoEffect(
    val id: String = UUID.randomUUID().toString(),
    val type: EffectType = EffectType.GAUSSIAN_BLUR,
    val name: String = "",
    val startMs: Long = 0L,
    val durationMs: Long = 3000L,
    val intensity: Float = 0.8f,
    val isEnabled: Boolean = true,
    val targetClipId: String? = null
)

data class EditingProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Project",
    val type: ProjectType = ProjectType.VIDEO,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_9_16,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailUri: String? = null,
    val clips: List<MediaClip> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickerOverlays: List<StickerOverlay> = emptyList(),
    val shapeOverlays: List<ShapeOverlay> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val effects: List<VideoEffect> = emptyList(),
    val beatMarkersMs: List<Long> = emptyList(),
    val adjustments: ColorAdjustments = ColorAdjustments()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.trimmedDurationMs }.coerceAtLeast(0L)
}

data class ExportConfiguration(
    val resolutionName: String = "1080p (Full HD)",
    val width: Int = 1080,
    val height: Int = 1920,
    val fps: Int = 30,
    val quality: String = "Premiere Pro Master",
    val bitrateMbps: Int = 25,
    val format: String = "MP4", // MP4 for video, PNG or JPEG for photo
    val estimatedSizeMb: Float = 35.0f
)

data class TitleTemplate(
    val id: String,
    val name: String,
    val category: String,
    val primaryText: String,
    val secondaryText: String? = null,
    val fontFamilyName: String,
    val fontColorHex: String,
    val secondaryColorHex: String? = null,
    val hasBackdrop: Boolean = false,
    val backdropColorHex: String = "#B3000000",
    val hasOutline: Boolean = true,
    val outlineColorHex: String = "#000000",
    val shapeType: ShapeType? = null,
    val shapeFillColor: String? = null
)

