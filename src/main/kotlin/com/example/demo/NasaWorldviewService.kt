package com.example.demo

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry
import java.time.Duration

@Service
class NasaWorldviewService {

    private val osmClient = WebClient.builder()
        .baseUrl("https://nominatim.openstreetmap.org")
        .build()

    private val nasaClient = WebClient.create("https://wvs.earthdata.nasa.gov/api/v1")

    fun getCitySnapshot(city: String): ByteArray {
        val response = osmClient.get()
            .uri {
                it.path("/search")
                    .queryParam("q", city)
                    .queryParam("format", "json")
                    .queryParam("limit", "1")
                    .build()
            }
            .retrieve()
            .bodyToMono(List::class.java)
            .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.max(1))
            .onErrorResume {
                println("⚠️ OSM Connect error: ${it.message}")
                null
            }
            .block() ?: throw RuntimeException("Coord error")

        val first = (response.firstOrNull() as? Map<*, *>) ?: throw RuntimeException("City not found")
        val lat = first["lat"].toString().toDouble()
        val lon = first["lon"].toString().toDouble()
        println("${lat}, ${lon}");

        val delta = 10
        val bbox = "${lat - delta},${lon - delta},${lat + delta},${lon + delta}"

        return nasaClient.get()
            .uri {
                it.path("/snapshot")
                    .queryParam("REQUEST", "GetSnapshot")
                    .queryParam("TIME", "2023-9-10")
                    .queryParam("BBOX", bbox)
                    .queryParam("CRS", "EPSG:4326")
                    .queryParam("LAYERS", "MODIS_Terra_CorrectedReflectance_TrueColor")
                    .queryParam("FORMAT", "image/jpeg")
                    .queryParam("WIDTH", "1024")
                    .queryParam("HEIGHT", "512")
                    .build()
            }
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .timeout(Duration.ofSeconds(10))
            .block() ?: throw RuntimeException("Nasa image error")
    }
}
