package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.MedicalDocument
import com.example.ui.CareViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentVaultScreen(
    viewModel: CareViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val documents by viewModel.documents.collectAsState()

    var selectedDocument by remember { mutableStateOf<MedicalDocument?>(null) }
    var documentToDelete by remember { mutableStateOf<MedicalDocument?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // En-tête Archivage de style Éditorial
            Text(
                text = "COFFRE-FORT LOCAL",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Classeur Ordonnances",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Recherchez instantanément par mot-clé dans l'intégralité de vos ordonnances analysées automatiquement.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Barre de recherche de style minimaliste / éditorial
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Rechercher un médicament, médecin, date...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("document_search_input"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Liste des documents
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isEmpty()) Icons.Outlined.FolderOpen else Icons.Outlined.FindInPage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Aucun document scanné" else "Aucun résultat trouvé",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) {
                                "Rendez-vous dans la section 'Scanner' pour prendre en photo ou importer une ordonnance d'un proche."
                            } else {
                                "Aucun document ou texte extrait ne contient le terme '$searchQuery'. Essayez un autre mot-clé."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        DocumentItemCard(
                            document = doc,
                            searchQuery = searchQuery,
                            onOpen = { selectedDocument = doc },
                            onDelete = { documentToDelete = doc }
                        )
                    }
                }
            }
        }
    }

    // Modal de confirmation de suppression
    documentToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text("Supprimer l'ordonnance ?") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement '${doc.title}' de votre coffre-fort local ? L'image et son analyse OCR seront détruits.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(doc.id, doc.localFilePath)
                        documentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal de détails complets d'un document
    selectedDocument?.let { doc ->
        DocumentDetailsDialog(
            document = doc,
            onDismiss = { selectedDocument = null }
        )
    }
}

@Composable
fun DocumentItemCard(
    document: MedicalDocument,
    searchQuery: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(document.dateAdded) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(document.dateAdded))
    }

    // Recherche d'un extrait de correspondance dans le texte OCR
    val textSnippet = remember(document.extractedText, searchQuery) {
        if (searchQuery.isBlank()) null else {
            val idx = document.extractedText.indexOf(searchQuery, ignoreCase = true)
            if (idx != -1) {
                val start = maxOf(0, idx - 30)
                val end = minOf(document.extractedText.length, idx + searchQuery.length + 40)
                val prefix = if (start > 0) "..." else ""
                val suffix = if (end < document.extractedText.length) "..." else ""
                val rawSnippet = document.extractedText.substring(start, end).replace('\n', ' ')
                "$prefix$rawSnippet$suffix"
            } else null
        }
    }

    Card(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("document_card_${document.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vignette miniature de l'image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val imgFile = remember(document.localFilePath) { File(document.localFilePath) }
                if (imgFile.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imgFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Miniature",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textes descriptifs
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Ajouté le $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // Affichage de l'extrait de texte s'il y a correspondance de recherche
                if (!textSnippet.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = textSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bouton supprimer document
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_doc_button_${document.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer le document",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun DocumentDetailsDialog(
    document: MedicalDocument,
    onDismiss: () -> Unit
) {
    val dateStr = remember(document.dateAdded) {
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(document.dateAdded))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Importé le $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                val file = remember(document.localFilePath) { File(document.localFilePath) }
                if (file.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(file)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Visualisation de l'ordonnance",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Texte extrait par l'IA locale (OCR) :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = document.extractedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
