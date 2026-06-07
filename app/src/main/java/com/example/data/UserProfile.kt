package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stocke le profil utilisateur de l'aidant pour l'inscription locale/connexion sécurisée,
 * ainsi que les droits et les paramètres de partage virtuel avec les proches.
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Long = 1, // Une seule ligne pour les infos de l'appareil
    val firstName: String,
    val lastName: String,
    val phone: String,
    val roleInCare: String, // ex: Conjoint, Fils, Fille, Auxiliaire de vie...
    val pinCode: String = "", // Code PIN local pour la connexion sécurisée (ex: "1234")
    val isRegistered: Boolean = false,
    
    // Suivi de la personne aidée
    val patientName: String = "",
    
    // Configuration de partage avec un proche
    val sharedContactName: String = "",
    val sharedContactPhone: String = "",
    val sharingRightsMode: String = "Consultation", // "Consultation", "Modification", "Prescription"
    val isSharingActive: Boolean = false
)
