package com.example.sicenetmultiplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenetmultiplatform.data.Kardex
import com.example.sicenetmultiplatform.ui.SicenetViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KardexScreen(viewModel: SicenetViewModel) {
    val kardex by viewModel.kardex.collectAsState()
    val alumno by viewModel.alumno.collectAsState()

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
            val date = Instant.fromEpochMilliseconds(it.lastUpdate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val dateStr = "${date.dayOfMonth}/${date.monthNumber}/${date.year} ${date.hour}:${date.minute.toString().padStart(2, '0')}"
            Text("Última actualización: $dateStr", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
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

@OptIn(ExperimentalMaterial3Api::class)
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
