package com.example.demo

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/forecast")
class WeatherController(private val weatherService: WeatherService) {

    @GetMapping
    fun getWeather(city: String): Map<String, Any?> {
        val forecast = weatherService.getForecast(city)
        val currentWeather = weatherService.getCurrentWeather(city)

        val currentItem = forecast.list.firstOrNull() ?: throw RuntimeException("No forecast data")

        val current = linkedMapOf<String, Any?>(
            "temperature" to currentItem.main.temp.toInt(),
            "feels_like" to currentItem.main.feels_like.toInt(),
            "condition" to (currentItem.weather.firstOrNull()?.main?.lowercase() ?: "unknown"),
            "humidity" to currentItem.main.humidity,
            "wind" to currentItem.wind.speed,
            "sunrise" to unixToTime(currentWeather.sys.sunrise),
            "sunset" to unixToTime(currentWeather.sys.sunset)
        )

        val forecastByDay = forecast.list.groupBy { unixToDay(it.dt) }
            .map { (day, items) ->
                val avgTemp = items.map { it.main.temp }.average().toInt()
                val condition = items.firstOrNull()?.weather?.firstOrNull()?.main?.lowercase() ?: "sun"
                linkedMapOf(
                    "day" to day,
                    "temp" to avgTemp,
                    "condition" to condition
                )
            }.take(5)

        return linkedMapOf(
            "city" to forecast.city.name,
            "temperature" to current["temperature"],
            "feels_like" to current["feels_like"],
            "condition" to current["condition"],
            "humidity" to current["humidity"],
            "wind" to current["wind"],
            "sunrise" to current["sunrise"],
            "sunset" to current["sunset"],
            "forecast" to forecastByDay
        )
    }

    private fun unixToDay(unix: Long): String {
        val instant = java.time.Instant.ofEpochSecond(unix)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        return zoned.dayOfWeek.name.take(3)
    }

    private fun unixToTime(unix: Long): String {
        val instant = java.time.Instant.ofEpochSecond(unix)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        return zoned.toLocalTime().toString()
    }
}
