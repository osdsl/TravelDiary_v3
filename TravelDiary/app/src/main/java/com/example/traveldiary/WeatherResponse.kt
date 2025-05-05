package com.example.traveldiary

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String // Название города
)

data class Main(
    val temp: Double // Температура
)

data class Weather(
    val description: String, // Описание (например, "облачно")
    val icon: String // Идентификатор иконки (например, "04d")
)
