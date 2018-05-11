package demo

import main.KotlinGenerator
import org.junit.Assert.assertTrue
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
        val generatedCode = StringBuilder()
        KotlinGenerator.generateFromAvdl(testProtocol.byteInputStream()).writeTo(generatedCode)
        println("generatedCode = ${generatedCode}")
        assertTrue(generatedCode.contains("testEnum"))
    }

}