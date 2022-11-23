package com.shankhadeepghoshal.kotlinfinaltask.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shankhadeepghoshal.kotlinfinaltask.config.DogDeserializer
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "dog")
data class Dog(
    @Id val id: Int? = null,
    val nameMain: String,
    val subName: String?
) {
    constructor(nameMain: String, subName: String?) : this(null, nameMain, subName)
}

@Table(name = "urls")
data class Url(
    @Id val id: Int? = null,
    val dogsId: Int,
    val url: String
)

data class DogDto(val name: String, val subName: List<String>)

data class DogApiResponseListAll(@JsonDeserialize(using = DogDeserializer::class) val dogDto: List<DogDto>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DogApiResponseGetImages(val message: List<String>)
