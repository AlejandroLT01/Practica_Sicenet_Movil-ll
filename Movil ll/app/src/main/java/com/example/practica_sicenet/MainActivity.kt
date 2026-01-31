package com.example.practica_sicenet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practica_sicenet.ui.SicenetUiState
import com.example.practica_sicenet.ui.SicenetViewModel
import com.example.practica_sicenet.ui.theme.Practica_SicenetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Practica_SicenetTheme {
                SicenetApp()
            }
        }
    }
}

@Composable
fun SicenetApp() {
    val navController = rememberNavController()
    val viewModel: SicenetViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel, onLoginSuccess = {
                navController.navigate("profile") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("profile") {
            ProfileScreen(viewModel)
        }
    }
}

@Composable
fun LoginScreen(viewModel: SicenetViewModel, onLoginSuccess: () -> Unit) {
    var matricula by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }
    var tipoUsuario by remember { mutableStateOf("ALUMNO") }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is SicenetUiState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Login SICENET", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = matricula,
                onValueChange = { matricula = it },
                label = { Text("Matrícula") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = contrasenia,
                onValueChange = { contrasenia = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = tipoUsuario == "ALUMNO", onClick = { tipoUsuario = "ALUMNO" })
                Text("Alumno")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = tipoUsuario == "DOCENTE", onClick = { tipoUsuario = "DOCENTE" })
                Text("Docente")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.login(matricula, contrasenia, tipoUsuario) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is SicenetUiState.Loading
            ) {
                if (uiState is SicenetUiState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Ingresar")
                }
            }

            if (uiState is SicenetUiState.Error) {
                Text(
                    text = (uiState as SicenetUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: SicenetViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Perfil Académico", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is SicenetUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is SicenetUiState.ProfileLoaded -> {
                    Text(text = (uiState as SicenetUiState.ProfileLoaded).profileData)
                }
                is SicenetUiState.Error -> {
                    Text(text = (uiState as SicenetUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.getProfile() }) {
                        Text("Reintentar")
                    }
                }
                else -> {}
            }
        }
    }
}
