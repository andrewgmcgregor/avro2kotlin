package main

import demo.*
import demo.converter.ExampleConverter
import demo.converter.ExamplePersonConverter
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
                Example(42, falseNesting, ExampleEnum.FOO, falseNesting, "user1"),
                Example(99, trueNesting, ExampleEnum.BAR, trueNesting, null),
                Example(99, trueNesting, ExampleEnum.BAZ, null, null))

        examples.forEach { assertThat(convertExampleThereAndBackAgain(it), `is`(it)) }
    }

    private fun convertExamplePersonThereAndBackAgain(examplePerson: ExamplePerson): ExamplePerson {
        val kotlinExamplePerson = ExamplePersonConverter.fromAvroSpecificRecord(examplePerson)
        val reconstitutedExamplePerson = ExamplePersonConverter.toAvroSpecificRecord(kotlinExamplePerson)
        return reconstitutedExamplePerson
    }

    private fun convertExampleThereAndBackAgain(example: Example): Example {
        val kotlinObject = ExampleConverter.fromAvroSpecificRecord(example)
        val javaObject = ExampleConverter.toAvroSpecificRecord(kotlinObject)
        return javaObject
    }
}