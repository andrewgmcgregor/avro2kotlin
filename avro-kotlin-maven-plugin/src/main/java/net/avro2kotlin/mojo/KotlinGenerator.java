package net.avro2kotlin.mojo;

import net.avro2kotlin.compiler.DataClassConverterGenerator;
import net.avro2kotlin.compiler.DataClassGenerator;
import net.avro2kotlin.compiler.SkinnyAvroFileSpec;
import net.avro2kotlin.compiler.SkinnySchemaSpecBuilder;

import static net.avro2kotlin.mojo.KotlinGeneratorTaskFinder.findAllTasks;

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
