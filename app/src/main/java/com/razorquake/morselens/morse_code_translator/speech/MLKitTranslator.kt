package com.razorquake.morselens.morse_code_translator.speech

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MLKitTranslator {
    private val translators = mutableMapOf<String, Translator>()

    suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String {
        return withContext(Dispatchers.IO) {
            val translatorKey = "$sourceLanguage-$targetLanguage"
            val translator = translators.getOrPut(translatorKey) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                Translation.getClient(options)
            }
            try {
                suspendCoroutine { continuation ->
                    var conditions = DownloadConditions.Builder().build()
                    translator.downloadModelIfNeeded(conditions)
                        .addOnSuccessListener{
                            translator.translate(text)
                                .addOnSuccessListener { result ->
                                    continuation.resume(result)
                                }
                                .addOnFailureListener { exception ->
                                    continuation.resumeWithException(exception)
                                }
                        }.addOnFailureListener{ exception ->
                            continuation.resumeWithException(exception)
                        }
                }
            } catch (e: Exception) {
                throw TranslationException("Translation failed: ${e.message}")
            }
        }
    }
    fun cleanup(){
        translators.values.forEach { it.close() }
        translators.clear()
    }
}
class TranslationException(message: String): Exception(message)