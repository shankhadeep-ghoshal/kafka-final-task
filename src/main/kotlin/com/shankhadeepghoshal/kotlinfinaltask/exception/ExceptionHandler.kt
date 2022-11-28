package com.shankhadeepghoshal.kotlinfinaltask.exception

import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono


@Component
class GlobalErrorAttrs : DefaultErrorAttributes() {
    override fun getErrorAttributes(
        request: ServerRequest?,
        options: ErrorAttributeOptions?
    ): MutableMap<String, Any> {
        val map = super.getErrorAttributes(request, options)
        map["status"] = HttpStatus.NOT_FOUND
        map["error"] = "Not Found"
        map["message"] = "Dog breed with given name not found"

        return map
    }
}

@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    errorAttrs: GlobalErrorAttrs,
    applicationContext: ApplicationContext?,
    codecConfig: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(
    errorAttrs, WebProperties.Resources(), applicationContext
) {

    init {
        super.setMessageReaders(codecConfig.readers)
        super.setMessageWriters(codecConfig.writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes?): RouterFunction<ServerResponse> =
        RouterFunctions.route(
            RequestPredicates.GET("/*/url"), this::renderErrorResponse
        )

    fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults())
        return ServerResponse.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorPropertiesMap))
    }
}
