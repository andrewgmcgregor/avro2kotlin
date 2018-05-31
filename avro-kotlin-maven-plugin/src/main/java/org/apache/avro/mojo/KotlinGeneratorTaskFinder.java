package org.apache.avro.mojo;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KotlinGeneratorTaskFinder {

    @NotNull
    public static List<KotlinGeneratorTask> findAllTasks(KotlinGeneratorContext context) {
        List<KotlinGeneratorTask> tasks = new ArrayList<>();

        if (!context.hasSourceDir() && !context.hasTestDir()) {
            throw new RuntimeException("neither sourceDirectory: "
                    + context.getSourceDirectory() + " or testSourceDirectory: " + context.getTestSourceDirectory()
                    + " are directories");
        }

        if (context.hasImports()) {
            for (String importedFile : context.getImports()) {
                File file = new File(importedFile);
                if (file.isDirectory()) {
                    String[] includedFiles = getIncludedFiles(
                            context,
                            file.getAbsolutePath(),
                            context.getExcludes(),
                            context.getIncludes());
                    tasks.addAll(createTasks(
                            includedFiles,
                            file,
                            context.getOutputDirectory()));
                } else if (file.isFile()) {
                    tasks.addAll(createTasks(
                            new String[]{file.getName()},
                            file.getParentFile(),
                            context.getOutputDirectory()));
                }
            }
        }

        if (context.hasSourceDir()) {
            String[] includedFiles = getIncludedFiles(
                    context,
                    context.getSourceDirectory().getAbsolutePath(),
                    context.getExcludes(),
                    context.getIncludes());
            tasks.addAll(createTasks(
                    includedFiles,
                    context.getSourceDirectory(),
                    context.getOutputDirectory()));
        }

        if (context.hasTestDir()) {
            String[] includedFiles = getIncludedFiles(
                    context,
                    context.getTestSourceDirectory().getAbsolutePath(),
                    context.getTestExcludes(),
                    context.getTestIncludes());
            tasks.addAll(createTasks(
                    includedFiles,
                    context.getTestSourceDirectory(),
                    context.getTestOutputDirectory()));
        }
        return tasks;
    }

    public static List<KotlinGeneratorTask> createTasks(String[] filenames,
                                                        File sourceDirectory,
                                                        File outputDirectory) {
        return Arrays.stream(filenames)
                .map(filename -> new KotlinGeneratorTask(filename, sourceDirectory, outputDirectory))
                .collect(Collectors.toList());
    }

    private static String[] getIncludedFiles(KotlinGeneratorContext context,
                                             String absPath,
                                             String[] excludes,
                                             String[] includes) {
        FileSetManager fileSetManager = new FileSetManager();
        FileSet fs = new FileSet();
        fs.setDirectory(absPath);
        fs.setFollowSymlinks(false);

        //exclude imports directory since it has already been compiled.
        if (context.getImports() != null) {
            String importExclude = null;

            for (String importFile : context.getImports()) {
                File file = new File(importFile);

                if (file.isDirectory()) {
                    importExclude = file.getName() + "/**";
                } else if (file.isFile()) {
                    importExclude = "**/" + file.getName();
                }

                fs.addExclude(importExclude);
            }
        }
        for (String include : includes) {
            fs.addInclude(include);
        }
        for (String exclude : excludes) {
            fs.addExclude(exclude);
        }
        return fileSetManager.getIncludedFiles(fs);
    }
}
