package demo

import kotlin.Long
import kotlin.String

data class ExampleKt(
        val id: Long,
        val exampleNesting: ExampleNestingKt,
        val my_nested_member: ExampleNestingKt?,
        val guid: String?
) {
    fun toAvroSpecificRecord() = Example(id, exampleNesting.toAvroSpecificRecord(), my_nested_member?.toAvroSpecificRecord(), guid)
    companion object ExampleKt {
        fun fromAvroSpecificRecord(example: Example) = ExampleKt(id = example.id, exampleNesting = demo.ExampleNestingKt.fromAvroSpecificRecord(example.exampleNesting), my_nested_member = if (example.my_nested_member == null) null else demo.ExampleNestingKt.fromAvroSpecificRecord(example.my_nested_member), guid = if (example.guid == null) null else example.guid)
    }
}
