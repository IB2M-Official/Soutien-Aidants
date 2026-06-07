package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente un document médical scanné (ex: ordonnance) avec son texte extrait localement par OCR.
 */
@Entity(tableName = "medical_documents")
data class MedicalDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val localFilePath: String, // Chemin du fichier local image/pdf
    val extractedText: String, // Texte extrait par Google ML Kit pour la recherche textuelle
    val dateAdded: Long = System.currentTimeMillis()
)
