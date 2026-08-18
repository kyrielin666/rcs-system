package com.rcs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RcsSystemApplication

fun main(args: Array<String>) {
	runApplication<RcsSystemApplication>(*args)
}
