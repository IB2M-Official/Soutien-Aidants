package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import com.example.data.CareDatabase
import com.example.data.CareRepository
import com.example.ui.CareViewModel
import com.example.ui.CareViewModelFactory
import com.example.ui.screens.AddDocumentScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentVaultScreen
import com.example.ui.screens.SymptomTrackScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PinLockScreen
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialisation de la base de données locale Room et du Repository
        val database = CareDatabase.getDatabase(applicationContext)
        val repository = CareRepository(database.careDao())

        // Initialisation du ViewModel
        val viewModel = ViewModelProvider(
            this,
            CareViewModelFactory(repository)
        )[CareViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainContentScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContentScreen(viewModel: CareViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsState()

    // Vérifier si un verrouillage PIN est configuré localement et requiert une connexion
    val isLockActive = remember(profile, isUserAuthenticated) {
        val p = profile
        p != null && p.isRegistered && p.pinCode.isNotBlank() && !isUserAuthenticated
    }

    if (isLockActive) {
        PinLockScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
    } else {
        // État actif de l'onglet de navigation
        var currentTab by remember { mutableIntStateOf(0) }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(), // Évite d'empiéter sur la pilule de retour système
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_nav"),
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    // Onglet 1: Aujourd'hui (checklist médicaments)
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        label = { Text("Aujourd'hui") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 0) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                                contentDescription = "Suivi médicaments"
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    // Onglet 2: Symptômes (suivi clinique)
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        label = { Text("Symptômes") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 1) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Suivi des symptômes"
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_symptoms")
                    )

                    // Onglet 3: Ordonnances (Classeur documents + recherche OCR)
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        label = { Text("Classeur") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Filled.FolderCopy else Icons.Outlined.FolderCopy,
                                contentDescription = "Classeur d'ordonnances"
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_vault")
                    )

                    // Onglet 4: Scanner (Ajout et OCR de photo)
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        label = { Text("Scanner") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 3) Icons.Filled.DocumentScanner else Icons.Outlined.DocumentScanner,
                                contentDescription = "Scanner une ordonnance"
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_scanner")
                    )

                    // Onglet 5: Profil (Inscription locale & Partage)
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4 },
                        label = { Text("Profil") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Dossier de l'aidant"
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            // Transition douce de remplacement d'écrans lors d'un switch d'onglet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> SymptomTrackScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> DocumentVaultScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    3 -> AddDocumentScreen(
                        viewModel = viewModel,
                        onSuccessSaved = {
                            // Redirige vers le classeur après une sauvegarde réussie
                            currentTab = 2
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    4 -> ProfileScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
