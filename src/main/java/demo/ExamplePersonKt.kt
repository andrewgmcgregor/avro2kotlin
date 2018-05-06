package demo

import kotlin.Int
import kotlin.String

data class ExamplePersonKt(val id: Int, val username: String?) {
    fun toAvroSpecificRecord() = ExamplePerson(id, username)
    companion object ExamplePersonKt {
        fun fromAvroSpecificRecord(examplePerson: ExamplePerson) = ExamplePersonKt(id = examplePerson.id, username = examplePerson.username)
    }
}
