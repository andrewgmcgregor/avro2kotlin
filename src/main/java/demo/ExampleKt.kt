package demo

import kotlin.Long
import kotlin.String

data class ExampleKt(val id: Long, val guid: String?) {
    fun toAvroSpecificRecord() = Example(id, guid)
    companion object ExampleKt {
        fun fromAvroSpecificRecord(example: Example) = ExampleKt(id = example.id, guid = example.guid)
    }
}
