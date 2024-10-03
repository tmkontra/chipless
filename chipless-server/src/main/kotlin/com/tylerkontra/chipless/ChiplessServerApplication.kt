package com.tylerkontra.chipless

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChiplessServerApplication

fun main(args: Array<String>) {
    runApplication<ChiplessServerApplication>(*args)
}
