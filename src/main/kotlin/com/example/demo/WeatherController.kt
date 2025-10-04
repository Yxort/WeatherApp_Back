package com.example.demo

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/forecast")
class WeatherController(private val weatherService: WeatherService) {

    @GetMapping
    fun getWeather(city: String): Map<String, Any?> {
        val raw = weatherService.getWeather(city)

        // Берём первый элемент как "текущую погоду"
        val currentItem = raw.list.firstOrNull() ?: throw RuntimeException("No forecast data")

        val current = linkedMapOf<String, Any?>(
            "temperature" to currentItem.main.temp.toInt(),
            "feels_like" to currentItem.main.feels_like.toInt(),
            "condition" to (currentItem.weather.firstOrNull()?.main?.lowercase() ?: "unknown"),
            "humidity" to currentItem.main.humidity,
            "wind" to currentItem.wind.speed,
            "sunrise" to "-", // Если нет sunrise в /forecast, оставляем "-"
            "sunset" to "-"   // Если нет sunset в /forecast, оставляем "-"
        )

        // Группируем прогноз по дням (максимум 5 дней)
        val forecastByDay = raw.list.groupBy { unixToDay(it.dt) }
            .map { (day, items) ->
                val avgTemp = items.map { it.main.temp }.average().toInt()
                val condition = items.firstOrNull()?.weather?.firstOrNull()?.main?.lowercase() ?: "sun"
                linkedMapOf(
                    "day" to day,
                    "temp" to avgTemp,
                    "condition" to condition
                )
            }.take(5)

        // Собираем финальный JSON с сохранением порядка
        val result = linkedMapOf<String, Any?>(
            "city" to raw.city.name,
            "temperature" to current["temperature"],
            "feels_like" to current["feels_like"],
            "condition" to current["condition"],
            "humidity" to current["humidity"],
            "wind" to current["wind"],
            "sunrise" to current["sunrise"],
            "sunset" to current["sunset"],
            "forecast" to forecastByDay
        )

        return result
    }

    private fun unixToDay(unix: Long): String {
        val instant = java.time.Instant.ofEpochSecond(unix)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        return zoned.dayOfWeek.name.take(3)
    }
}
