package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.Medication
import com.example.ui.CareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val medications by viewModel.medications.collectAsState()

    // Vérifie et réinitialise automatiquement les traitements si le jour a changé
    LaunchedEffect(Unit) {
        viewModel.checkAndAutoResetToday(context)
    }

    var showAddDialog by remember { mutableStateOf(false) }

    // Calcul des statistiques
    val totalMeds = medications.size
    val takenMeds = medications.count { it.isTakenToday }
    val progress = if (totalMeds > 0) takenMeds.toFloat() / totalMeds else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_medication_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un médicament"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // En-tête Éditorial : Date et Avatar en haut de l'écran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val currentDate = remember {
                    SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(Date()).uppercase()
                }
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                // Avatar rond "JD" (ex: Jean Dupont) de l'éditorial
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Titre de Display Éditoriale
            Text(
                text = "Aujourd'hui",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 44.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Carte de Progrès Éditoriale
            WelcomeHeader(totalMeds = totalMeds, takenMeds = takenMeds, progress = progress)
            
            Spacer(modifier = Modifier.height(24.dp))

            // Section Titre et action de réinitialisation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROCHAINS MÉDICAMENTS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )

                if (totalMeds > 0) {
                    TextButton(
                        onClick = { viewModel.resetAllMedications() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("reset_medications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Réinitialiser",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Liste de checklist de médicaments
            if (medications.isEmpty()) {
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
                            imageVector = Icons.Outlined.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Aucun traitement enregistré",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ajoutez les traitements du jour à l'aide du bouton + pour démarrer le suivi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(medications, key = { it.id }) { med ->
                        MedicationItem(
                            medication = med,
                            onToggleTaken = { viewModel.toggleMedicationTaken(med) },
                            onDelete = { viewModel.deleteMedication(med.id) }
                        )
                    }
                }
            }
        }

        // Dialogue pour ajouter un traitement
        if (showAddDialog) {
            AddMedicationDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, dosage, freq ->
                    viewModel.addMedication(name, dosage, freq)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun WelcomeHeader(totalMeds: Int, takenMeds: Int, progress: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Carte verte/teal élégante avec ombrage doux conforme au design html
    Card(
        shape = RoundedCornerShape(24.dp), // Coins de 2rem equivalents
        colors = CardDefaults.cardColors(
            containerColor = primaryColor,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "SUIVI TRAITEMENT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (totalMeds == 0) "Commencer le suivi"
                               else if (takenMeds == totalMeds) "Tous pris aujourd'hui 🎉"
                               else "$takenMeds sur $totalMeds pris",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Barre de progrès blanche avec arrière-plan translucide
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun MedicationItem(
    medication: Medication,
    onToggleTaken: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Changement d'opacité si le traitement est pris
    val cardAlpha = if (medication.isTakenToday) 0.6f else 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medication_item_${medication.id}")
            .graphicsLayer { alpha = cardAlpha },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (medication.isTakenToday) {
            // Finement gris-vert pour les pris
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        } else {
            // Bordure plus visible (ring-2) pour les actifs
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox pour marquer comme pris
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (medication.isTakenToday) MaterialTheme.colorScheme.primary 
                        else Color.Transparent
                    )
                    .border(
                        2.dp,
                        if (medication.isTakenToday) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleTaken() }
                    .testTag("check_med_${medication.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (medication.isTakenToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Validé",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenu textuel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleTaken() }
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textDecoration = if (medication.isTakenToday) TextDecoration.LineThrough else TextDecoration.None,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${medication.dosage}  •  ${medication.frequency.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (medication.isTakenToday) MaterialTheme.colorScheme.onSurfaceVariant 
                                else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Bouton de suppression
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.testTag("delete_med_button_${medication.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer ce médicament",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer le traitement ?") },
            text = { Text("Êtes-vous sûr de vouloir supprimer '${medication.name}' de la liste des traitements ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, dosage: String, frequency: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau traitement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Nom du médicament *") },
                    isError = nameError,
                    placeholder = { Text("ex: Doliprane 1000mg") },
                    leadingIcon = { Icon(Icons.Default.Medication, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_med_name_input")
                )
                if (nameError) {
                    Text(
                        text = "Le nom du médicament est obligatoire.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    placeholder = { Text("ex: 1 comprimé, 2 gouttes") },
                    leadingIcon = { Icon(Icons.Default.Scale, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_med_dosage_input")
                )

                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Fréquence / Moment de prise") },
                    placeholder = { Text("ex: Matin et Soir, Après le repas") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_med_freq_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onAdd(name.trim(), dosage.trim().ifBlank { "Non spécifié" }, frequency.trim().ifBlank { "Selon besoin" })
                    }
                },
                modifier = Modifier.testTag("add_med_submit_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
