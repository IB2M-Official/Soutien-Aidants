package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Dépôt centralisant l'accès aux données de l'application (Médicaments et Documents).
 * Respecte l'architecture recommandée par Android.
 */
class CareRepository(private val careDao: CareDao) {

    // --- Actions sur les Médicaments ---
    val allMedications: Flow<List<Medication>> = careDao.getAllMedications()

    suspend fun insertMedication(medication: Medication): Long {
        return careDao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: Medication) {
        careDao.updateMedication(medication)
    }

    suspend fun updateTakenStatus(id: Long, isTaken: Boolean) {
        careDao.updateTakenStatus(id, isTaken)
    }

    suspend fun resetAllTakenToday() {
        careDao.resetAllTakenToday()
    }

    suspend fun deleteMedicationById(id: Long) {
        careDao.deleteMedicationById(id)
    }

    // --- Actions sur les Documents / Ordonnances ---
    val allDocuments: Flow<List<MedicalDocument>> = careDao.getAllDocuments()

    suspend fun insertDocument(document: MedicalDocument): Long {
        return careDao.insertDocument(document)
    }

    suspend fun deleteDocumentById(id: Long) {
        careDao.deleteDocumentById(id)
    }

    fun searchDocuments(query: String): Flow<List<MedicalDocument>> {
        return careDao.searchDocuments(query)
    }

    // --- Actions sur les Symptômes ---
    val allSymptoms: Flow<List<SymptomLog>> = careDao.getAllSymptoms()

    suspend fun insertSymptom(symptom: SymptomLog): Long {
        return careDao.insertSymptom(symptom)
    }

    suspend fun deleteSymptomById(id: Long) {
        careDao.deleteSymptomById(id)
    }

    // --- Actions sur le Profil Utilisateur et Partage ---
    val userProfile: Flow<UserProfile?> = careDao.getUserProfileFlow()

    suspend fun getUserProfileDirect(): UserProfile? {
        return careDao.getUserProfileDirect()
    }

    suspend fun saveUserProfile(profile: UserProfile): Long {
        return careDao.insertUserProfile(profile)
    }
}
