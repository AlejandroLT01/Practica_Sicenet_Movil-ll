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
import com.example.practica_sicenet.data.Kardex
import com.example.practica_sicenet.ui.SicenetViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun KardexScreen(viewModel: SicenetViewModel) {
    val kardex by viewModel.kardex.collectAsState()
    val alumno by viewModel.alumno.collectAsState()

    // Sincronizar Kardex usando el lineamiento real del alumno
    LaunchedEffect(alumno) {
        alumno?.let {
            if (kardex.isEmpty()) {
                viewModel.syncData("KARDEX", lineamiento = it.modEducativo)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kardex", style = MaterialTheme.typography.headlineMedium)
        alumno?.let {
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it.lastUpdate))
            Text("Última actualización: $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (kardex.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No hay datos en el Kardex o sincronizando...")
            }
        } else {
            LazyColumn {
                items(kardex) { item ->
                    KardexItem(item)
                }
            }
        }
    }
}

@Composable
fun KardexItem(item: Kardex) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.materia, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Periodo: ${item.periodo}", fontSize = 12.sp)
                Text(text = "Semestre: ${item.semestre} | Créditos: ${item.creditos}", fontSize = 12.sp)
            }
            Text(
                text = item.calificacion.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (item.calificacion >= 70) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
