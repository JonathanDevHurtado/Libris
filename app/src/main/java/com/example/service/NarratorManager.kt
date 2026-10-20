package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class LocalVoiceModel(
    val id: String,
    val name: String,
    val localeDisplayName: String,
    val isNetworkRequired: Boolean,
    val quality: String
)

class NarratorManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<LocalVoiceModel>>(emptyList())
    val availableVoices: StateFlow<List<LocalVoiceModel>> = _availableVoices.asStateFlow()

    private val _currentSentenceIndex = MutableStateFlow(0)
    val currentSentenceIndex: StateFlow<Int> = _currentSentenceIndex.asStateFlow()

    private var currentSentences: List<String> = emptyList()
    private var currentActiveIndex = 0

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("NarratorManager", "Error initializing TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale("es", "ES")
            refreshAvailableVoices()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                }

                override fun onDone(utteranceId: String?) {
                    // Next sentence if available
                    val nextIndex = currentActiveIndex + 1
                    if (nextIndex < currentSentences.size && _isPlaying.value) {
                        currentActiveIndex = nextIndex
                        _currentSentenceIndex.value = nextIndex
                        speakSentenceAtIndex(nextIndex)
                    } else {
                        _isPlaying.value = false
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isPlaying.value = false
                }
            })
        }
    }

    fun refreshAvailableVoices() {
        val ttsInstance = tts ?: return
        try {
            val voices: Set<Voice>? = ttsInstance.voices
            val list = mutableListOf<LocalVoiceModel>()
            if (voices != null) {
                for (v in voices) {
                    // Filter offline / local voices
                    val locale = v.locale
                    val quality = when {
                        v.quality >= Voice.QUALITY_VERY_HIGH -> "Muy Alta"
                        v.quality >= Voice.QUALITY_HIGH -> "Alta"
                        else -> "Estándar"
                    }
                    val isOffline = !v.isNetworkConnectionRequired
                    list.add(
                        LocalVoiceModel(
                            id = v.name,
                            name = cleanVoiceName(v.name),
                            localeDisplayName = "${locale.displayLanguage} (${locale.country})",
                            isNetworkRequired = !isOffline,
                            quality = quality
                        )
                    )
                }
            }
            if (list.isEmpty()) {
                // Fallback default voice entries for device simulation
                list.add(LocalVoiceModel("es_es_local_1", "Español Natural (Local)", "Español (España)", false, "Muy Alta"))
                list.add(LocalVoiceModel("es_mx_local_1", "Español Cálido (Local)", "Español (México)", false, "Alta"))
                list.add(LocalVoiceModel("en_us_local_1", "Narrador Clásico (Inglés)", "English (US)", false, "Alta"))
            }
            _availableVoices.value = list
        } catch (e: Exception) {
            Log.e("NarratorManager", "Could not fetch voices", e)
        }
    }

    private fun cleanVoiceName(raw: String): String {
        return raw.replace("#", " ")
            .replace("_", " ")
            .split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    fun setVoice(voiceId: String) {
        val ttsInstance = tts ?: return
        try {
            val voice = ttsInstance.voices?.firstOrNull { it.name == voiceId }
            if (voice != null) {
                ttsInstance.voice = voice
            }
        } catch (e: Exception) {
            Log.w("NarratorManager", "Error setting voice", e)
        }
    }

    fun setSpeedAndPitch(speed: Float, pitch: Float) {
        tts?.setSpeechRate(speed.coerceIn(0.5f, 2.5f))
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    fun startReading(paragraphs: List<String>, startIndex: Int = 0) {
        val flatSentences = mutableListOf<String>()
        for (p in paragraphs) {
            val sentences = p.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
            if (sentences.isNotEmpty()) {
                flatSentences.addAll(sentences)
            } else if (p.isNotBlank()) {
                flatSentences.add(p)
            }
        }
        if (flatSentences.isEmpty()) return

        currentSentences = flatSentences
        currentActiveIndex = startIndex.coerceIn(0, flatSentences.size - 1)
        _currentSentenceIndex.value = currentActiveIndex
        _isPlaying.value = true

        speakSentenceAtIndex(currentActiveIndex)
    }

    private fun speakSentenceAtIndex(index: Int) {
        if (index !in currentSentences.indices) return
        val text = currentSentences[index]
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_$index")
    }

    fun pauseOrResume() {
        if (_isPlaying.value) {
            tts?.stop()
            _isPlaying.value = false
        } else {
            if (currentSentences.isNotEmpty()) {
                _isPlaying.value = true
                speakSentenceAtIndex(currentActiveIndex)
            }
        }
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
    }

    fun nextSentence() {
        if (currentActiveIndex + 1 < currentSentences.size) {
            currentActiveIndex++
            _currentSentenceIndex.value = currentActiveIndex
            if (_isPlaying.value) {
                speakSentenceAtIndex(currentActiveIndex)
            }
        }
    }

    fun previousSentence() {
        if (currentActiveIndex > 0) {
            currentActiveIndex--
            _currentSentenceIndex.value = currentActiveIndex
            if (_isPlaying.value) {
                speakSentenceAtIndex(currentActiveIndex)
            }
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
