package com.shankhadeepghoshal.kotlinfinaltask.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.shankhadeepghoshal.kotlinfinaltask.exception.DogNotFoundException
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogApiResponseGetImages
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogsFetchApiResponse
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Url
import com.shankhadeepghoshal.kotlinfinaltask.repository.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody


@Service
class DogsApiService(
    private val dogRepo: DogRepository,
    private val urlRepository: UrlRepository,
    private val restService: RestService,
    private val objectMapper: ObjectMapper
) {
    private val log = KotlinLogging.logger {}
    fun findAllDogs() = dogRepo.findAll()

    @Throws(DogNotFoundException::class)
    @Transactional
    suspend fun findDogsUrl(dogName: String): List<Url>? {
        var urlsByDogNameFromDb = urlRepository.findUrlsGivenDogName(dogName)

        if (urlsByDogNameFromDb.isNullOrEmpty()) {
            val urlsByDogNameFromApi = restService.getUrlsDogName(dogName).message
            if (urlsByDogNameFromApi.isNullOrEmpty()) {
                log.error { "Url not found for $dogName" }
                throw DogNotFoundException("Doge with given name not found")
            } else {
                val dogId = dogRepo.findIdsGivenBreedMainName(dogName)
                urlsByDogNameFromApi.forEach { urlRepository.insertUrlForDogId(dogId, it) }
                urlsByDogNameFromDb = urlRepository.findUrlsGivenDogName(dogName)
                log.info { "Id found for $dogName is $dogId" }
            }
        }

        return urlsByDogNameFromDb
    }

    @Transactional
    suspend fun findAllDogsAndPersist() {
        if (!dogRepo.existsById(1)) {
            restService.getAllDogs()
                .message
                .map { entry ->  Dog(name = entry.key)}
                .forEach { dogRepo.save(it) }
        }
    }
}

@Service
class RestService {
    companion object {
        private val IMAGE_URL_BY_BREED_MAIN_NAME: (String) -> String =
            { breedName -> "https://dog.ceo/api/breed/${breedName}/images" }

        private const val GET_ALL_DOG_BREED_URL = "https://dog.ceo/api/breeds/list/all"
    }

    suspend fun getUrlsDogName(@PathVariable("breedName") breedName: String) =
        WebClient.create()
            .get()
            .uri(IMAGE_URL_BY_BREED_MAIN_NAME.invoke(breedName))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<DogApiResponseGetImages>()

    suspend fun getAllDogs() =
        WebClient.create()
            .get()
            .uri(GET_ALL_DOG_BREED_URL)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<DogsFetchApiResponse>()
}
