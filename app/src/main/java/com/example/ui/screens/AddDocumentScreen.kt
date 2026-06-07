package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.CareViewModel
import java.io.File

@Composable
fun AddDocumentScreen(
    viewModel: CareViewModel,
    onSuccessSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOcrProcessing by viewModel.isOcrProcessing.collectAsState()
    val lastOcrResult by viewModel.lastOcrResult.collectAsState()

    var documentTitle by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // État de permission de la caméra
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Lanceur d'autorisation caméra
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
            if (isGranted && tempCameraUri != null) {
                // Relancer l'appareil photo si maintenant accordé
            }
        }
    )

    // Lanceur Photo Picker (Sélecteur système)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                // Donne un titre par défaut si vide
                if (documentTitle.isBlank()) {
                    documentTitle = "Ordonnance Importée"
                }
            }
        }
    )

    // Lanceur Appareil Photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                selectedImageUri = tempCameraUri
                if (documentTitle.isBlank()) {
                    documentTitle = "Scan Appareil Photo"
                }
            }
        }
    )

    // Aide pour créer une URI de fichier temporaire pour la caméra
    fun launchCameraIntent() {
        try {
            val tempFile = File.createTempFile("temp_care_capture_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // En-tête de Numérisation Éditorial
        Text(
            text = "SCANNER INTELLIGENT",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
        )

        Text(
            text = "Nouveau Document",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Prenez en photo une ordonnance ou importez un document médical. L'IA extrait immédiatement son texte localement sans aucune connexion Internet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (selectedImageUri == null) {
            // Étape 1 : Choisir la source du document
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Veuillez choisir une option de scan :",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                launchCameraIntent()
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("launch_camera_button")
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prendre une photo (Appareil)")
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("launch_gallery_button")
                    ) {
                        Icon(imageVector = Icons.Default.Collections, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importer un document existant")
                    }
                }
            }
        } else {
            // Étape 2 : Visualisation de l'image sélectionnée et déclenchement de l'OCR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Document sélectionné",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bouton reset / prendre une autre
                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                documentTitle = ""
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Choisir une autre image")
                        }
                    }

                    OutlinedTextField(
                        value = documentTitle,
                        onValueChange = { documentTitle = it },
                        label = { Text("Titre / Intitulé du document") },
                        placeholder = { Text("ex: Ordonnance Pédiatre Mars 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("added_document_title_input")
                    )

                    // Bloc de traitement ou Résultat OCR
                    if (isOcrProcessing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                            Text(
                                "IA en cours d'analyse locale (OCR)...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (lastOcrResult != null) {
                        Text(
                            text = "Texte extrait par l'IA :",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                        ) {
                            Text(
                                text = lastOcrResult ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }

                        // Boutons d'actions
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                selectedImageUri?.let { uri ->
                                    viewModel.scanAndSaveDocument(
                                        context = context,
                                        title = documentTitle,
                                        imageUri = uri,
                                        onComplete = {
                                            selectedImageUri = null
                                            documentTitle = ""
                                            onSuccessSaved()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_scanned_doc_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enregistrer dans le Classeur")
                        }
                    } else {
                        // Pas encore d'OCR, bouton pour lancer l'extraction
                        Button(
                            onClick = {
                                selectedImageUri?.let { uri ->
                                    viewModel.scanAndSaveDocument(
                                        context = context,
                                        title = documentTitle,
                                        imageUri = uri,
                                        onComplete = {}
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("trigger_ocr_button")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lancer l'IA d'Extraction (OCR)")
                        }
                    }
                }
            }
        }
    }
}
