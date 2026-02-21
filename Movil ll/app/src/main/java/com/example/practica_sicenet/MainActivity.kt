package com.example.practica_sicenet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.practica_sicenet.ui.SicenetViewModel
import com.example.practica_sicenet.ui.theme.Practica_SicenetTheme
import com.example.practica_sicenet.ui.screens.*
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SicenetApp() {
    val navController = rememberNavController()
    val viewModel: SicenetViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showDrawer = currentRoute != null && currentRoute != "login"

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    NavigationDrawerItem(
                        label = { Text("Perfil") },
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Carga Académica") },
                        selected = currentRoute == "carga",
                        onClick = {
                            navController.navigate("carga") {
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Kardex") },
                        selected = currentRoute == "kardex",
                        onClick = {
                            navController.navigate("kardex") {
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Calif. Unidades") },
                        selected = currentRoute == "unidades",
                        onClick = {
                            navController.navigate("unidades") {
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Calif. Finales") },
                        selected = currentRoute == "finales",
                        onClick = {
                            navController.navigate("finales") {
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Sicenet") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    NavGraph(navController, viewModel)
                }
            }
        }
    } else {
        NavGraph(navController, viewModel)
    }
}

@Composable
fun NavGraph(navController: androidx.navigation.NavHostController, viewModel: SicenetViewModel) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel, onLoginSuccess = {
                navController.navigate("profile") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("profile") { ProfileScreen(viewModel) }
        composable("carga") { CargaScreen(viewModel) }
        composable("kardex") { KardexScreen(viewModel) }
        composable("unidades") { CalificacionesUnidadesScreen(viewModel) }
        composable("finales") { CalificacionesFinalesScreen(viewModel) }
    }
}
