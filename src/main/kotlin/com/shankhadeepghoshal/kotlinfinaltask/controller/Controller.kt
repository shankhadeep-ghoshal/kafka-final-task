package com.shankhadeepghoshal.kotlinfinaltask.controller

import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.service.DogsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(private val dogsApiService: DogsApiService) {
    @GetMapping(path = ["/dogs"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    suspend fun getAllDogs(): Flow<Dog> {
        return dogsApiService.finaAllDogs()
    }

    @GetMapping(path = ["/{breedName}/url"], produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun getUrlsByMainBreedName(@PathVariable breedName: String): Flow<String> {
        return dogsApiService.findDogsUrl(breedName).asFlow()
    }
}
