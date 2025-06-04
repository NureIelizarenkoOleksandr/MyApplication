package com.example.myapplication

data class Schedule(val id: Int, val vehicle_id: Int, val departure_time: String, val arrival_time: String)

data class Route(
    val id: Int,
    val name: String,
    val schedules: List<Schedule>,
    val route_number: String? = null,
    val distance: Double? = null,
    val average_delay_minutes: Double? = null
)

data class RouteResponse(val items: List<Route>)


