package demo

import kotlin.Long
import kotlin.String

data class ExampleKt(
        val id: Long,
        val exampleNesting: ExampleNestingKt,
        val guid: String?
) {
    fun toAvroSpecificRecord() = Example(id, exampleNesting.toAvroSpecificRecord(), guid)
    companion object ExampleKt {
        fun fromAvroSpecificRecord(example: Example) = ExampleKt(id = example.id, exampleNesting = demo.ExampleNestingKt.fromAvroSpecificRecord(example.exampleNesting), guid = example.guid)
    }
}
