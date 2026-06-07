package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.CareViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: CareViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // En-tête Éditorial
        Text(
            text = "ESPACE AIDANT CONFIDENTIEL",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Profil & Partage",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Configurez vos détails d'inscription locale, protégez l'accès par code PIN, et gérez les droits de partage cryptés avec les autres membres de la famille.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        val isRegistered = profile?.isRegistered ?: false

        if (!isRegistered) {
            // Écran Magic Wizard d'inscription d'aide locale
            RegistrationWizard(
                onRegister = { fn, ln, ph, role, pin, ptName ->
                    viewModel.registerProfile(fn, ln, ph, role, pin, ptName)
                }
            )
        } else {
            // Informations utilisateur & partage
            profile?.let { userPr ->
                ProfileDashboard(
                    profile = userPr,
                    onLogout = { viewModel.logout() },
                    onUpdateSharing = { cName, cPhone, rMode, rActive ->
                        viewModel.updateSharingSettings(cName, cPhone, rMode, rActive)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun RegistrationWizard(
    onRegister: (firstName: String, lastName: String, phone: String, roleInCare: String, pinCode: String, patientName: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var roleInCare by remember { mutableStateOf("Fils / Fille") }
    var pinCode by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }

    val roles = listOf("Conjoint", "Fils / Fille", "Infirmier", "Auxiliaire de vie", "Autre proche")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("registration_wizard_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CRÉATION DE DOSSIER SECURE",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )

            // Prénom de l'aidant
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Votre prénom (Aidant)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_firstname"),
                shape = RoundedCornerShape(12.dp)
            )

            // Nom de l'aidant
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Votre nom de famille") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_lastname"),
                shape = RoundedCornerShape(12.dp)
            )

            // Téléphone de l'aidant
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Votre numéro de téléphone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_phone"),
                shape = RoundedCornerShape(12.dp)
            )

            // Personne aidée
            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text("Prénom & Nom du proche aidé") },
                placeholder = { Text("Ex: Marie Dupont") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_patient_name"),
                shape = RoundedCornerShape(12.dp)
            )

            // Rôle de relation
            Text(
                text = "VOTRE RELATION AVEC LE PROCHE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pour simplifier, affiche de simples puces de filtres
                roles.take(3).forEach { role ->
                    val isSelected = roleInCare == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            .clickable { roleInCare = role }
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Code PIN de verrouillage local (Optionnel)
            OutlinedTextField(
                value = pinCode,
                onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinCode = it },
                label = { Text("Code PIN de sécurité local (4 chiffres - Optionnel)") },
                placeholder = { Text("Ex: 1234") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_pincode"),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "🔒 Vos données restent entièrement stockées localement sur ce téléphone et cryptées. Rien n'est envoyé sur des serveurs.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    onRegister(firstName, lastName, phone, roleInCare, pinCode, patientName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reg_submit_button"),
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank() && patientName.isNotBlank()
            ) {
                Text("Valider mon inscription locale", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileDashboard(
    profile: UserProfile,
    onLogout: () -> Unit,
    onUpdateSharing: (contactName: String, contactPhone: String, rightsMode: String, isSharingEnabled: Boolean) -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    var shareContactName by remember { mutableStateOf(profile.sharedContactName) }
    var shareContactPhone by remember { mutableStateOf(profile.sharedContactPhone) }
    var rightsMode by remember { mutableStateOf(profile.sharingRightsMode) }
    var isSharingActive by remember { mutableStateOf(profile.isSharingActive) }

    var syncStatusMessage by remember { mutableStateOf("Prêt pour l'envoi sécurisé") }
    var isSyncingInProgress by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Carte d'identité de l'aidant (Editorial)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${profile.firstName.take(1).uppercase()}${profile.lastName.take(1).uppercase()}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${profile.firstName} ${profile.lastName}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Lien : ${profile.roleInCare} de ${profile.patientName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Bouton de déconnexion (Simulateur d'authentification)
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("lock_app_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Verrouiller l'accès de l'application",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TÉLÉPHONE ENREGISTRÉ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = profile.phone,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (profile.pinCode.isNotBlank()) "PIN ACTIF 🔒" else "SANS VERROU ⚠️",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Section de Partage Sécurisé de Données avec les proches (Exigé par l'utilisateur)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PARTAGE SANS COÛT CLOUD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Partager avec un Proche",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Switch(
                        checked = isSharingActive,
                        onCheckedChange = {
                            isSharingActive = it
                            onUpdateSharing(shareContactName, shareContactPhone, rightsMode, it)
                        },
                        modifier = Modifier.testTag("sharing_active_toggle")
                    )
                }

                Text(
                    text = "Aide à la coordination familiale : synchronisez en pair-à-pair offline crypté AES-256 avec un proche de confiance sans aucun hébergement centralisé payant.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isSharingActive) {
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                    // Contact de partage
                    OutlinedTextField(
                        value = shareContactName,
                        onValueChange = {
                            shareContactName = it
                            onUpdateSharing(it, shareContactPhone, rightsMode, isSharingActive)
                        },
                        label = { Text("Prénom & Nom du proche") },
                        placeholder = { Text("Ex: Thomas Rossi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sharing_contact_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = shareContactPhone,
                        onValueChange = {
                            shareContactPhone = it
                            onUpdateSharing(shareContactName, it, rightsMode, isSharingActive)
                        },
                        label = { Text("Numéro de téléphone du destinataire") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sharing_contact_phone"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Granularité des modes de droit requis : Consultation, Modification, Prescription
                    Text(
                        text = "DROITS D'ACCÈS DU DESTINATAIRE :",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val modes = listOf("Consultation", "Modification", "Prescription")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        modes.forEach { mode ->
                            val isSelected = rightsMode == mode
                            val colorScheme = MaterialTheme.colorScheme
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colorScheme.primary else colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        rightsMode = mode
                                        onUpdateSharing(shareContactName, shareContactPhone, mode, isSharingActive)
                                    }
                                    .border(
                                        1.dp,
                                        if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (mode) {
                                        "Consultation" -> "Lect. seule"
                                        "Modification" -> "Modific."
                                        else -> "Prescript."
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Simulator Button: Synch Data Now
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "SIMULATEUR DE PARTAGE LOCAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Droits alloués : $rightsMode",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = {
                                    scope.launch {
                                        isSyncingInProgress = true
                                        syncStatusMessage = "Génération du canal d'échange pair-à-pair..."
                                        delay(1000)
                                        syncStatusMessage = "Chiffrement AES du dossier médical..."
                                        delay(1000)
                                        syncStatusMessage = "Envoi sécurisé du flux vers ${shareContactPhone}..."
                                        delay(1200)
                                        isSyncingInProgress = false
                                        syncStatusMessage = "Dossier partagé avec succès en mode : ${rightsMode} ! ✅"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_sync_button"),
                                enabled = !isSyncingInProgress && shareContactName.isNotBlank() && shareContactPhone.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isSyncingInProgress) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Simuler la Synchronisation", fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = syncStatusMessage,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
