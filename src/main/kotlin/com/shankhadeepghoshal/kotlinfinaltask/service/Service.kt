package com.shankhadeepghoshal.kotlinfinaltask.service

import com.shankhadeepghoshal.kotlinfinaltask.exception.DogNotFoundException
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogApiResponseGetImages
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogApiResponseListAll
import com.shankhadeepghoshal.kotlinfinaltask.repository.GET_ALL_DOG_BREED_URL
import com.shankhadeepghoshal.kotlinfinaltask.repository.ICustomDogRepository
import com.shankhadeepghoshal.kotlinfinaltask.repository.IDogRepository
import com.shankhadeepghoshal.kotlinfinaltask.repository.IMAGE_URL_BY_BREED_MAIN_NAME
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

interface DogsApiService {
    suspend fun finaAllDogs(): Flow<Dog>
    suspend fun findDogsUrl(dogBreedName: String): List<String>
}

@Service
class DogsApiServiceImpl(
    private val customDogRepo: ICustomDogRepository,
    private val crudDogRepo: IDogRepository,
    private val restService: RestService
) : DogsApiService {
    override suspend fun finaAllDogs() = crudDogRepo.findAll()
    override suspend fun findDogsUrl(dogBreedName: String): List<String> {
        var urlsByDogName = customDogRepo.findUrlsGivenBreedMainName(dogBreedName)

        if (urlsByDogName.isNullOrEmpty()) {
            urlsByDogName = restService.getUrlOfGivenBreedByMainName(dogBreedName).message
            if (urlsByDogName.isEmpty()) {
                throw DogNotFoundException("Doge with given name not found")
            }
            val dogId = customDogRepo.findIdsGivenBreedMainName(dogBreedName)
                ?: throw DogNotFoundException("Doge with given name not found")
            customDogRepo.insertUrlForGivenDogId(dogId, urlsByDogName)
        }

        return urlsByDogName
    }
}

@Service
class RestService {
    suspend fun getUrlOfGivenBreedByMainName(@PathVariable("breedName") breedName: String) =
        WebClient.create().get()
            .uri(IMAGE_URL_BY_BREED_MAIN_NAME.invoke(breedName))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<DogApiResponseGetImages>()

    suspend fun getAllDogs(): DogApiResponseListAll =
        WebClient.create().get()
            .uri(GET_ALL_DOG_BREED_URL)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody()
}