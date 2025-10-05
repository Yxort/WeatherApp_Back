package com.example.demo

import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType

@RestController
@RequestMapping("/api/nasa")
class NasaController(private val nasaService: NasaWorldviewService) {

    @GetMapping("/snapshot", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getSnapshot(@RequestParam city: String): ByteArray {
        return nasaService.getCitySnapshot(city)
    }
}
