package com.example.practicacontentproviderensicedroidcompoose

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.practicacontentproviderensicedroidcompoose.ui.theme.PracticaContentProviderEnSiceDroidCompooseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PracticaContentProviderEnSiceDroidCompooseTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ContentProviderClientScreen()
                }
            }
        }
    }
}

// Modelo de datos expandido para soportar todos los campos
data class SicenetItem(
    val materia: String,
    val docente: String = "",
    val grupo: String = "",
    val creditos: String = "",
    val calificacion: String = "",
    val semestre: String = "",
    val periodo: String = "",
    val isCarga: Boolean
)

@Composable
fun ContentProviderClientScreen() {
    val context = LocalContext.current
    var dataList by remember { mutableStateOf<List<SicenetItem>>(emptyList()) }
    var title by remember { mutableStateOf("Consulta de Datos") }
    
    // Estados para el diálogo de edición
    var showEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<SicenetItem?>(null) }
    
    // Variables temporales para los campos del formulario
    var editDocente by remember { mutableStateOf("") }
    var editGrupo by remember { mutableStateOf("") }
    var editCreditos by remember { mutableStateOf("") }
    var editCalif by remember { mutableStateOf("") }
    var editSemestre by remember { mutableStateOf("") }
    var editPeriodo by remember { mutableStateOf("") }
    var editMateria by remember { mutableStateOf("") } // NUEVO: Para editar el nombre
    var originalMateria by remember { mutableStateOf("") } // NUEVO: Para no perder la referencia

    val AUTHORITY = "com.example.practica_sicenet.provider"
    val URI_CARGA = Uri.parse("content://$AUTHORITY/carga")
    val URI_KARDEX = Uri.parse("content://$AUTHORITY/kardex")

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) Toast.makeText(context, "Permisos concedidos", Toast.LENGTH_SHORT).show()
    }

    fun queryData(uri: Uri, isCarga: Boolean) {
        try {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            val results = mutableListOf<SicenetItem>()
            
            cursor?.use {
                while (it.moveToNext()) {
                    val mat = it.getString(it.getColumnIndexOrThrow("materia"))
                    if (isCarga) {
                        results.add(SicenetItem(
                            materia = mat,
                            docente = it.getString(it.getColumnIndexOrThrow("docente")),
                            grupo = it.getString(it.getColumnIndexOrThrow("grupo")),
                            creditos = it.getInt(it.getColumnIndexOrThrow("creditos")).toString(),
                            isCarga = true
                        ))
                    } else {
                        results.add(SicenetItem(
                            materia = mat,
                            calificacion = it.getInt(it.getColumnIndexOrThrow("calificacion")).toString(),
                            semestre = it.getInt(it.getColumnIndexOrThrow("semestre")).toString(),
                            creditos = it.getInt(it.getColumnIndexOrThrow("creditos")).toString(),
                            periodo = it.getString(it.getColumnIndexOrThrow("periodo")),
                            isCarga = false
                        ))
                    }
                }
            }
            dataList = results
            title = if (isCarga) "Carga Académica" else "Kardex"
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun deleteItem(item: SicenetItem) {
        try {
            val uri = if (item.isCarga) URI_CARGA else URI_KARDEX
            val rows = context.contentResolver.delete(uri, "materia=?", arrayOf(item.materia))
            if (rows > 0) {
                Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                queryData(uri, item.isCarga)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateData() {
        itemToEdit?.let { item ->
            try {
                val values = ContentValues().apply {
                    put("materia", editMateria) // Nuevo nombre
                    put("creditos", editCreditos.toIntOrNull() ?: 0)
                    if (item.isCarga) {
                        put("docente", editDocente)
                        put("grupo", editGrupo)
                    } else {
                        put("calificacion", editCalif.toIntOrNull() ?: 0)
                        put("semestre", editSemestre.toIntOrNull() ?: 0)
                        put("periodo", editPeriodo)
                    }
                }
                val uri = if (item.isCarga) URI_CARGA else URI_KARDEX
                // Usamos originalMateria en el selectionArgs para identificar el registro original
                val rows = context.contentResolver.update(uri, values, "materia=?", arrayOf(originalMateria))
                if (rows > 0) {
                    Toast.makeText(context, "Actualizado", Toast.LENGTH_SHORT).show()
                    queryData(uri, item.isCarga)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        showEditDialog = false
    }

    // Diálogo de Gestión (Editar / Eliminar)
    if (showEditDialog && itemToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Gestionar: ${originalMateria}") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(value = editMateria, onValueChange = { editMateria = it }, label = { Text("Nombre de la Materia") }, modifier = Modifier.fillMaxWidth())
                        if (itemToEdit!!.isCarga) {
                            OutlinedTextField(value = editDocente, onValueChange = { editDocente = it }, label = { Text("Docente") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editGrupo, onValueChange = { editGrupo = it }, label = { Text("Grupo") }, modifier = Modifier.fillMaxWidth())
                        } else {
                            OutlinedTextField(value = editCalif, onValueChange = { editCalif = it }, label = { Text("Calificación") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editSemestre, onValueChange = { editSemestre = it }, label = { Text("Semestre") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editPeriodo, onValueChange = { editPeriodo = it }, label = { Text("Periodo") }, modifier = Modifier.fillMaxWidth())
                        }
                        OutlinedTextField(value = editCreditos, onValueChange = { editCreditos = it }, label = { Text("Créditos") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(onClick = { updateData() }) { Text("Guardar Cambios") }
            },
            dismissButton = {
                Button(onClick = { deleteItem(itemToEdit!!); showEditDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Eliminar")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gestor Sicenet", style = MaterialTheme.typography.headlineMedium)
        
        Button(onClick = { launcher.launch(arrayOf("com.example.practica_sicenet.READ_DATABASE", "com.example.practica_sicenet.WRITE_DATABASE")) }, modifier = Modifier.fillMaxWidth()) {
            Text("1. Solicitar Permisos")
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { queryData(URI_CARGA, true) }, modifier = Modifier.weight(1f)) { Text("Ver Carga") }
            Button(onClick = { queryData(URI_KARDEX, false) }, modifier = Modifier.weight(1f)) { Text("Ver Kardex") }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(dataList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                        itemToEdit = item
                        originalMateria = item.materia // Guardamos el nombre original
                        editMateria = item.materia
                        editDocente = item.docente
                        editGrupo = item.grupo
                        editCreditos = item.creditos
                        editCalif = item.calificacion
                        editSemestre = item.semestre
                        editPeriodo = item.periodo
                        showEditDialog = true
                    }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "📖 ${item.materia}", style = MaterialTheme.typography.titleSmall)
                            if (item.isCarga) {
                                Text("👨‍🏫 ${item.docente} | 👥 ${item.grupo} | 🎓 Cr: ${item.creditos}")
                            } else {
                                Text("⭐ ${item.calificacion} | 📅 Sem: ${item.semestre} | 🕒 ${item.periodo}")
                            }
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }
    }
}
