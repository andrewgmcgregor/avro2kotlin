package demo

import kotlin.Boolean

data class ExampleNestingKt(val isGood: Boolean) {
    fun toAvroSpecificRecord() = ExampleNesting(isGood)
    companion object ExampleNestingKt {
        fun fromAvroSpecificRecord(exampleNesting: ExampleNesting) = ExampleNestingKt(isGood = exampleNesting.isGood)
    }
}