package main

import demo.Example
import demo.ExampleNesting
import demo.ExamplePerson
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class IntegrationTest {

    @Test
    fun avsc() {
        var examplePerson1 = ExamplePerson(42, "user1")
        assertThat(convertExamplePersonThereAndBackAgain(examplePerson1), `is`(examplePerson1))

        val examplePersonWithNull = ExamplePerson(99, null)
        assertThat(convertExamplePersonThereAndBackAgain(examplePersonWithNull), `is`(examplePersonWithNull))
    }

    @Test
    fun avdl() {
        val falseNesting = ExampleNesting(false)
        val trueNesting = ExampleNesting(true)
        val examples = listOf(
                Example(42, falseNesting, falseNesting, "user1"),
                Example(99, trueNesting, trueNesting, null),
                Example(99, trueNesting, null, null))

        examples.forEach { assertThat(convertExampleThereAndBackAgain(it), `is`(it)) }
    }

    private fun convertExamplePersonThereAndBackAgain(examplePerson: ExamplePerson): ExamplePerson {
        val kotlinExamplePerson = demo.ExamplePersonKt.fromAvroSpecificRecord(examplePerson)
        val reconstitutedExamplePerson = kotlinExamplePerson.toAvroSpecificRecord()
        return reconstitutedExamplePerson
    }

    private fun convertExampleThereAndBackAgain(example: Example): Example {
        val kotlinObject = demo.ExampleKt.fromAvroSpecificRecord(example)
        val javaObject = kotlinObject.toAvroSpecificRecord()
        return javaObject
    }
}