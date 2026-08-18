package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordDurationSeconds = MutableStateFlow(0)
    val recordDurationSeconds: StateFlow<Int> = _recordDurationSeconds.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    fun startRecording(): File? {
        if (_isRecording.value) return null
        return try {
            val audioDir = File(context.filesDir, "audio_notes").apply { mkdirs() }
            val outputFile = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            currentOutputFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _recordDurationSeconds.value = 0

            timerJob = scope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordDurationSeconds.value += 1
                    try {
                        _amplitude.value = mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        _amplitude.value = 0
                    }
                }
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
            null
        }
    }

    fun stopRecording(): File? {
        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false
        _amplitude.value = 0

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentOutputFile
        } catch (e: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
            currentOutputFile
        }
    }

    fun cancelRecording() {
        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false
        _amplitude.value = 0
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentOutputFile?.delete()
            currentOutputFile = null
        } catch (e: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }
}

class AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(0)
    val currentPositionSec: StateFlow<Int> = _currentPositionSec.asStateFlow()

    private val _totalDurationSec = MutableStateFlow(0)
    val totalDurationSec: StateFlow<Int> = _totalDurationSec.asStateFlow()

    fun play(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return

        stop()

        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
            mediaPlayer = player
            _isPlaying.value = true
            val totalSec = (player.duration / 1000).coerceAtLeast(1)
            _totalDurationSec.value = totalSec

            player.setOnCompletionListener {
                stop()
            }

            progressJob = scope.launch {
                while (_isPlaying.value && mediaPlayer != null) {
                    try {
                        val pos = (mediaPlayer?.currentPosition ?: 0) / 1000
                        _currentPositionSec.value = pos
                    } catch (e: Exception) {
                        break
                    }
                    delay(250)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            stop()
        }
    }

    fun resume() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
        } catch (e: Exception) {
            stop()
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPositionSec.value = 0
    }
}

class SpeechToTextHelper(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is not available on this device")
            return
        }

        stopListening()

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network connection required"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Speech recognition error ($error)"
                    }
                    onError(errorMsg)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    if (spoken.isNotEmpty()) {
                        _recognizedText.value = spoken
                        onResult(spoken)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    if (spoken.isNotEmpty()) {
                        _recognizedText.value = spoken
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError("Failed to start speech recognizer: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        }
        speechRecognizer = null
        _isListening.value = false
    }
}
