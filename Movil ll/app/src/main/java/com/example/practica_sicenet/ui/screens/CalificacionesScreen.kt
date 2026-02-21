package com.example.practica_sicenet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practica_sicenet.data.CalificacionFinal
import com.example.practica_sicenet.data.CalificacionUnidad
import com.example.practica_sicenet.ui.SicenetViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalificacionesUnidadesScreen(viewModel: SicenetViewModel) {
    val calif by viewModel.califUnidades.collectAsState()
    val alumno by viewModel.alumno.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.syncData("UNIDADES")
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Calificaciones por Unidad", style = MaterialTheme.typography.headlineMedium)
        alumno?.let {
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it.lastUpdate))
            Text("Última actualización: $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(calif) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = item.materia, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Unidades: ${item.unidades}", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CalificacionesFinalesScreen(viewModel: SicenetViewModel) {
    val calif by viewModel.califFinales.collectAsState()
    val alumno by viewModel.alumno.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.syncData("FINALES", mod = alumno?.modEducativo ?: 1)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Calificaciones Finales", style = MaterialTheme.typography.headlineMedium)
        alumno?.let {
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it.lastUpdate))
            Text("Última actualización: $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(calif) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.materia, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(
                            text = item.calificacion.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (item.calificacion >= 70) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
