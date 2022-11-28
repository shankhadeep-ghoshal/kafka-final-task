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
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class DogsApiServiceUnitTest {
    private val url = "http://www.foo.com/labrador"

    @MockK
    lateinit var dogRepo: DogRepository

    @MockK
    lateinit var urlRepository: UrlRepository

    @MockK
    lateinit var restService: RestService

    lateinit var dogsService: DogsApiService

    @BeforeEach
    fun setUp() {
        val objectMapper = ObjectMapper()
        dogsService = DogsApiService(dogRepo, urlRepository, restService, objectMapper)
    }

    @Test
    fun `test find all dogs`() = runTest {
        val dogOfInterest = Dog(1, "Labrador")

        every { dogRepo.findAll() } returns flowOf(dogOfInterest)
        val dogReturned = dogsService.findAllDogs().last()

        Assertions.assertEquals(dogOfInterest, dogReturned)
    }

    @Test
    fun `test throw exception when dog not found`() = runTest {
        val nonExistentDogName = "404 Dog"

        coEvery { urlRepository.findUrlsGivenDogName(nonExistentDogName) } returns
                emptyList()
        coEvery { restService.getUrlsDogName(nonExistentDogName) } returns
                DogApiResponseGetImages(emptyList())

        assertThrows<DogNotFoundException> {
            dogsService.findDogsUrl(nonExistentDogName)
        }
    }

    @Test
    fun `test get urls for given dog name from api`() = runTest {
        val dogExists = "Labrador"

        coEvery { urlRepository.findUrlsGivenDogName(dogExists) } returns emptyList()
        coEvery { dogRepo.findIdsGivenBreedMainName(dogExists) } returns 0
        coEvery { urlRepository.insertUrlForDogId(0, url) } returns Unit
        coEvery { urlRepository.findUrlsGivenDogName(dogExists) } returns listOf(
            Url(
                url = url,
                dogId = 1
            )
        )
        coEvery { restService.getUrlsDogName(dogExists) } returns
                DogApiResponseGetImages(listOf(url))

        val urlsObtained = dogsService.findDogsUrl(dogExists)

        Assertions.assertNotNull(urlsObtained)
        if (urlsObtained != null) {
            Assertions.assertEquals(url, urlsObtained.toTypedArray()[0].url)
        }
    }
}
