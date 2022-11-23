package com.shankhadeepghoshal.kotlinfinaltask.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.shankhadeepghoshal.kotlinfinaltask.pojo.DogDto
import com.shankhadeepghoshal.kotlinfinaltask.repository.ICustomDogRepository
import com.shankhadeepghoshal.kotlinfinaltask.service.RestService
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

class DogDeserializer : JsonDeserializer<List<DogDto>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<DogDto> =
        p.readValueAsTree<JsonNode>()
            .get("message")
            .fields()
            .asSequence()
            .map { entry ->
                DogDto(
                    entry.key,
                    if (null == entry.value) {
                        emptyList()
                    } else {
                        entry.value.asSequence().map { it.toString() }.toList()
                    }
                )
            }
            .toList()
}

@Configuration
class ObjectMapperConfig {
    @Bean
    @Order(-2)
    fun objMapper(): ObjectMapper =
        ObjectMapper().registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        ).configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
}

@Configuration
@EnableR2dbcRepositories
class DbConfig {
    @Bean
    fun startDb(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        initializer.setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("schema.sql")))

        return initializer
    }
}

@Configuration
class StartupConfig(
    private val restService: RestService,
    private val customDogeRepo: ICustomDogRepository
) {
    @Bean
    fun insertDataIntoDogTableOnRepoStart() {
        runBlocking { runBlocking { restService.getAllDogs().dogDto }?.let { customDogeRepo.insertAllDogs(it) } }
    }
}
