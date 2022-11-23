package com.shankhadeepghoshal.kotlinfinaltask.repository

import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogDto
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.*
import org.springframework.stereotype.Repository

// RT199627365IN

const val GET_ALL_DOG_BREED_URL = "https://dog.ceo/api/breeds/list/all"
val IMAGE_URL_BY_BREED_MAIN_NAME: (String) -> String =
    { breedName -> "https://dog.ceo/api/breed/${breedName}/images" }

const val INSERT_DOG =
    """
        INSERT INTO dogs (main_name, sub_name)
        VALUES (:main_name, :sub_name);
    """

const val SELECT_URLS_GIVEN_BREED_MAIN_NAME =
    """
        SELECT urlT.`url` as urls
        FROM `urls` urlT
        INNER JOIN `dogs` dog ON dog.id = urlT.dogs_id
        WHERE dog.main_name = :main_name;
    """

const val SELECT_IDS_GIVEN_BREED_MAIN_NAME =
    """
        SELECT `dogs`.`id` 
        FROM `dogs` 
        WHERE `dogs`.`main_name` = :main_name
        LIMIT 1;
    """

const val INSERT_URLS_FOR_DOG_MAIN_NAME =
    """
        INSERT INTO urls (url, dogs_id)
        VALUES (:url, :dog_id)
    """

interface ICustomDogRepository {
    suspend fun insertAllDogs(dogsData: List<DogDto>): Int
    suspend fun findUrlsGivenBreedMainName(dogName: String): List<String>?
    suspend fun findIdsGivenBreedMainName(dogName: String): Int?
    suspend fun insertUrlForGivenDogId(dogId: Int?, urls: List<String>): Int
}

@Repository
interface IDogRepository : CoroutineCrudRepository<Dog, Int?>

@Repository
class DataRepository(private val client: DatabaseClient) : ICustomDogRepository {
    override suspend fun insertAllDogs(dogsData: List<DogDto>) =
        dogsData.flatMap { dog ->
            dog.subName.map { Dog(dog.name, it) }
        }.sumOf {
            client.sql(INSERT_DOG)
                .bind("main_name", it.nameMain)
                .bind("sub_name", it.subName)
                .fetch()
                .awaitRowsUpdated()
        }

    override suspend fun findUrlsGivenBreedMainName(dogName: String): List<String>? =
        client.sql(SELECT_URLS_GIVEN_BREED_MAIN_NAME)
            .bind("main_name", dogName)
            .map { row, _ -> row.get("urls", List::class.java) }
            .awaitOne()
            ?.map { it.toString() }

    override suspend fun findIdsGivenBreedMainName(dogName: String) =
        client.sql(SELECT_IDS_GIVEN_BREED_MAIN_NAME)
            .bind("main_name", dogName)
            .map { row, _ -> row.get("id", Int::class.java) }
            .awaitSingle()

    override suspend fun insertUrlForGivenDogId(dogId: Int?, urls: List<String>) =
        urls.sumOf {
            client.sql(INSERT_URLS_FOR_DOG_MAIN_NAME)
                .bind(":url", it)
                .bind(":dog_id", dogId)
                .fetch()
                .awaitRowsUpdated()
        }
}

