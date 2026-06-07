package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MedicalDocument
import com.example.data.Medication
import com.example.data.CareRepository
import com.example.data.SymptomLog
import com.example.data.UserProfile
import com.example.util.TextAnalyzer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CareViewModel(private val repository: CareRepository) : ViewModel() {

    // --- ÉTAT DES MÉDICAMENTS ---
    val medications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ÉTAT DES SYMPTÔMES ---
    val symptoms: StateFlow<List<SymptomLog>> = repository.allSymptoms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ÉTAT DU PROFIL UTILISATEUR & DE LA SÉCURITÉ ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isUserAuthenticated = MutableStateFlow(false)
    val isUserAuthenticated = _isUserAuthenticated.asStateFlow()

    // --- ÉTAT DES DOCUMENTS ET RECHERCHE ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Recherche réactive sur les documents à partir du mot-clé
    val documents: StateFlow<List<MedicalDocument>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allDocuments
            } else {
                repository.searchDocuments(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ÉTATS D'OCR ET TRAITEMENT ---
    private val _isOcrProcessing = MutableStateFlow(false)
    val isOcrProcessing = _isOcrProcessing.asStateFlow()

    private val _lastOcrResult = MutableStateFlow<String?>(null)
    val lastOcrResult = _lastOcrResult.asStateFlow()

    // Méthode de vérification et reset automatique journalier
    fun checkAndAutoResetToday(context: Context) {
        val sharedPrefs = context.getSharedPreferences("care_prefs", Context.MODE_PRIVATE)
        val lastResetDate = sharedPrefs.getString("last_reset_date", "")
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (lastResetDate != todayDate) {
            viewModelScope.launch {
                repository.resetAllTakenToday()
                sharedPrefs.edit().putString("last_reset_date", todayDate).apply()
            }
        }
    }

    // --- ACTIONS MÉDICAMENTS ---

    fun addMedication(name: String, dosage: String, frequency: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val med = Medication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    isTakenToday = false
                )
                repository.insertMedication(med)
            }
        }
    }

    fun toggleMedicationTaken(medication: Medication) {
        viewModelScope.launch {
            val updated = medication.copy(isTakenToday = !medication.isTakenToday)
            repository.updateMedication(updated)
        }
    }

    fun resetAllMedications() {
        viewModelScope.launch {
            repository.resetAllTakenToday()
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            repository.deleteMedicationById(id)
        }
    }

    // --- ACTIONS DOCUMENTS ---

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Traite un document scanné : copie l'image locale Uri vers les fichiers internes privés de l'application
     * (sécurité et confidentialité totales, pas de Cloud), lance l'OCR local ML Kit et l'insère en base Room.
     */
    fun scanAndSaveDocument(
        context: Context,
        title: String,
        imageUri: Uri,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isOcrProcessing.value = true
            _lastOcrResult.value = null
            try {
                // 1. Extraire le texte avec ML Kit OCR
                val textAnalyzer = TextAnalyzer(context)
                val extractedText = textAnalyzer.extractTextFromImageUri(imageUri)
                _lastOcrResult.value = extractedText

                // 2. Copier l'image de manière permanente et sécurisée en interne privé
                val savedLocalPath = copyImageToPrivateStorage(context, imageUri)

                // 3. Sauvegarder dans Room pour l'archivage local
                val doc = MedicalDocument(
                    title = title.ifBlank { "Ordonnance sans titre" },
                    localFilePath = savedLocalPath,
                    extractedText = extractedText,
                    dateAdded = System.currentTimeMillis()
                )
                repository.insertDocument(doc)
                _isOcrProcessing.value = false
                onComplete()
            } catch (e: Exception) {
                _lastOcrResult.value = "Erreur de traitement : ${e.localizedMessage}"
                _isOcrProcessing.value = false
            }
        }
    }

    fun deleteDocument(id: Long, filePath: String) {
        viewModelScope.launch {
            // Supprime l'entrée de la base de données
            repository.deleteDocumentById(id)
            // Supprime également le fichier image local associé
            try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Copie l'image de l'URI d'origine vers l'espace de stockage interne de l'application pour persister
     * le document indépendamment des caches éphémères du sélecteur ou de la caméra.
     */
    private fun copyImageToPrivateStorage(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val fileName = "scan_document_${System.currentTimeMillis()}.jpg"
        val destinationFile = File(context.filesDir, fileName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return destinationFile.absolutePath
    }

    // --- ACTIONS SYMPTÔMES ---

    fun addSymptom(name: String, severity: Int, notes: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val s = SymptomLog(
                    name = name,
                    severity = severity,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertSymptom(s)
            }
        }
    }

    fun deleteSymptom(id: Long) {
        viewModelScope.launch {
            repository.deleteSymptomById(id)
        }
    }

    // --- LOGIQUE INSCRIPTION, CO-CONNEXION ET PARTAGE VIRTUEL LOCAL ---

    fun registerProfile(
        firstName: String,
        lastName: String,
        phone: String,
        roleInCare: String,
        pinCode: String,
        patientName: String
    ) {
        viewModelScope.launch {
            if (firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank()) {
                val profile = UserProfile(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    roleInCare = roleInCare,
                    pinCode = pinCode,
                    isRegistered = true,
                    patientName = patientName
                )
                repository.saveUserProfile(profile)
                _isUserAuthenticated.value = true
            }
        }
    }

    fun login(pin: String): Boolean {
        val currentProfile = userProfile.value
        return if (currentProfile != null && currentProfile.isRegistered) {
            if (currentProfile.pinCode.isBlank() || currentProfile.pinCode == pin) {
                _isUserAuthenticated.value = true
                true
            } else {
                false
            }
        } else {
            // Aucun profil => Inscription requise, authentification temporaire autorisée pendant l'inscription
            false
        }
    }

    fun logout() {
        _isUserAuthenticated.value = false
    }

    fun updateSharingSettings(
        contactName: String,
        contactPhone: String,
        mode: String,
        active: Boolean
    ) {
        viewModelScope.launch {
            val currentProfile = userProfile.value ?: UserProfile(
                firstName = "",
                lastName = "",
                phone = "",
                roleInCare = "",
                isRegistered = false,
                patientName = ""
            )
            val updated = currentProfile.copy(
                sharedContactName = contactName,
                sharedContactPhone = contactPhone,
                sharingRightsMode = mode,
                isSharingActive = active
            )
            repository.saveUserProfile(updated)
        }
    }

    fun bypassAuth() {
        _isUserAuthenticated.value = true
    }
}

/**
 * Factory personnalisée pour instancier le CareViewModel avec son Repository.
 */
class CareViewModelFactory(private val repository: CareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CareViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel inconnu ou non géré")
    }
}
