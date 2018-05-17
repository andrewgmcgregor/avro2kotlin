//package org.apache.avro.mojo
//
//import main.DataClassConverterGenerator
//import main.DataClassGenerator
//import main.SkinnySchemaSpecBuilder
//import java.io.File
//import java.io.IOException
//
///**
// * Generate Kotlin classes and interfaces
// *
// * @goal kotlin-generator
// * @requiresDependencyResolution runtime
// * @phase generate-sources
// * @threadSafe
// */
//class KotlinGeneratorMojo : AbstractAvroMojo() {
//    /**
//     * A set of Ant-like inclusion patterns used to select files from the source
//     * directory for processing. By default, the pattern
//     * `**&#47;*.avdl` is used to select IDL files.
//     *
//     * @parameter
//     */
//    private val includes = arrayOf("**/*.avdl", "**/*.avsc")
//
//    /**
//     * A set of Ant-like inclusion patterns used to select files from the source
//     * directory for processing. By default, the pattern
//     * `**&#47;*.avdl is used to select IDL files.
//     *
//     * @parameter
//    ` */
//    private val testIncludes = arrayOf("**/*.avdl", "**/*.avsc")
//
//    @Throws(IOException::class)
//    override fun doCompile(filename: String, sourceDirectory: File, outputDirectory: File) {
//        val inputFile = sourceDirectory.absolutePath + "/" + filename
//        val skinnyAvroFileSpec = SkinnySchemaSpecBuilder.generateFromFile(inputFile)
//
//        DataClassGenerator
//                .generateFrom(skinnyAvroFileSpec)
//                .writeFileRelativeTo(outputDirectory)
//
//        DataClassConverterGenerator
//                .generateFrom(skinnyAvroFileSpec)
//                .writeFileRelativeTo(outputDirectory)
//    }
//
//    override fun getIncludes(): Array<String> {
//        return includes
//    }
//
//
//    override fun getTestIncludes(): Array<String> {
//        return testIncludes
//    }
//}
