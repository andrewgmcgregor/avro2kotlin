package net.avro2kotlin.mojo;

import java.io.File;

public class KotlinGeneratorTask {
    public final String filename;
    public final File sourceDirectory;
    public final File outputDirectory;

    public KotlinGeneratorTask(String filename, File sourceDirectory, File outputDirectory) {
        this.filename = filename;
        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
    }
}
