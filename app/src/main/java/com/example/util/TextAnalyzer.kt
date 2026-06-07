package com.example.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Service d'analyse d'image utilisant Google ML Kit Text Recognition.
 * Effectue l'OCR de manière 100% locale, confidentielle et gratuite sur l'appareil.
 */
class TextAnalyzer(private val context: Context) {

    // Initialisation du client OCR d'extraction de texte (Alphabet latin standard)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extrait le texte d'un fichier image donné par son Uri.
     * Cette fonction suspendue s'intègre parfaitement avec les coroutines Kotlin.
     */
    suspend fun extractTextFromImageUri(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Renvoie le texte extrait complet
                    val resultText = visionText.text
                    if (resultText.isBlank()) {
                        continuation.resume("Aucun texte détecté dans le document.")
                    } else {
                        continuation.resume(resultText)
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume("Échec de l'OCR local : ${exception.localizedMessage}")
                }
        } catch (e: Exception) {
            continuation.resume("Erreur de chargement de l'image : ${e.localizedMessage}")
        }
    }
}
