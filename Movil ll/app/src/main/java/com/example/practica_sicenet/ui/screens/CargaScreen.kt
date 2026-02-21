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
import com.example.practica_sicenet.data.CargaAcademica
import com.example.practica_sicenet.ui.SicenetViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CargaScreen(viewModel: SicenetViewModel) {
    val carga by viewModel.carga.collectAsState()
    val alumno by viewModel.alumno.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.syncData("CARGA")
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Carga Académica", style = MaterialTheme.typography.headlineMedium)
        alumno?.let {
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it.lastUpdate))
            Text("Última actualización: $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(carga) { item ->
                CargaItem(item)
            }
        }
    }
}

@Composable
fun CargaItem(item: CargaAcademica) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = item.materia, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Docente: ${item.docente}", fontSize = 14.sp)
            Text(text = "Grupo: ${item.grupo} | Créditos: ${item.creditos}", fontSize = 12.sp)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text(text = "Lunes: ${item.lunes} | Martes: ${item.martes} | Miércoles: ${item.miercoles}", fontSize = 11.sp)
            Text(text = "Jueves: ${item.jueves} | Viernes: ${item.viernes}", fontSize = 11.sp)
        }
    }
}
