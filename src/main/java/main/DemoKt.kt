package main

import demo.ExamplePerson
import demo.ExamplePersonKt

fun main(args: Array<String>) {
    println("asdf")

    val javaPerson = ExamplePerson(
            Int.MAX_VALUE, //id = 42,
            null, //username = "user1",
            null,
            null)
    println("javaPerson = ${javaPerson}")

    val kotlinPerson = ExamplePersonKt.toKotlin(examplePerson = javaPerson)
    println("kotlinPerson = ${kotlinPerson}")
}

