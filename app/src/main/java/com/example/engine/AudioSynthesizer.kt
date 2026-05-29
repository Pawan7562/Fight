package com.example.engine

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AudioSynthesizer {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            Log.e("AudioSynthesizer", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playSlapSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // High slap splash sound
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                delay(60)
                toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
            } catch (e: Exception) {
                // Ignore audio issues
            }
        }
    }

    fun playThrowSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Sliding launch sound
                toneGen?.startTone(ToneGenerator.TONE_DTMF_D, 120)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playHitSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Impact crunch
                toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 100)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playPowerActivatedSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Ascending space sound
                toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                delay(100)
                toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                delay(100)
                toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playDholBeats() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val beats = listOf(100, 100, 200, 100, 100, 200)
                val tones = listOf(
                    ToneGenerator.TONE_CDMA_PIP,
                    ToneGenerator.TONE_CDMA_PIP,
                    ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                    ToneGenerator.TONE_CDMA_PIP,
                    ToneGenerator.TONE_CDMA_PIP,
                    ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                )
                for (i in beats.indices) {
                    toneGen?.startTone(tones[i], beats[i])
                    delay(beats[i] + 50L)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playScreamSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
