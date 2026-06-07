package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente un symptôme suivi chez le proche (ex: fièvre, confusion, douleur, fatigue...).
 */
@Entity(tableName = "symptom_logs")
data class SymptomLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val severity: Int, // Échelle de 1 à 5
    val timestamp: Long, // Date et heure de l'enregistrement
    val notes: String // Notes ou observations supplémentaires
)
