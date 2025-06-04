package com.example.myapplication

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ArrowBack


@Composable
fun MyApp() {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = when {
                            currentRoute?.startsWith("routeDetail") == true -> "Детали маршрута"
                            currentRoute == "routes" -> "Маршруты"
                            else -> "StudyShare"
                        }
                    )
                },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null && currentRoute != "menu") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    } else {
                        IconButton(onClick = {
                            scope.launch { scaffoldState.drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Меню")
                        }
                    }
                }
            )
        },
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Меню", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    scope.launch { scaffoldState.drawerState.close() }
                    navController.navigate("routes") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Text("📍 Просмотр маршрутов")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "menu",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("menu") { MainMenu(navController) }
            composable("routes") { RouteListScreen(navController) }
            composable(
                "routeDetail/{routeId}",
                arguments = listOf(navArgument("routeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val routeId = backStackEntry.arguments?.getInt("routeId") ?: 0
                RouteDetailScreen(routeId, navController)
            }
        }
    }
}
