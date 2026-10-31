package com.example.filters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.model.ColorAdjustments
import com.example.model.FilterPreset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object SazfloyColorEngine {

    val IDENTITY_MATRIX = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    val PRESETS = listOf(
        FilterPreset(
            id = "none",
            name = "Original",
            category = "Normal",
            colorMatrix = IDENTITY_MATRIX
        ),
        FilterPreset(
            id = "natural",
            name = "SAZFLOY Natural",
            category = "Daily",
            colorMatrix = floatArrayOf(
                1.05f, 0f, 0f, 0f, 3f,
                0f, 1.08f, 0f, 0f, 4f,
                0f, 0f, 1.02f, 0f, 2f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(contrast = 8f, saturation = 12f)
        ),
        FilterPreset(
            id = "cinematic",
            name = "SAZFLOY Cinematic",
            category = "Cinema",
            colorMatrix = floatArrayOf(
                1.15f, 0.05f, -0.05f, 0f, -5f,
                0f, 1.12f, 0.05f, 0f, 2f,
                -0.1f, -0.05f, 1.25f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(contrast = 18f, saturation = -5f, vignette = 25f)
        ),
        FilterPreset(
            id = "film",
            name = "SAZFLOY Film",
            category = "Analog",
            colorMatrix = floatArrayOf(
                1.10f, 0.08f, 0.02f, 0f, 8f,
                0.04f, 1.02f, 0.04f, 0f, 4f,
                -0.08f, 0.05f, 0.92f, 0f, -6f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(contrast = 12f, highlights = -10f, shadows = 15f)
        ),
        FilterPreset(
            id = "portrait",
            name = "SAZFLOY Portrait",
            category = "Beauty",
            colorMatrix = floatArrayOf(
                1.12f, 0.02f, 0.02f, 0f, 10f,
                0.01f, 1.06f, 0.02f, 0f, 6f,
                0.01f, 0.02f, 1.04f, 0f, 4f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(brightness = 5f, saturation = 6f)
        ),
        FilterPreset(
            id = "warm",
            name = "SAZFLOY Warm",
            category = "Tone",
            colorMatrix = floatArrayOf(
                1.22f, 0.04f, 0f, 0f, 12f,
                0.02f, 1.08f, 0f, 0f, 5f,
                -0.08f, -0.05f, 0.88f, 0f, -8f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(temperature = 35f)
        ),
        FilterPreset(
            id = "cool",
            name = "SAZFLOY Cool",
            category = "Tone",
            colorMatrix = floatArrayOf(
                0.88f, 0f, 0.05f, 0f, -8f,
                0f, 1.02f, 0.08f, 0f, 2f,
                0.05f, 0.08f, 1.24f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(temperature = -35f)
        ),
        FilterPreset(
            id = "vintage",
            name = "SAZFLOY Vintage",
            category = "Retro",
            colorMatrix = floatArrayOf(
                0.90f, 0.15f, 0.05f, 0f, 18f,
                0.08f, 0.85f, 0.10f, 0f, 12f,
                0.05f, 0.08f, 0.70f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(vignette = 35f, contrast = 10f)
        ),
        FilterPreset(
            id = "night",
            name = "SAZFLOY Night",
            category = "Mood",
            colorMatrix = floatArrayOf(
                0.82f, 0.02f, 0.12f, 0f, -10f,
                0.01f, 0.95f, 0.10f, 0f, -6f,
                0.04f, 0.12f, 1.25f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(contrast = 20f, highlights = 15f)
        ),
        FilterPreset(
            id = "landscape",
            name = "SAZFLOY Landscape",
            category = "Nature",
            colorMatrix = floatArrayOf(
                1.14f, -0.05f, 0.02f, 0f, 2f,
                -0.02f, 1.24f, -0.04f, 0f, 4f,
                0.02f, 0.05f, 1.18f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(saturation = 25f, contrast = 14f)
        ),
        FilterPreset(
            id = "bw",
            name = "SAZFLOY Black & White",
            category = "Mono",
            colorMatrix = floatArrayOf(
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(contrast = 20f)
        ),
        FilterPreset(
            id = "sepia",
            name = "Sazfloy Sepia",
            category = "Retro",
            colorMatrix = floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(contrast = 15f)
        ),
        FilterPreset(
            id = "grayscale",
            name = "Sazfloy Grayscale",
            category = "Mono",
            colorMatrix = floatArrayOf(
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            defaultAdjustments = ColorAdjustments(contrast = 18f)
        )
    )

    fun applyFastBlur(src: Bitmap, radius: Int): Bitmap {
        if (radius <= 0) return src
        val w = src.width
        val h = src.height
        val scale = (1f / (radius.coerceAtMost(25) * 0.4f + 1f)).coerceIn(0.08f, 1f)
        val smallW = (w * scale).toInt().coerceAtLeast(16)
        val smallH = (h * scale).toInt().coerceAtLeast(16)
        val small = Bitmap.createScaledBitmap(src, smallW, smallH, true)
        return Bitmap.createScaledBitmap(small, w, h, true)
    }

    fun getPresetById(id: String): FilterPreset {
        return PRESETS.firstOrNull { it.id == id } ?: PRESETS.first()
    }

    /**
     * Builds a unified 4x5 float array suitable for Compose ColorMatrix or Android ColorMatrix.
     */
    fun createCompositeMatrix(adjustments: ColorAdjustments): FloatArray {
        val preset = getPresetById(adjustments.filterPresetId)
        val intensity = adjustments.filterIntensity.coerceIn(0f, 1f)

        // 1. Blend preset with identity matrix based on intensity
        val blendedPreset = FloatArray(20)
        for (i in 0 until 20) {
            blendedPreset[i] = IDENTITY_MATRIX[i] + (preset.colorMatrix[i] - IDENTITY_MATRIX[i]) * intensity
        }

        val baseMatrix = ColorMatrix(blendedPreset)

        // 2. Brightness + Exposure (translates RGB channels)
        val brightnessShift = (adjustments.brightness * 1.2f) + (adjustments.exposure * 1.5f)
        if (brightnessShift != 0f) {
            val brightMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, brightnessShift,
                    0f, 1f, 0f, 0f, brightnessShift,
                    0f, 0f, 1f, 0f, brightnessShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            baseMatrix.postConcat(brightMatrix)
        }

        // 3. Contrast
        val contrastVal = adjustments.contrast
        if (contrastVal != 0f) {
            val scale = (contrastVal + 100f) / 100f
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            baseMatrix.postConcat(contrastMatrix)
        }

        // 4. Saturation
        val satFactor = ((adjustments.saturation + 100f) / 100f).coerceAtLeast(0f)
        if (satFactor != 1f) {
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(satFactor)
            baseMatrix.postConcat(satMatrix)
        }

        // 5. Temperature (Warm vs Cool) and Tint (Green vs Magenta)
        val temp = adjustments.temperature / 100f
        val tint = adjustments.tint / 100f
        if (temp != 0f || tint != 0f) {
            val rMult = 1f + (temp * 0.25f)
            val gMult = 1f - (tint * 0.15f)
            val bMult = 1f - (temp * 0.25f) + (tint * 0.15f)
            val tempTintMatrix = ColorMatrix(
                floatArrayOf(
                    rMult, 0f, 0f, 0f, 0f,
                    0f, gMult, 0f, 0f, 0f,
                    0f, 0f, bMult, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            baseMatrix.postConcat(tempTintMatrix)
        }

        // 6. DaVinci Resolve-style 4-Way Color Wheels (Lift, Gamma, Gain, Offset)
        val cw = adjustments.colorWheels
        if (!cw.isDefault) {
            // Lift: Shifts shadows/blacks
            val liftRad = Math.toRadians(cw.lift.hueDegrees.toDouble())
            val liftR = (cos(liftRad) * cw.lift.saturation * 35.0).toFloat() + (cw.lift.luminance * 40f)
            val liftB = (sin(liftRad) * cw.lift.saturation * 35.0).toFloat() + (cw.lift.luminance * 40f)
            val liftG = (-0.5 * (cos(liftRad) + sin(liftRad)) * cw.lift.saturation * 35.0).toFloat() + (cw.lift.luminance * 40f)

            // Gain: Multiplies highlights
            val gainRad = Math.toRadians(cw.gain.hueDegrees.toDouble())
            val gainR = (1f + (cos(gainRad) * cw.gain.saturation * 0.45).toFloat() + (cw.gain.luminance * 0.4f)).coerceAtLeast(0.05f)
            val gainB = (1f + (sin(gainRad) * cw.gain.saturation * 0.45).toFloat() + (cw.gain.luminance * 0.4f)).coerceAtLeast(0.05f)
            val gainG = (1f + (-0.5 * (cos(gainRad) + sin(gainRad)) * cw.gain.saturation * 0.45).toFloat() + (cw.gain.luminance * 0.4f)).coerceAtLeast(0.05f)

            // Gamma: Bends midtones
            val gammaRad = Math.toRadians(cw.gamma.hueDegrees.toDouble())
            val gammaR = (1f + (cos(gammaRad) * cw.gamma.saturation * 0.35).toFloat() + (cw.gamma.luminance * 0.3f)).coerceAtLeast(0.05f)
            val gammaB = (1f + (sin(gammaRad) * cw.gamma.saturation * 0.35).toFloat() + (cw.gamma.luminance * 0.3f)).coerceAtLeast(0.05f)
            val gammaG = (1f + (-0.5 * (cos(gammaRad) + sin(gammaRad)) * cw.gamma.saturation * 0.35).toFloat() + (cw.gamma.luminance * 0.3f)).coerceAtLeast(0.05f)

            // Offset: Global master shift
            val offRad = Math.toRadians(cw.offset.hueDegrees.toDouble())
            val offR = (cos(offRad) * cw.offset.saturation * 25.0).toFloat() + (cw.offset.luminance * 30f)
            val offB = (sin(offRad) * cw.offset.saturation * 25.0).toFloat() + (cw.offset.luminance * 30f)
            val offG = (-0.5 * (cos(offRad) + sin(offRad)) * cw.offset.saturation * 25.0).toFloat() + (cw.offset.luminance * 30f)

            val wheelsMatrix = ColorMatrix(
                floatArrayOf(
                    gainR * gammaR, 0f, 0f, 0f, liftR + offR,
                    0f, gainG * gammaG, 0f, 0f, liftG + offG,
                    0f, 0f, gainB * gammaB, 0f, liftB + offB,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            baseMatrix.postConcat(wheelsMatrix)
        }

        return baseMatrix.array
    }

    /**
     * Applies the composite matrix, vignette, 3D LUT, and optional rotation/flip to a Bitmap.
     */
    fun processBitmap(
        source: Bitmap,
        adjustments: ColorAdjustments,
        rotationDegrees: Int = 0,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false
    ): Bitmap {
        val matrix = android.graphics.Matrix()

        if (flipHorizontal || flipVertical) {
            val sx = if (flipHorizontal) -1f else 1f
            val sy = if (flipVertical) -1f else 1f
            matrix.postScale(sx, sy, source.width / 2f, source.height / 2f)
        }
        if (rotationDegrees % 360 != 0) {
            matrix.postRotate(rotationDegrees.toFloat(), source.width / 2f, source.height / 2f)
        }

        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val colorArray = createCompositeMatrix(adjustments)
        paint.colorFilter = ColorMatrixColorFilter(colorArray)

        canvas.drawBitmap(source, matrix, paint)

        // Apply Vignette if enabled
        if (adjustments.vignette > 0f) {
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val radius = (source.width.coerceAtLeast(source.height) * 0.75f)
            val cx = source.width / 2f
            val cy = source.height / 2f
            val alpha = (adjustments.vignette / 100f * 0.85f * 255).toInt().coerceIn(0, 255)
            val gradient = RadialGradient(
                cx, cy, radius,
                intArrayOf(0x00000000, 0x00000000, (alpha shl 24) or 0x000000),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
            vignettePaint.shader = gradient
            canvas.drawRect(0f, 0f, source.width.toFloat(), source.height.toFloat(), vignettePaint)
        }

        // Apply 3D LUT if selected
        val lutResult = if (adjustments.activeLutId != "none" && adjustments.activeLutId.isNotBlank()) {
            val lut = LutEngine.getLutById(adjustments.activeLutId)
            if (lut != null) {
                LutEngine.applyLutToBitmap(output, lut, adjustments.lutIntensity)
            } else {
                output
            }
        } else {
            output
        }

        val blurred = if (adjustments.blurRadius > 0f) {
            applyFastBlur(lutResult, adjustments.blurRadius.toInt())
        } else {
            lutResult
        }

        return blurred
    }

    /**
     * Applies a professional multimedia video effect to a frame Bitmap.
     */
    fun applyVideoEffect(source: Bitmap, effect: com.example.model.VideoEffect): Bitmap {
        if (!effect.isEnabled || effect.intensity <= 0.01f) return source

        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val intensity = effect.intensity.coerceIn(0f, 1f)

        when (effect.type) {
            com.example.model.EffectType.GAUSSIAN_BLUR -> {
                val radius = (intensity * 22f).toInt().coerceAtLeast(1)
                return applyFastBlur(source, radius)
            }

            com.example.model.EffectType.RADIAL_BLUR -> {
                // Radial zoom blur simulation by layered scaled passes
                canvas.drawBitmap(source, 0f, 0f, null)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    alpha = (intensity * 80).toInt().coerceIn(10, 120)
                }
                val scale1 = 1f + (0.04f * intensity)
                val scale2 = 1f + (0.08f * intensity)
                canvas.save()
                canvas.scale(scale1, scale1, w / 2f, h / 2f)
                canvas.drawBitmap(source, 0f, 0f, paint)
                canvas.restore()
                canvas.save()
                canvas.scale(scale2, scale2, w / 2f, h / 2f)
                canvas.drawBitmap(source, 0f, 0f, paint)
                canvas.restore()
            }

            com.example.model.EffectType.SHARPEN -> {
                val k = intensity * 0.35f
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1f + 2f * k, -k, -k, 0f, 0f,
                        -k, 1f + 2f * k, -k, 0f, 0f,
                        -k, -k, 1f + 2f * k, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            com.example.model.EffectType.RGB_SPLIT -> {
                canvas.drawBitmap(source, 0f, 0f, null)
                val shift = (intensity * 18f).toInt().coerceAtLeast(2)
                // Red channel shifted left
                val redPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    alpha = (intensity * 140).toInt().coerceIn(20, 200)
                    colorFilter = ColorMatrixColorFilter(
                        floatArrayOf(
                            1f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                }
                canvas.drawBitmap(source, -shift.toFloat(), 0f, redPaint)

                // Cyan/Blue channel shifted right
                val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    alpha = (intensity * 140).toInt().coerceIn(20, 200)
                    colorFilter = ColorMatrixColorFilter(
                        floatArrayOf(
                            0f, 0f, 0f, 0f, 0f,
                            0f, 1f, 0f, 0f, 0f,
                            0f, 0f, 1f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                }
                canvas.drawBitmap(source, shift.toFloat(), 0f, bluePaint)
            }

            com.example.model.EffectType.DIGITAL_NOISE -> {
                canvas.drawBitmap(source, 0f, 0f, null)
                // Scanlines & TV noise
                val linePaint = Paint().apply {
                    color = 0x5500E5BC.toInt()
                    strokeWidth = 2f
                }
                val step = (12 - (intensity * 6).toInt()).coerceAtLeast(4)
                for (y in 0 until h step step) {
                    canvas.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), linePaint)
                }
            }

            com.example.model.EffectType.VHS_TAPE -> {
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1.12f, 0.05f, 0f, 0f, 10f * intensity,
                        0f, 1.05f, 0.02f, 0f, 4f * intensity,
                        -0.05f, 0f, 0.95f, 0f, -6f * intensity,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)

                // VHS tracking horizontal lines
                val vhsLinePaint = Paint().apply {
                    color = 0x44FFFFFF.toInt()
                    strokeWidth = 3f
                }
                val trackY1 = (h * 0.15f)
                val trackY2 = (h * 0.85f)
                canvas.drawLine(0f, trackY1, w.toFloat(), trackY1, vhsLinePaint)
                canvas.drawLine(0f, trackY2, w.toFloat(), trackY2, vhsLinePaint)
            }

            com.example.model.EffectType.MONOCHROME_GRAIN -> {
                val matrix = ColorMatrix().apply {
                    setSaturation(1f - intensity)
                }
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)

                // Fine film grain
                val grainPaint = Paint().apply {
                    color = 0x22FFFFFF.toInt()
                }
                val count = (w * h / 200 * intensity).toInt().coerceIn(100, 3000)
                val rand = java.util.Random(42L)
                for (i in 0 until count) {
                    val rx = rand.nextFloat() * w
                    val ry = rand.nextFloat() * h
                    canvas.drawPoint(rx, ry, grainPaint)
                }
            }

            com.example.model.EffectType.ANAMORPHIC_TEAL -> {
                // Teal & Orange Hollywood matrix
                val t = intensity
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1f + (0.25f * t), 0f, 0f, 0f, 15f * t,
                        0f, 1f + (0.05f * t), 0f, 0f, 5f * t,
                        -0.1f * t, 0f, 1f + (0.2f * t), 0f, 18f * t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            com.example.model.EffectType.BLEACH_BYPASS -> {
                // High contrast desaturated
                val t = intensity
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1f + (0.35f * t), 0f, 0f, 0f, -10f * t,
                        0f, 1f + (0.35f * t), 0f, 0f, -10f * t,
                        0f, 0f, 1f + (0.35f * t), 0f, -10f * t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val satMatrix = ColorMatrix().apply { setSaturation(1f - (0.5f * t)) }
                matrix.postConcat(satMatrix)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            com.example.model.EffectType.NEON_CYAN -> {
                val t = intensity
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1f + (0.1f * t), 0f, 0f, 0f, 5f * t,
                        0f, 1f + (0.35f * t), 0f, 0f, 15f * t,
                        0f, 0f, 1f + (0.45f * t), 0f, 25f * t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            com.example.model.EffectType.GOLDEN_HOUR -> {
                val t = intensity
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1f + (0.35f * t), 0.05f * t, 0f, 0f, 25f * t,
                        0f, 1f + (0.2f * t), 0f, 0f, 12f * t,
                        -0.15f * t, -0.1f * t, 1f - (0.25f * t), 0f, -15f * t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            com.example.model.EffectType.LIGHT_LEAK -> {
                canvas.drawBitmap(source, 0f, 0f, null)
                val leakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alpha = (intensity * 180).toInt().coerceIn(30, 220)
                    shader = RadialGradient(
                        0f, 0f, w * 0.75f,
                        intArrayOf((alpha shl 24) or 0xFF8A00, (alpha / 2 shl 24) or 0xFF2D75, 0x00000000),
                        floatArrayOf(0f, 0.45f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), leakPaint)
            }

            com.example.model.EffectType.LENS_FLARE -> {
                canvas.drawBitmap(source, 0f, 0f, null)
                // Anamorphic horizontal streak
                val flarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alpha = (intensity * 190).toInt().coerceIn(20, 230)
                    shader = LinearGradient(
                        0f, h * 0.5f, w.toFloat(), h * 0.5f,
                        intArrayOf(0x0000E5BC, (alpha shl 24) or 0x00E5BC, (alpha shl 24) or 0xFFFFFF, (alpha shl 24) or 0x00E5BC, 0x0000E5BC),
                        floatArrayOf(0f, 0.35f, 0.5f, 0.65f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, (h * 0.5f) - 10f, w.toFloat(), (h * 0.5f) + 10f, flarePaint)
            }

            com.example.model.EffectType.MIRROR_SPLIT -> {
                // Mirror left half onto right half
                canvas.drawBitmap(source, 0f, 0f, null)
                canvas.save()
                canvas.clipRect(w / 2f, 0f, w.toFloat(), h.toFloat())
                canvas.translate(w.toFloat(), 0f)
                canvas.scale(-1f, 1f)
                canvas.drawBitmap(source, 0f, 0f, null)
                canvas.restore()
            }

            com.example.model.EffectType.FISHEYE -> {
                canvas.drawBitmap(source, 0f, 0f, null)
                // Vignette curvature
                val fishPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alpha = (intensity * 210).toInt().coerceIn(40, 240)
                    shader = RadialGradient(
                        w / 2f, h / 2f, w * 0.65f,
                        intArrayOf(0x00000000, 0x00000000, (alpha shl 24) or 0x000000),
                        floatArrayOf(0f, 0.6f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), fishPaint)
            }

            com.example.model.EffectType.SHAKE -> {
                val dx = (Math.sin(System.currentTimeMillis() / 60.0) * 16.0 * intensity).toFloat()
                val dy = (Math.cos(System.currentTimeMillis() / 80.0) * 12.0 * intensity).toFloat()
                canvas.drawBitmap(source, dx, dy, null)
            }

            com.example.model.EffectType.ZOOM_PULSE -> {
                val scale = 1f + (0.12f * intensity)
                canvas.save()
                canvas.scale(scale, scale, w / 2f, h / 2f)
                canvas.drawBitmap(source, 0f, 0f, null)
                canvas.restore()
            }

            com.example.model.EffectType.SEPIA_VINTAGE -> {
                val t = intensity
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1f - (0.607f * t) + (0.393f * t), 0.769f * t, 0.189f * t, 0f, 0f,
                        0.349f * t, 1f - (0.314f * t) + (0.686f * t), 0.168f * t, 0f, 0f,
                        0.272f * t, 0.534f * t, 1f - (0.869f * t) + (0.131f * t), 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            com.example.model.EffectType.FADED_POLAROID -> {
                val t = intensity
                val matrix = ColorMatrix(
                    floatArrayOf(
                        1.08f, 0.05f, 0.05f, 0f, 20f * t,
                        0.02f, 1.02f, 0.04f, 0f, 15f * t,
                        0.05f, 0.05f, 0.88f, 0f, 25f * t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }

            com.example.model.EffectType.VIGNETTE_FOCUS -> {
                canvas.drawBitmap(source, 0f, 0f, null)
                val vigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val alpha = (intensity * 230).toInt().coerceIn(30, 245)
                    shader = RadialGradient(
                        w / 2f, h / 2f, (w.coerceAtLeast(h) * 0.65f),
                        intArrayOf(0x00000000, 0x00000000, (alpha shl 24) or 0x000000),
                        floatArrayOf(0f, 0.4f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vigPaint)
            }
        }

        return output
    }
}

