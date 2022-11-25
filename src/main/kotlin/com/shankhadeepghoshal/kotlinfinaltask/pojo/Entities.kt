package com.shankhadeepghoshal.kotlinfinaltask.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "dog")
data class Dog(
    @Id val id: Int? = null,
    @Column("name") val name: String
)

@Table(name = "urls")
data class Url(
    @Id val id: Int? = null,
    @Column("dog_id") val dogId: Int,
    @Column("url") val url: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DogApiResponseGetImages(val message: List<String>?)
