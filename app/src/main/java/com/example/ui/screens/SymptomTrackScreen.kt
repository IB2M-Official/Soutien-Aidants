package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SymptomLog
import com.example.ui.CareViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SymptomTrackScreen(
    viewModel: CareViewModel,
    modifier: Modifier = Modifier
) {
    val symptoms by viewModel.symptoms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_symptom_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un symptôme"
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

            // En-tête Éditorial de la section
            Text(
                text = "SUIVI CLINIQUE LOCAL",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Symptômes",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enregistrez précisément les changements d'état physique ou cognitif de votre proche pour en garder la trace clinique cryptée.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Liste d'historique
            if (symptoms.isEmpty()) {
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
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Aucun symptôme",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Aucun symptôme rapporté",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tout semble stable ! Cliquez sur le bouton + ci-dessous pour signaler un symptôme (fièvre, agitation, douleur...).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("symptoms_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(symptoms, key = { it.id }) { log ->
                        SymptomLogCard(
                            log = log,
                            onDelete = { viewModel.deleteSymptom(log.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding pour le FAB
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSymptomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, severity, notes ->
                viewModel.addSymptom(name, severity, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SymptomLogCard(
    log: SymptomLog,
    onDelete: () -> Unit
) {
    val dateText = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMMM 'à' HH:mm", Locale.FRANCE)
        sdf.format(Date(log.timestamp))
    }

    // Déterminer la couleur éditoriale associée à la gravité (1 à 5)
    val severityColor = when (log.severity) {
        1 -> Color(0xFF006A6A) // Teal léger
        2 -> Color(0xFF9EA33A) // Olive
        3 -> Color(0xFFD6941C) // Ambre
        4 -> Color(0xFFD35515) // Orange soutenu
        else -> Color(0xFFBA1A1A) // Rouge d'urgence
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("symptom_item_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Indicateur de gravité
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(severityColor.copy(alpha = 0.1f))
                    .border(2.dp, severityColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = log.severity.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = severityColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Corps textuel
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = log.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = dateText.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (log.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bouton de suppression
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer ce symptôme",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSymptomDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, severity: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var severity by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    val commonSymptoms = listOf(
        "Agitation / Confusion",
        "Douleurs",
        "Perte d'appétit / Hydratation",
        "Fièvre / Température",
        "Sommeil agité",
        "Troubles moteurs / Chute",
        "Oublis sévères",
        "Fatigue extrême"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Signaler un symptôme",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Choix ou saisie du symptôme
                Text(
                    text = "TYPE DE SYMPTÔME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ex: Somnolence inhabituelle, Agitation...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("symptom_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Suggestions rapides
                Text(
                    text = "SUGGESTIONS RAPIDES :",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRowLayout(
                    spacing = 8.dp
                ) {
                    commonSymptoms.forEach { suggestion ->
                        val isSelected = name == suggestion
                        FilterChip(
                            selected = isSelected,
                            onClick = { name = suggestion },
                            label = { Text(suggestion) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Gravité de 1 à 5
                Text(
                    text = "NIVEAU DE GRAVITÉ (1 À 5)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { num ->
                        val isSelected = severity == num
                        val btnColor = when (num) {
                            1 -> Color(0xFF006A6A)
                            2 -> Color(0xFF8C9130)
                            3 -> Color(0xFFC98616)
                            4 -> Color(0xFFC74C10)
                            else -> Color(0xFFBA1A1A)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) btnColor else btnColor.copy(alpha = 0.1f)
                                )
                                .border(
                                    2.dp,
                                    if (isSelected) btnColor else btnColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { severity = num }
                                .testTag("severity_btn_$num"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = num.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else btnColor
                            )
                        }
                    }
                }

                // Notes complémentaires
                Text(
                    text = "NOTES & OBSERVATIONS COMPLÉMENTAIRES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Ex: A commencé après le repas de midi. Semble s'atténuer un peu.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("symptom_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, severity, notes)
                    }
                },
                modifier = Modifier.testTag("submit_symptom_button"),
                enabled = name.isNotBlank()
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

/**
 * Layout utilitaire simple pour de la disposition en flux horizontale d'éléments.
 */
@Composable
fun FlowRowLayout(
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val layoutWidth = constraints.maxWidth

        var rowWidth = 0
        var rowHeight = 0
        var currentY = 0

        class PositionedPlaceable(val placeable: androidx.compose.ui.layout.Placeable, val x: Int, val y: Int)
        val positioned = mutableListOf<PositionedPlaceable>()

        placeables.forEach { placeable ->
            if (rowWidth + placeable.width > layoutWidth) {
                currentY += rowHeight + spacingPx
                rowWidth = 0
                rowHeight = 0
            }
            positioned.add(PositionedPlaceable(placeable, rowWidth, currentY))
            rowWidth += placeable.width + spacingPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        layout(layoutWidth, currentY + rowHeight) {
            positioned.forEach { item ->
                item.placeable.placeRelative(item.x, item.y)
            }
        }
    }
}
