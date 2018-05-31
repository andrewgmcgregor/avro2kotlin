package org.apache.avro.mojo;

import main.DataClassConverterGenerator;
import main.DataClassGenerator;
import main.SkinnyAvroFileSpec;
import main.SkinnySchemaSpecBuilder;

import static org.apache.avro.mojo.KotlinGeneratorTaskFinder.findAllTasks;

public class KotlinGenerator {

    public static void generateAll(KotlinGeneratorContext context) {
        findAllTasks(context).forEach(task -> generate(context, task));
    }

    public static void generate(KotlinGeneratorContext context, KotlinGeneratorTask generatorTask) {
        String inputFile = context.getSourceDirectory().getAbsolutePath() + "/" + generatorTask.filename;
        SkinnyAvroFileSpec skinnyAvroFileSpec = SkinnySchemaSpecBuilder.INSTANCE.generateFromFile(inputFile);

        DataClassGenerator.INSTANCE
                .generateFrom(skinnyAvroFileSpec)
                .writeFileRelativeTo(generatorTask.outputDirectory);

        DataClassConverterGenerator.INSTANCE
                .generateFrom(skinnyAvroFileSpec)
                .writeFileRelativeTo(generatorTask.outputDirectory);
    }
}
