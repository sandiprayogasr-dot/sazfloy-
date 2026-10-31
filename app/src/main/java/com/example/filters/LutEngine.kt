package com.example.filters

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class LutProfile(
    val id: String,
    val name: String,
    val category: String,
    val size: Int,
    val table: FloatArray, // size * size * size * 3
    val isCustom: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LutProfile
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

object LutEngine {

    private val customLuts = mutableListOf<LutProfile>()

    val BUILTIN_LUTS: List<LutProfile> by lazy {
        listOf(
            createProceduralLut("arri_alexa", "ARRI Alexa LogC", "Cinema Standard") { r, g, b ->
                // S-curve with soft highlight compression & film contrast
                val rOut = (1.0f / (1.0f + Math.exp(-6.0 * (r - 0.45)).toFloat()))
                val gOut = (1.0f / (1.0f + Math.exp(-6.0 * (g - 0.45)).toFloat()))
                val bOut = (1.0f / (1.0f + Math.exp(-6.0 * (b - 0.45)).toFloat()))
                Triple(rOut.coerceIn(0f, 1f), (gOut * 0.98f).coerceIn(0f, 1f), (bOut * 0.94f).coerceIn(0f, 1f))
            },
            createProceduralLut("teal_orange", "Teal & Orange", "Hollywood") { r, g, b ->
                // Boost warm tones in highlights, cyan/teal in shadows
                val luma = 0.299f * r + 0.587f * g + 0.114f * b
                val rOut = if (luma > 0.4f) r * 1.15f + 0.05f else r * 0.85f
                val gOut = if (luma > 0.4f) g * 1.02f else g * 1.05f + 0.03f
                val bOut = if (luma > 0.4f) b * 0.80f else b * 1.25f + 0.08f
                Triple(rOut.coerceIn(0f, 1f), gOut.coerceIn(0f, 1f), bOut.coerceIn(0f, 1f))
            },
            createProceduralLut("kodak_2383", "Kodak 2383 Print", "Film Stock") { r, g, b ->
                // Deep blacks, warm mids, rolled highlights
                val rOut = Math.pow(r.toDouble(), 0.92).toFloat() * 1.08f
                val gOut = Math.pow(g.toDouble(), 0.96).toFloat() * 1.02f
                val bOut = Math.pow(b.toDouble(), 1.05).toFloat() * 0.92f
                Triple(rOut.coerceIn(0f, 1f), gOut.coerceIn(0f, 1f), bOut.coerceIn(0f, 1f))
            },
            createProceduralLut("fuji_eterna", "Fuji Eterna 500T", "Vintage Film") { r, g, b ->
                // Soft muted contrast, subtle green-cyan tint in midtones
                val rOut = r * 0.94f + 0.03f
                val gOut = g * 0.98f + 0.02f
                val bOut = b * 0.92f + 0.05f
                Triple(rOut.coerceIn(0f, 1f), gOut.coerceIn(0f, 1f), bOut.coerceIn(0f, 1f))
            },
            createProceduralLut("sony_slog3", "Sony S-Log3 Cine", "Sony Pro") { r, g, b ->
                // Wide dynamic range mapping to vibrant cinema tone
                val rOut = Math.pow(r.toDouble(), 0.85).toFloat() * 1.1f
                val gOut = Math.pow(g.toDouble(), 0.85).toFloat() * 1.05f
                val bOut = Math.pow(b.toDouble(), 0.85).toFloat() * 1.12f
                Triple(rOut.coerceIn(0f, 1f), gOut.coerceIn(0f, 1f), bOut.coerceIn(0f, 1f))
            },
            createProceduralLut("cyberpunk", "Cyberpunk Neon", "Stylized") { r, g, b ->
                // Punchy cyan and magenta split toning with rich darks
                val luma = 0.299f * r + 0.587f * g + 0.114f * b
                val rOut = if (luma > 0.5f) r * 1.25f else r * 0.7f
                val gOut = g * 0.90f
                val bOut = if (luma < 0.5f) b * 1.35f + 0.1f else b * 1.1f
                Triple(rOut.coerceIn(0f, 1f), gOut.coerceIn(0f, 1f), bOut.coerceIn(0f, 1f))
            }
        )
    }

    fun getAllLuts(): List<LutProfile> {
        return BUILTIN_LUTS + customLuts
    }

    fun getLutById(id: String): LutProfile? {
        if (id == "none" || id.isBlank()) return null
        return getAllLuts().firstOrNull { it.id == id }
    }

    fun addCustomLut(lut: LutProfile) {
        customLuts.removeAll { it.id == lut.id }
        customLuts.add(lut)
    }

    private fun createProceduralLut(
        id: String,
        name: String,
        category: String,
        transform: (Float, Float, Float) -> Triple<Float, Float, Float>
    ): LutProfile {
        val size = 17 // Standard 17x17x17 grid (4913 points)
        val table = FloatArray(size * size * size * 3)
        var idx = 0
        for (b in 0 until size) {
            val bIn = b.toFloat() / (size - 1)
            for (g in 0 until size) {
                val gIn = g.toFloat() / (size - 1)
                for (r in 0 until size) {
                    val rIn = r.toFloat() / (size - 1)
                    val (rOut, gOut, bOut) = transform(rIn, gIn, bIn)
                    table[idx++] = rOut
                    table[idx++] = gOut
                    table[idx++] = bOut
                }
            }
        }
        return LutProfile(id, name, category, size, table, isCustom = false)
    }

    /**
     * Parses standard Adobe / DaVinci Resolve .cube 3D LUT format.
     */
    fun parseCube(stream: InputStream, title: String): LutProfile {
        val reader = BufferedReader(InputStreamReader(stream))
        var size = 0
        val dataList = mutableListOf<Float>()

        reader.useLines { lines ->
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#")) continue

                if (line.startsWith("LUT_3D_SIZE")) {
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 2) {
                        size = parts[1].toIntOrNull() ?: 17
                    }
                    continue
                }

                // Check RGB triplet
                val tokens = line.split("\\s+".toRegex())
                if (tokens.size >= 3) {
                    val r = tokens[0].toFloatOrNull()
                    val g = tokens[1].toFloatOrNull()
                    val b = tokens[2].toFloatOrNull()
                    if (r != null && g != null && b != null) {
                        dataList.add(r)
                        dataList.add(g)
                        dataList.add(b)
                    }
                }
            }
        }

        if (size == 0) {
            // Infer cube size from entries count: size^3 * 3 = count
            val points = dataList.size / 3
            val cbrt = Math.cbrt(points.toDouble()).toInt()
            size = if (cbrt * cbrt * cbrt == points) cbrt else 17
        }

        val table = dataList.toFloatArray()
        val id = "lut_custom_${System.currentTimeMillis()}"
        val sanitizedTitle = if (title.endsWith(".cube", ignoreCase = true)) title.dropLast(5) else title

        val profile = LutProfile(
            id = id,
            name = sanitizedTitle,
            category = "Imported .cube",
            size = size,
            table = table,
            isCustom = true
        )
        addCustomLut(profile)
        return profile
    }

    /**
     * Fast trilinear sampling from 3D LUT table.
     */
    fun sampleLut(
        lut: LutProfile,
        rIn: Float,
        gIn: Float,
        bIn: Float,
        intensity: Float
    ): Triple<Float, Float, Float> {
        val size = lut.size
        val table = lut.table
        val maxCoord = (size - 1).toFloat()

        val rClamped = rIn.coerceIn(0f, 1f) * maxCoord
        val gClamped = gIn.coerceIn(0f, 1f) * maxCoord
        val bClamped = bIn.coerceIn(0f, 1f) * maxCoord

        val r0 = rClamped.toInt().coerceIn(0, size - 1)
        val g0 = gClamped.toInt().coerceIn(0, size - 1)
        val b0 = bClamped.toInt().coerceIn(0, size - 1)

        val r1 = (r0 + 1).coerceAtMost(size - 1)
        val g1 = (g0 + 1).coerceAtMost(size - 1)
        val b1 = (b0 + 1).coerceAtMost(size - 1)

        val rf = rClamped - r0
        val gf = gClamped - g0
        val bf = bClamped - b0

        fun getLutPixel(r: Int, g: Int, b: Int, channel: Int): Float {
            val index = ((b * size + g) * size + r) * 3 + channel
            return if (index in table.indices) table[index] else 0f
        }

        // Interpolate Red channel
        val c00 = getLutPixel(r0, g0, b0, 0) * (1f - rf) + getLutPixel(r1, g0, b0, 0) * rf
        val c01 = getLutPixel(r0, g0, b1, 0) * (1f - rf) + getLutPixel(r1, g0, b1, 0) * rf
        val c10 = getLutPixel(r0, g1, b0, 0) * (1f - rf) + getLutPixel(r1, g1, b0, 0) * rf
        val c11 = getLutPixel(r0, g1, b1, 0) * (1f - rf) + getLutPixel(r1, g1, b1, 0) * rf
        val c0 = c00 * (1f - gf) + c10 * gf
        val c1 = c01 * (1f - gf) + c11 * gf
        val rGraded = c0 * (1f - bf) + c1 * bf

        // Interpolate Green channel
        val g00 = getLutPixel(r0, g0, b0, 1) * (1f - rf) + getLutPixel(r1, g0, b0, 1) * rf
        val g01 = getLutPixel(r0, g0, b1, 1) * (1f - rf) + getLutPixel(r1, g0, b1, 1) * rf
        val g10 = getLutPixel(r0, g1, b0, 1) * (1f - rf) + getLutPixel(r1, g1, b0, 1) * rf
        val g11 = getLutPixel(r0, g1, b1, 1) * (1f - rf) + getLutPixel(r1, g1, b1, 1) * rf
        val g0_val = g00 * (1f - gf) + g10 * gf
        val g1_val = g01 * (1f - gf) + g11 * gf
        val gGraded = g0_val * (1f - bf) + g1_val * bf

        // Interpolate Blue channel
        val b00 = getLutPixel(r0, g0, b0, 2) * (1f - rf) + getLutPixel(r1, g0, b0, 2) * rf
        val b01 = getLutPixel(r0, g0, b1, 2) * (1f - rf) + getLutPixel(r1, g0, b1, 2) * rf
        val b10 = getLutPixel(r0, g1, b0, 2) * (1f - rf) + getLutPixel(r1, g1, b0, 2) * rf
        val b11 = getLutPixel(r0, g1, b1, 2) * (1f - rf) + getLutPixel(r1, g1, b1, 2) * rf
        val b0_val = b00 * (1f - gf) + b10 * gf
        val b1_val = b01 * (1f - gf) + b11 * gf
        val bGraded = b0_val * (1f - bf) + b1_val * bf

        // Blend with original input based on intensity
        val outR = rIn + (rGraded - rIn) * intensity
        val outG = gIn + (gGraded - gIn) * intensity
        val outB = bIn + (bGraded - bIn) * intensity

        return Triple(outR.coerceIn(0f, 1f), outG.coerceIn(0f, 1f), outB.coerceIn(0f, 1f))
    }

    /**
     * Applies LUT transformation directly to Bitmap pixels.
     */
    fun applyLutToBitmap(src: Bitmap, lut: LutProfile, intensity: Float): Bitmap {
        if (intensity <= 0.001f) return src
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        val lutSize = lut.size
        val table = lut.table
        val maxCoord = (lutSize - 1).toFloat()

        for (i in pixels.indices) {
            val c = pixels[i]
            val a = (c shr 24) and 0xFF
            val r = ((c shr 16) and 0xFF) / 255f
            val g = ((c shr 8) and 0xFF) / 255f
            val b = (c and 0xFF) / 255f

            val rIdx = (r * maxCoord).toInt().coerceIn(0, lutSize - 1)
            val gIdx = (g * maxCoord).toInt().coerceIn(0, lutSize - 1)
            val bIdx = (b * maxCoord).toInt().coerceIn(0, lutSize - 1)

            val baseIdx = ((bIdx * lutSize + gIdx) * lutSize + rIdx) * 3
            if (baseIdx + 2 < table.size) {
                val lutR = table[baseIdx]
                val lutG = table[baseIdx + 1]
                val lutB = table[baseIdx + 2]

                val finalR = (r + (lutR - r) * intensity).coerceIn(0f, 1f)
                val finalG = (g + (lutG - g) * intensity).coerceIn(0f, 1f)
                val finalB = (b + (lutB - b) * intensity).coerceIn(0f, 1f)

                pixels[i] = (a shl 24) or
                        ((finalR * 255).toInt() shl 16) or
                        ((finalG * 255).toInt() shl 8) or
                        (finalB * 255).toInt()
            }
        }

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, width, 0, 0, width, height)
        return out
    }
}
