package com.example.practica_sicenet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practica_sicenet.ui.SicenetUiState
import com.example.practica_sicenet.ui.SicenetViewModel
import com.example.practica_sicenet.ui.theme.Practica_SicenetTheme
import org.json.JSONObject

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
    val viewModel: SicenetViewModel = viewModel(factory = SicenetViewModel.Factory)

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel, onLoginSuccess = {
                navController.navigate("profile") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("profile") {
            ProfileScreen(viewModel, onLogout = {
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            })
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Login SICENET",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it },
                    label = { Text("Matrícula") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = contrasenia,
                    onValueChange = { contrasenia = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = tipoUsuario == "ALUMNO",
                        onClick = { tipoUsuario = "ALUMNO" },
                        label = { Text("Alumno") },
                        leadingIcon = if (tipoUsuario == "ALUMNO") {
                            { Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(
                        selected = tipoUsuario == "DOCENTE",
                        onClick = { tipoUsuario = "DOCENTE" },
                        label = { Text("Docente") },
                        leadingIcon = if (tipoUsuario == "DOCENTE") {
                            { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.login(matricula, contrasenia, tipoUsuario) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState !is SicenetUiState.Loading
                ) {
                    if (uiState is SicenetUiState.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Ingresar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (uiState is SicenetUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as SicenetUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: SicenetViewModel, onLogout: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil Académico", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            when (uiState) {
                is SicenetUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SicenetUiState.ProfileLoaded -> {
                    ProfileContent((uiState as SicenetUiState.ProfileLoaded).profileData)
                }
                is SicenetUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = (uiState as SicenetUiState.Error).message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.getProfile() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProfileContent(data: String) {
    // Función de ayuda para buscar valores en el JSON (ignora mayúsculas y minúsculas en las claves)
    fun JSONObject.find(vararg keys: String): String {
        for (keyToFind in keys) {
            val iterator = this.keys()
            while (iterator.hasNext()) {
                val currentKey = iterator.next()
                if (currentKey.equals(keyToFind, ignoreCase = true)) {
                    val value = this.optString(currentKey, "").trim()
                    if (value.isNotEmpty() && !value.equals("null", ignoreCase = true)) {
                        return value
                    }
                }
            }
        }
        return "-"
    }

    var nombre = "Nombre No Disponible"
    var matricula = ""
    var carrera = "No disponible"
    var especialidad = "No disponible"
    var semestre = "-"
    var estatus = "-"
    var credAcum = "-"
    var credAct = "-"
    var inscrito = "-"
    var modEd = "-"
    var adeudoStr = "Sin adeudos"
    var fechaRein = "-"

    try {
        var json = JSONObject(data)
        if (json.has("d")) {
             val dValue = json.get("d")
             json = if (dValue is String) JSONObject(dValue) else dValue as JSONObject
        }
        
        val student = json.optJSONObject("alumno") ?: json

        nombre = student.find("nombre", "strNombre", "NombreCompleto", "nombreAlumno")
        matricula = student.find("matricula", "clvMatricula", "Matricula")
        carrera = student.find("carrera", "strCarrera", "Carrera")
        especialidad = student.find("especialidad", "strEspecialidad", "Especialidad")
        semestre = student.find("semestre", "semestreActual", "strSemestre", "Semestre")
        estatus = student.find("estatus", "strEstatus", "Estatus")
        credAcum = student.find("creditosAcumulados", "creAcumulados", "CreditosAcumulados", "creditosAcum")
        credAct = student.find("creditosActuales", "creActuales", "CreditosActuales", "creditosAct")
        
        val rawInscrito = student.find("inscrito")
        inscrito = if (rawInscrito.equals("true", ignoreCase = true) || rawInscrito.equals("si", ignoreCase = true) || rawInscrito == "1") "SÍ" else "NO"
        
        modEd = student.find("modalidad", "modEducativa", "Modalidad", "modEducativo")
        
        val rawAdeudo = student.find("adeudo", "strAdeudo", "Adeudo", "tieneAdeudo")
        adeudoStr = if (rawAdeudo.equals("false", ignoreCase = true) || rawAdeudo == "0" || rawAdeudo == "-" || rawAdeudo.isBlank() || rawAdeudo.contains("sin", ignoreCase = true)) {
            "Sin adeudos"
        } else {
            rawAdeudo // Muestra el mensaje de adeudo si existe
        }
        
        fechaRein = student.find("fechaReinscripcion", "strFechaReinscripcion", "FechaReinscripcion")
        
    } catch (e: Exception) {
        nombre = "Error de Formato"
        carrera = data // Muestra los datos crudos para depurar
        especialidad = "No se pudieron procesar todos los datos."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(text = matricula, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        InfoCardFull("Carrera", carrera)
        InfoCardFull("Especialidad", especialidad)
        
        Row(Modifier.fillMaxWidth()) {
            InfoCardHalf("Semestre", semestre, Modifier.weight(1f))
            InfoCardHalf("Estatus", estatus, Modifier.weight(1f))
        }
        
        Row(Modifier.fillMaxWidth()) {
            InfoCardHalf("Créditos Acum.", credAcum, Modifier.weight(1f))
            InfoCardHalf("Créditos Act.", credAct, Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth()) {
            InfoCardHalf("Inscrito", inscrito, Modifier.weight(1f))
            InfoCardHalf("Mod. Educativo", modEd, Modifier.weight(1f))
        }
        
        InfoCardFull("Adeudo", adeudoStr)
        InfoCardFull("Fecha Reinscripción", fechaRein)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun InfoCardFull(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun InfoCardHalf(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
