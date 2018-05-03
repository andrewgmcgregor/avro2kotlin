package demo

import kotlin.Int
import kotlin.String

data class ExamplePersonKt(val id: Int, val username: String?) { //val
    fun toAvroSpecificRecord(): ExamplePerson {
        return ExamplePerson(id, username) //return
    }
    companion object ExamplePersonKt {
        fun fromAvroSpecificRecord(examplePerson: ExamplePerson): demo.ExamplePersonKt { //full type
            return ExamplePersonKt(id = examplePerson.id, username = examplePerson.username) //return and no !!
        }
    }
}