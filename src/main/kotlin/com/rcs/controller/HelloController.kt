package com.rcs.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HelloController {

    @GetMapping("/hello")
    fun hello(): Map<String, Any> = mapOf(
        "code" to 0,
        "message" to "success",
        "data" to "Hello, RCS System!"
    )
}
