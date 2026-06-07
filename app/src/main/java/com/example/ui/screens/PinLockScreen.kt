package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
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
import com.example.ui.CareViewModel

@Composable
fun PinLockScreen(
    viewModel: CareViewModel,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Label et logo Editorial
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Chiffrement local",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "SOUTIEN AIDANTS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Accès Sécurisé",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pour protéger le secret médical, saisissez votre code PIN local.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Visualisation des ronds de mot de passe (4 points max)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..4).forEach { index ->
                val filled = enteredPin.length >= index
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clavier numérique personnalisé (M3 / Editorial spacing)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "back")
            )

            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { char ->
                        if (char.isEmpty()) {
                            Spacer(modifier = Modifier.size(64.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                                    .clickable {
                                        errorMessage = null
                                        if (char == "back") {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        } else {
                                            if (enteredPin.length < 4) {
                                                enteredPin += char
                                                if (enteredPin.length == 4) {
                                                    // Déclencher la vérification automatique
                                                    val success = viewModel.login(enteredPin)
                                                    if (!success) {
                                                        enteredPin = ""
                                                        errorMessage = "Code PIN incorrect"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("pin_btn_$char"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (char == "back") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Effacer",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = char,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Option d'accès sécurisé temporaire si nécessaire, ou rappel
        Text(
            text = "Enregistré crypté localement sur l'appareil",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
