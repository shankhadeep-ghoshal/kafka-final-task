package com.shankhadeepghoshal.kotlinfinaltask.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.shankhadeepghoshal.kotlinfinaltask.exception.DogNotFoundException
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogApiResponseGetImages
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Url
import com.shankhadeepghoshal.kotlinfinaltask.repository.DogRepository
import com.shankhadeepghoshal.kotlinfinaltask.repository.UrlRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class DogsApiServiceUnitTest {
    private val URL = "http://www.foo.com/labrador"

    @MockK
    lateinit var dogRepo: DogRepository

    @MockK
    lateinit var urlRepository: UrlRepository

    @MockK
    lateinit var restService: RestService

    @MockK
    lateinit var objectMapper: ObjectMapper

    @InjectMockKs
    lateinit var apiService: DogsApiServiceImpl

    @Test
    fun `test find all dogs`() = runTest {
        val dogOfInterest = Dog(1, "Labrador")

        every { dogRepo.findAll() } returns flowOf(dogOfInterest)
        val dogReturned = apiService.findAllDogs().last()

        Assertions.assertEquals(dogOfInterest, dogReturned)
    }

    @Test
    fun `test throw exception when dog not found`() = runTest {
        val nonExistentDogName = "404 Dog"

        coEvery { urlRepository.findUrlsGivenDogName(nonExistentDogName) } returns
                emptyList()
        coEvery { restService.getUrlsDogName(nonExistentDogName) } returns
                DogApiResponseGetImages(emptyList())

        Assertions.assertThrows(DogNotFoundException::class.java) {
            runBlocking {
                apiService.findDogsUrl(nonExistentDogName)
            }
        }
    }

    @Test
    fun `test get urls for given dog name from api`() = runTest {
        val dogExists = "Labrador"

        coEvery { urlRepository.findUrlsGivenDogName(dogExists) } returns emptyList()
        coEvery { dogRepo.findIdsGivenBreedMainName(dogExists) } returns 0
        coEvery { urlRepository.insertUrlForDogId(0, URL) } returns Unit
        coEvery { urlRepository.findUrlsGivenDogName(dogExists) } returns listOf(
            Url(
                url = URL,
                dogId = 1
            )
        )
        coEvery { restService.getUrlsDogName(dogExists) } returns
                DogApiResponseGetImages(listOf(URL))

        val urlsObtained = runBlocking {
            apiService.findDogsUrl(dogExists)
        }

        Assertions.assertNotNull(urlsObtained)
        if (urlsObtained != null) {
            Assertions.assertEquals(URL, urlsObtained.toTypedArray()[0].url)
        }
    }
}
