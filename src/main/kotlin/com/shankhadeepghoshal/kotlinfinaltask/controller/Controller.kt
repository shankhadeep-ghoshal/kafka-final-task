package com.shankhadeepghoshal.kotlinfinaltask.controller

import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Url
import com.shankhadeepghoshal.kotlinfinaltask.service.DogsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(private val dogsApiService: DogsApiService) {
    private val log = KotlinLogging.logger {}

    @GetMapping(path = ["/dogs"], produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun getAllDogs(): Flow<Dog> {
        log.info { "Request incoming for fetching all dogs" }
        return dogsApiService.findAllDogs()
    }

    @GetMapping(path = ["/{breedName}/url"], produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun getUrlsByMainBreedName(@PathVariable breedName: String): Flow<Url>? {
        log.info { "Request incoming for fetching URLs of breed: $breedName" }
        return dogsApiService.findDogsUrl(breedName)?.asFlow()
    }
}
