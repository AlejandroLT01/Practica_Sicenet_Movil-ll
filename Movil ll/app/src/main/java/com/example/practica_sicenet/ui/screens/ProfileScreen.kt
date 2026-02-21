package com.example.practica_sicenet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practica_sicenet.data.Alumno
import com.example.practica_sicenet.ui.SicenetUiState
import com.example.practica_sicenet.ui.SicenetViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(viewModel: SicenetViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val alumno by viewModel.alumno.collectAsState()

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
            
            alumno?.let {
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it.lastUpdate))
                Text("Última actualización: $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is SicenetUiState.Loading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
            }

            alumno?.let { alu ->
                InfoCard(title = "Matrícula", value = alu.matricula)
                InfoCard(title = "Carrera", value = alu.carrera)
                InfoCard(title = "Nombre", value = alu.nombre)
                InfoCard(title = "Especialidad", value = alu.especialidad)
                InfoCard(title = "Semestre", value = alu.semestre.toString())
                InfoCard(title = "Estatus", value = alu.estatus)
                InfoCard(title = "Créditos Totales", value = alu.creditosReunidos.toString())
                InfoCard(title = "Créditos Actuales", value = alu.creditosActuales.toString())
                InfoCard(title = "Inscrito", value = if (alu.inscrito) "Sí" else "No")
                InfoCard(title = "Mod. Educativo", value = alu.modEducativo.toString())
                InfoCard(title = "Adeudo", value = if (alu.adeudo) "Sí" else "Sin Adeudos")
                InfoCard(title = "Fecha de Reinscripción", value = alu.fechaReinscripcion)
            }

            if (uiState is SicenetUiState.Error) {
                Text(text = (uiState as SicenetUiState.Error).message, color = MaterialTheme.colorScheme.error)
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
