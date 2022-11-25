package com.shankhadeepghoshal.kotlinfinaltask.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.shankhadeepghoshal.kotlinfinaltask.exception.DogNotFoundException
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogApiResponseGetImages
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Url
import com.shankhadeepghoshal.kotlinfinaltask.repository.DogRepository
import com.shankhadeepghoshal.kotlinfinaltask.repository.GET_ALL_DOG_BREED_URL
import com.shankhadeepghoshal.kotlinfinaltask.repository.IMAGE_URL_BY_BREED_MAIN_NAME
import com.shankhadeepghoshal.kotlinfinaltask.repository.UrlRepository
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

interface DogsApiService {
    fun findAllDogs(): Flow<Dog>
    suspend fun findDogsUrl(dogName: String): List<Url>?
    suspend fun findAllDogsAndPersist()
}

@Service
class DogsApiServiceImpl(
    private val dogRepo: DogRepository,
    private val urlRepository: UrlRepository,
    private val restService: RestService,
    private val objectMapper: ObjectMapper
) : DogsApiService {
    private val log = KotlinLogging.logger {}
    override fun findAllDogs() = dogRepo.findAll()

    @Throws(DogNotFoundException::class)
    @Transactional
    override suspend fun findDogsUrl(dogName: String): List<Url>? {
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
    override suspend fun findAllDogsAndPersist() {
        if (dogRepo.findById(1) == null) {
            objectMapper.readTree(restService.getAllDogs())
                .get("message")
                .fields()
                .asSequence()
                .map { entry ->
                    Dog(name = entry.key)
                }.forEach { dogRepo.save(it) }
        }
    }
}

@Service
class RestService {
    suspend fun getUrlsDogName(@PathVariable("breedName") breedName: String) =
        WebClient.create()
            .get()
            .uri(IMAGE_URL_BY_BREED_MAIN_NAME.invoke(breedName))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<DogApiResponseGetImages>()

    suspend fun getAllDogs(): String {
        val retrieve = WebClient.create()
            .get()
            .uri(GET_ALL_DOG_BREED_URL)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
        val body = retrieve.awaitBody<String>()

        return body
    }
}
