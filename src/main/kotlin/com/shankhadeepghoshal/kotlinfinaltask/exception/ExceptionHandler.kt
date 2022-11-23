package com.shankhadeepghoshal.kotlinfinaltask.exception

import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/*

class GlobalErrorAttrs: DefaultErrorAttributes() {
    override fun getErrorAttributes(request: ServerRequest?, options: ErrorAttributeOptions?): MutableMap<String, Any> {
        val map = super.getErrorAttributes(request, options)
        map["status"] = HttpStatus.NOT_FOUND
        map["message"] = "Dog breed with given name not found"
        return map
    }
}

@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(errorAttributes: ErrorAttributes?, resources: WebProperties.Resources?,
                                     applicationContext: ApplicationContext?
) : AbstractErrorWebExceptionHandler(
    errorAttributes, resources, applicationContext
) {
    override fun getRoutingFunction(errorAttributes: ErrorAttributes?): RouterFunction<ServerResponse> = RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse)

    fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults())
        return ServerResponse.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorPropertiesMap))
    }
}*/

@Order(2)
@RestControllerAdvice
class GlobalControllerAdvice {
    @ExceptionHandler(value = [DogNotFoundException::class])
    fun handleDogNotFoundException(ex: DogNotFoundException, req: ServerHttpRequest): ResponseEntity<String> =
        ResponseEntity.notFound().build();
}
