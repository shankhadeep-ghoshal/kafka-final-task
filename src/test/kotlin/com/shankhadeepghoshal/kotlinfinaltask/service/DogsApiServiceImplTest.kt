package com.shankhadeepghoshal.kotlinfinaltask.service

import com.shankhadeepghoshal.kotlinfinaltask.exception.DogNotFoundException
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogApiResponseGetImages
import com.shankhadeepghoshal.kotlinfinaltask.repository.ICustomDogRepository
import com.shankhadeepghoshal.kotlinfinaltask.repository.IDogRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@MockKExtension.KeepMocks
internal class DogsApiServiceImplTest {
    private val URLS = listOf("http://www.foo.com/labrador")

    @MockK
    lateinit var customDogRepo: ICustomDogRepository

    @MockK
    lateinit var crudDogRepo: IDogRepository

    @MockK
    lateinit var restService: RestService

    @InjectMockKs
    lateinit var apiServiceImpl: DogsApiServiceImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `test find all dogs`() {
        val dogOfInterest = Dog(1, "Labrador", "Retriever")
        coEvery { crudDogRepo.findAll() } returns flowOf(dogOfInterest)
        val dogReturned = runBlocking {
            val finaAllDogs = apiServiceImpl.finaAllDogs().last()
            finaAllDogs
        }
        Assertions.assertEquals(dogOfInterest, dogReturned)
    }

    @Test
    fun `test throw exception when dog not found`() {
        val nonExistentDogName = "404 Dog"
        coEvery { customDogRepo.findUrlsGivenBreedMainName(nonExistentDogName) } returns emptyList()
        coEvery { restService.getUrlOfGivenBreedByMainName(nonExistentDogName) } returns DogApiResponseGetImages(
            emptyList()
        )
        Assertions.assertThrows(DogNotFoundException::class.java) {
            runBlocking {
                apiServiceImpl.findDogsUrl(nonExistentDogName)
            }
        }
    }

    @Test
    fun `test get urls for given dog name from api`() {
        val dogExists = "Labrador"
        coEvery { customDogRepo.findUrlsGivenBreedMainName(dogExists) } returns emptyList()
        coEvery { customDogRepo.findIdsGivenBreedMainName(dogExists) } returns 0
        coEvery { customDogRepo.insertUrlForGivenDogId(0, URLS) } returns 1
        coEvery { restService.getUrlOfGivenBreedByMainName(dogExists) } returns DogApiResponseGetImages(URLS)

        val urlsObtained = runBlocking {
            apiServiceImpl.findDogsUrl(dogExists)
        }

        Assertions.assertArrayEquals(URLS.toTypedArray(), urlsObtained.toTypedArray())
    }
}