package org.apache.avro.mojo;

import main.DataClassConverterGenerator;
import main.DataClassGenerator;
import main.SkinnyAvroFileSpec;
import main.SkinnySchemaSpecBuilder;

import java.io.File;

public class KotlinGenerator {

    public static void generate(String filename, File sourceDirectory, File outputDirectory) {
        String inputFile = sourceDirectory.getAbsolutePath() + "/" + filename;
        SkinnyAvroFileSpec skinnyAvroFileSpec = SkinnySchemaSpecBuilder.INSTANCE.generateFromFile(inputFile);

        DataClassGenerator.INSTANCE
                .generateFrom(skinnyAvroFileSpec)
                .writeFileRelativeTo(outputDirectory);

        DataClassConverterGenerator.INSTANCE
                .generateFrom(skinnyAvroFileSpec)
                .writeFileRelativeTo(outputDirectory);
    }

}
