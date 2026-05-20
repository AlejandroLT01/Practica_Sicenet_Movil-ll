package com.example.sicenetmultiplatform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.sicenetmultiplatform.data.*
import com.example.sicenetmultiplatform.data.repository.*
import com.example.sicenetmultiplatform.ui.SicenetViewModel
import com.example.sicenetmultiplatform.ui.theme.Practica_SicenetTheme
import com.example.sicenetmultiplatform.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    val sicenetRepository = SicenetComponent.sicenetRepository
    val localRepository = SicenetComponent.localRepository

    Practica_SicenetTheme {
        SicenetApp(localRepository, sicenetRepository)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SicenetApp(localRepository: LocalRepository, sicenetRepository: SicenetRepository) {
    val navController = rememberNavController()
    val viewModel: SicenetViewModel = viewModel { 
        SicenetViewModel(localRepository, sicenetRepository) 
    }
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
                                Text("☰")
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
fun NavGraph(navController: NavHostController, viewModel: SicenetViewModel) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel) {
                navController.navigate("profile") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("profile") { ProfileScreen(viewModel) }
        composable("carga") { CargaScreen(viewModel) }
        composable("kardex") { KardexScreen(viewModel) }
        composable("unidades") { CalificacionesUnidadesScreen(viewModel) }
        composable("finales") { CalificacionesFinalesScreen(viewModel) }
    }
}
