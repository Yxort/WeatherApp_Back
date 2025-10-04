package com.example.demo

import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: MainWeather,
    val weather: List<WeatherCondition>,
    val wind: Wind
)

data class MainWeather(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class WeatherCondition(
    val main: String
)

data class Wind(
    val speed: Double
)

data class City(
    val name: String
)

@Service
class WeatherService {
    private val apiKey = "762fc2d75d1c2b82065519d8f831af23"
    private val client = WebClient.create("https://api.openweathermap.org/data/2.5")

    fun getForecast(city: String): ForecastResponse {
        return client.get()
            .uri { uriBuilder ->
                uriBuilder.path("/forecast")
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .build()
            }
            .retrieve()
            .bodyToMono(ForecastResponse::class.java)
            .block()!!
    }

    fun getCurrentWeather(city: String): CurrentWeatherResponse {
        return client.get()
            .uri { uriBuilder ->
                uriBuilder.path("/weather")
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .build()
            }
            .retrieve()
            .bodyToMono(CurrentWeatherResponse::class.java)
            .block()!!
    }
}

// DTO для /weather
data class CurrentWeatherResponse(
    val sys: Sys,
)

data class Sys(
    val sunrise: Long,
    val sunset: Long
)
