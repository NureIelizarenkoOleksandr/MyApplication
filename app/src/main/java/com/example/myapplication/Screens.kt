package com.example.myapplication

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Парсит время "HH:mm:ss" в секунды с начала суток
fun parseTimeToSeconds(timeStr: String): Int {
    val parts = timeStr.split(":")
    if (parts.size != 3) return Int.MAX_VALUE
    val hour = parts[0].toIntOrNull() ?: return Int.MAX_VALUE
    val minute = parts[1].toIntOrNull() ?: return Int.MAX_VALUE
    val second = parts[2].toIntOrNull() ?: return Int.MAX_VALUE
    return hour * 3600 + minute * 60 + second
}

// Получить текущее время в секундах с начала суток
fun getSecondsSinceMidnight(): Int {
    val now = Calendar.getInstance()
    return now.get(Calendar.HOUR_OF_DAY) * 3600 +
            now.get(Calendar.MINUTE) * 60 +
            now.get(Calendar.SECOND)
}

@Composable
fun MainMenu(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Откройте боковое меню для навигации", style = MaterialTheme.typography.h6)
    }
}

@Composable
fun RouteListScreen(navController: NavController) {
    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(page) {
        isLoading = true
        try {
            val result = withContext(Dispatchers.IO) {
                apiService.getRoutes(page)
            }
            routes = result.items
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    val nowSeconds = getSecondsSinceMidnight()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Маршруты (страница $page):", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))

        for (route in routes) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        navController.navigate("routeDetail/${route.id}")
                    },
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Маршрут: ${route.name}", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Фильтруем расписания с временем >= текущего времени
//                    val nextSchedule = route.schedules
//                        .filter { parseTimeToSeconds(it.departure_time) >= nowSeconds }
//                        .minByOrNull { parseTimeToSeconds(it.departure_time) }
//
//                    if (nextSchedule != null) {
//                        Text("   Отправление: ${nextSchedule.departure_time}, Прибытие: ${nextSchedule.arrival_time}")
//                    } else {
//                        // Если нет будущих отправлений сегодня, можно показать что-то или ничего
//                        Text("   Нет предстоящих отправлений сегодня")
//                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { if (page > 1) page-- }, enabled = page > 1) {
                Text("Назад")
            }
            Button(onClick = { page++ }) {
                Text("Далее")
            }
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun RouteDetailScreen(routeId: Int, navController: NavController) {
    var route by remember { mutableStateOf<Route?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(routeId) {
        try {
            val result = withContext(Dispatchers.IO) {
                apiService.getRouteById(routeId)
            }
            route = result
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        route?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Название маршрута: ${it.name}", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Номер маршрута: ${it.route_number ?: "N/A"}")
                Text("Дистанция: ${it.distance ?: "N/A"} км")
                Text("Средняя задержка: ${it.average_delay_minutes ?: "N/A"} мин")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Расписание:", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))
                for (schedule in it.schedules) {
                    Text("Отправление: ${schedule.departure_time}, Прибытие: ${schedule.arrival_time}")
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Маршрут не найден", style = MaterialTheme.typography.h6)
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit  // передаем токен назад
) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isLogin) "Вход" else "Регистрация", style = MaterialTheme.typography.h5)

        Spacer(Modifier.height(16.dp))

        if (!isLogin) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Имя пользователя") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email или имя пользователя") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        if (isLogin) {
                            val response = apiService.login(
                                username = email,
                                password = password
                            )
                            onAuthSuccess(response.access_token)
                        } else {
                            apiService.register(
                                UserCreate(
                                    email = email,
                                    password = password,
                                    username = username.takeIf { it.isNotBlank() }
                                )
                            )
                            // После регистрации можно сразу логиниться или показывать сообщение
                            val response = apiService.login(
                                username = email,
                                password = password
                            )
                            onAuthSuccess(response.access_token)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Ошибка: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colors.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (isLogin) "Войти" else "Зарегистрироваться")
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = {
            isLogin = !isLogin
            errorMessage = null
        }) {
            Text(if (isLogin) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
        }
    }
}
