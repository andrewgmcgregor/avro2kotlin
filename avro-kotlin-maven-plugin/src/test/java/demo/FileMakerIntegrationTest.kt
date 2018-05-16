package demo

import main.FileMaker
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

class FileMakerIntegrationTest {
    @Test
    fun shouldMakeFileRelativeToPath() {
        val tempDir = createTempDir()
        tempDir.deleteOnExit()

        FileMaker("hello/world", "HowAreYou.txt", "Hello, world!")
                .writeFileRelativeTo(tempDir)

        val lines = File("${tempDir.absolutePath}/hello/world/HowAreYou.txt").readLines()
        assertThat(lines.get(0), `is`("Hello, world!"))
    }
}