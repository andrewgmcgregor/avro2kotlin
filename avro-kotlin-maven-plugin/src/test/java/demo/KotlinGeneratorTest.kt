package demo

import main.KotlinGenerator
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class KotlinGeneratorTest {

    @Test
    fun shouldSupportEnums() {
        val testProtocol = """
@namespace("demo")
protocol ExampleInterface {
  enum TestEnum { FOO, BAR }
  record ExampleNesting {
    TestEnum testEnum;
  }
}
        """
        val generatedCodeCollector = StringBuilder()
        KotlinGenerator.generateFromAvdl(testProtocol.byteInputStream()).writeTo(generatedCodeCollector)
        val generatedCode = generatedCodeCollector.toString()
        assertThat(generatedCode, containsString("testEnum"))
        assertThat(generatedCode, containsString("fun toAvroSpecificRecord() = TestEnum"))
        assertThat(generatedCode, containsString("fun fromAvroSpecificRecord(testEnum: TestEnum)"))
    }

}