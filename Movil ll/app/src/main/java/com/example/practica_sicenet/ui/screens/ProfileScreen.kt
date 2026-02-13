package com.example.practica_sicenet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practica_sicenet.ui.SicenetUiState
import com.example.practica_sicenet.ui.SicenetViewModel

@Composable
fun ProfileScreen(viewModel: SicenetViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    Scaffold {
        padding ->
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

            when (val state = uiState) {
                is SicenetUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is SicenetUiState.ProfileLoaded -> {
                    val alumno = state.alumno
                    InfoCard(title = "Matrícula", value = alumno.matricula)
                    InfoCard(title = "Carrera", value = alumno.carrera)
                    InfoCard(title = "Nombre", value = alumno.nombre)
                    InfoCard(title = "Especialidad", value = alumno.especialidad)
                    InfoCard(title = "Semestre", value = alumno.semestre.toString())
                    InfoCard(title = "Estatus", value = alumno.estatus)
                    InfoCard(title = "Créditos Totales", value = alumno.creditosReunidos.toString())
                    InfoCard(title = "Créditos Actuales", value = alumno.creditosActuales.toString())
                    InfoCard(title = "Inscrito", value = if (alumno.inscrito) "Sí" else "No")
                    InfoCard(title = "Mod. Educativo", value = alumno.modEducativo.toString())
                    InfoCard(title = "Adeudo", value = if (alumno.adeudo) "Sí" else "Sin Adeudos")
                    InfoCard(title = "Fecha de Reinscripción", value = alumno.fechaReinscripcion)
                }
                is SicenetUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
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