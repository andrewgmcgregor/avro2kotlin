package org.apache.avro.mojo;

import main.DataClassConverterGenerator;
import main.DataClassGenerator;
import main.SkinnyAvroFileSpec;
import main.SkinnySchemaSpecBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Generate Kotlin classes and interfaces
 *
 * @goal kotlin-generator
 * @requiresDependencyResolution runtime
 * @phase generate-sources
 * @threadSafe
 */
public class KotlinGeneratorMojo extends AbstractAvroMojo {
    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.avdl</code> is used to select IDL files.
     *
     * @parameter
     */
    private String[] includes = new String[]{"**/*.avdl", "**/*.avsc"};

    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.avdl</co  de> is used to select IDL files.
     *
     * @parameter
     */
    private String[] testIncludes = new String[]{"**/*.avdl", "**/*.avsc"};

    @Override
    protected void doCompile(String filename, File sourceDirectory, File outputDirectory) throws IOException {
        String inputFile = sourceDirectory.getAbsolutePath() + "/" + filename;
        SkinnyAvroFileSpec skinnyAvroFileSpec = SkinnySchemaSpecBuilder.INSTANCE.generateFromFile(inputFile);

        DataClassGenerator.INSTANCE
                .generateFrom(skinnyAvroFileSpec)
                .writeFileRelativeTo(outputDirectory);

        DataClassConverterGenerator.INSTANCE
                .generateFrom(skinnyAvroFileSpec)
                .writeFileRelativeTo(outputDirectory);
    }

    @Override
    protected String[] getIncludes() {
        return includes;
    }


    @Override
    protected String[] getTestIncludes() {
        return testIncludes;
    }
}
