package com.example.practica_sicenet.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practica_sicenet.ui.SicenetUiState
import com.example.practica_sicenet.ui.SicenetViewModel
import com.example.practica_sicenet.ui.theme.Practica_SicenetTheme

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

            //InfoCard(title = "No. Control", value = "1234")
            //Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is SicenetUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is SicenetUiState.ProfileLoaded -> {
                    //Text(text = (uiState as SicenetUiState.ProfileLoaded).profileData)
                    //Spacer(modifier = Modifier.height(16.dp))
                    InfoCard(title = "Datos alumno", value = (uiState as SicenetUiState.ProfileLoaded).profileData)
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

@Composable
fun InfoCard(
    title: String,
    value: String,
    color: Color = Color(0xFFF8F9FA),
    contentColor: Color = Color.Black
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 15.sp, color = contentColor)
        }
    }
}