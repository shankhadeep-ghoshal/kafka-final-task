package com.shankhadeepghoshal.kotlinfinaltask.repository

import com.shankhadeepghoshal.kotlinfinaltask.pojo.Dog
import com.shankhadeepghoshal.kotlinfinaltask.pojo.Url
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.*
import org.springframework.stereotype.Repository

val IMAGE_URL_BY_BREED_MAIN_NAME: (String) -> String =
    { breedName -> "https://dog.ceo/api/breed/${breedName}/images" }

const val GET_ALL_DOG_BREED_URL = "https://dog.ceo/api/breeds/list/all"

const val SELECT_URLS_GIVEN_BREED_MAIN_NAME =
    """
        SELECT urls.* 
        FROM `urls` 
        INNER JOIN `dog` dog ON dog.id = urls.dog_id
        WHERE dog.name = :name;
    """

const val SELECT_IDS_GIVEN_BREED_MAIN_NAME =
    """
        SELECT `dog`.`id` 
        FROM `dog` 
        WHERE `dog`.`name` = :name
    """

const val INSERT_URLS_FOR_DOG_MAIN_NAME =
    """
        INSERT INTO urls (url, dog_id)
        VALUES (:url, :dogId)
    """

@Repository
interface DogRepository : CoroutineCrudRepository<Dog, Int> {
    @Query(SELECT_IDS_GIVEN_BREED_MAIN_NAME)
    suspend fun findIdsGivenBreedMainName(mainName: String): Int
}

@Repository
interface UrlRepository : CoroutineCrudRepository<Url, Int> {
    @Query(SELECT_URLS_GIVEN_BREED_MAIN_NAME)
    suspend fun findUrlsGivenDogName(mainName: String): List<Url>?

    @Query(INSERT_URLS_FOR_DOG_MAIN_NAME)
    suspend fun insertUrlForDogId(dogId: Int, url: String)
}
