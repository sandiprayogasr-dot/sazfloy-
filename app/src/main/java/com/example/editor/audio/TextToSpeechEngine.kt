package com.example.editor.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.AudioTrack
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

object TextToSpeechEngine {

    data class VoiceOption(
        val id: String,
        val displayName: String,
        val locale: Locale
    )

    val AVAILABLE_VOICES = listOf(
        VoiceOption("id", "Bahasa Indonesia (ID)", Locale("id", "ID")),
        VoiceOption("en_us", "English (US)", Locale.US),
        VoiceOption("en_uk", "English (UK)", Locale.UK),
        VoiceOption("ja", "Japanese (JP)", Locale.JAPAN),
        VoiceOption("ko", "Korean (KR)", Locale.KOREA),
        VoiceOption("es", "Spanish (ES)", Locale("es", "ES")),
        VoiceOption("fr", "French (FR)", Locale.FRANCE)
    )

    suspend fun synthesizeToAudioTrack(
        context: Context,
        text: String,
        voiceOption: VoiceOption,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f,
        startMs: Long = 0L
    ): Result<AudioTrack> = suspendCancellableCoroutine { continuation ->
        val outputDir = File(context.filesDir, "sazfloy_tts").apply { mkdirs() }
        val outputFile = File(outputDir, "tts_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.wav")
        val utteranceId = "sazfloy_tts_${System.currentTimeMillis()}"

        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.let { tts ->
                    tts.language = voiceOption.locale
                    tts.setPitch(pitch.coerceIn(0.5f, 2.0f))
                    tts.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))

                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {}

                        override fun onDone(id: String?) {
                            try {
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(outputFile.absolutePath)
                                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                val durationMs = durStr?.toLongOrNull()?.coerceAtLeast(500L) ?: 3000L
                                retriever.release()

                                val displayTitle = if (text.length > 20) text.take(17) + "..." else text
                                val track = AudioTrack(
                                    id = UUID.randomUUID().toString(),
                                    uri = outputFile.absolutePath,
                                    title = "AI Voice: $displayTitle",
                                    startMs = startMs,
                                    durationMs = durationMs,
                                    volume = 1.0f
                                )
                                tts.shutdown()
                                continuation.resume(Result.success(track))
                            } catch (e: Exception) {
                                tts.shutdown()
                                continuation.resume(Result.failure(e))
                            }
                        }

                        override fun onError(id: String?) {
                            tts.shutdown()
                            continuation.resume(Result.failure(Exception("TTS Synthesis failed on utterance: $id")))
                        }
                    })

                    val params = Bundle()
                    tts.synthesizeToFile(text, params, outputFile, utteranceId)
                }
            } else {
                ttsInstance?.shutdown()
                continuation.resume(Result.failure(Exception("Gagal menginisialisasi TextToSpeech engine (status=$status)")))
            }
        }

        continuation.invokeOnCancellation {
            ttsInstance?.shutdown()
        }
    }
}
