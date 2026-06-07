package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CareDao {

    // --- LOGIQUE MÉDICAMENTS ---

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Query("UPDATE medications SET isTakenToday = :isTaken WHERE id = :id")
    suspend fun updateTakenStatus(id: Long, isTaken: Boolean)

    @Query("UPDATE medications SET isTakenToday = 0")
    suspend fun resetAllTakenToday()

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    // --- LOGIQUE DOCUMENTS & ORDONNANCES ---

    @Query("SELECT * FROM medical_documents ORDER BY dateAdded DESC")
    fun getAllDocuments(): Flow<List<MedicalDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: MedicalDocument): Long

    @Query("DELETE FROM medical_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    /**
     * Recherche performante à l'intérieur du texte extrait par l'OCR local ou du titre.
     */
    @Query("""
        SELECT * FROM medical_documents 
        WHERE title LIKE '%' || :query || '%' 
        OR extractedText LIKE '%' || :query || '%' 
        ORDER BY dateAdded DESC
    """)
    fun searchDocuments(query: String): Flow<List<MedicalDocument>>

    // --- LOGIQUE SUIVI DES SYMPTÔMES ---

    @Query("SELECT * FROM symptom_logs ORDER BY timestamp DESC")
    fun getAllSymptoms(): Flow<List<SymptomLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomLog): Long

    @Query("DELETE FROM symptom_logs WHERE id = :id")
    suspend fun deleteSymptomById(id: Long)

    // --- LOGIQUE INCRIPTION & CONFIG DU PROFIL D'ACCÈS ---

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile): Long
}
