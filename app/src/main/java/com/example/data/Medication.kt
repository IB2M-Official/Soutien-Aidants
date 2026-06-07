package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente un médicament ou traitement à suivre par le proche.
 */
@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val frequency: String, // ex: "Matin", "Midi", "Soir", "Au coucher"
    val isTakenToday: Boolean = false,
    val lastTakenTimestamp: Long = 0L // Optionnel pour le suivi du reset journalier
)
