package org.apache.avro.mojo;

import org.apache.avro.compiler.specific.SpecificCompiler;

import java.io.File;
import java.util.List;

public class KotlinGeneratorContext {
    private final String[] includes;
    private final String[] testIncludes;
    private final File sourceDirectory;
    private final File outputDirectory;
    private final File testSourceDirectory;
    private final File testOutputDirectory;
    private final SpecificCompiler.FieldVisibility fieldVisibility;
    private final String[] imports;
    private final String[] excludes;
    private final String[] testExcludes;
    private final String stringType;
    private final String templateDirectory;
    private final boolean createSetters;
    private final boolean enableDecimalLogicalType;
    private final List runtimeClasspathElements;
    private final String sourceEncoding;

    public KotlinGeneratorContext(String[] includes,
                                  String[] testIncludes,
                                  File sourceDirectory,
                                  File outputDirectory,
                                  File testSourceDirectory,
                                  File testOutputDirectory,
                                  SpecificCompiler.FieldVisibility fieldVisibility,
                                  String[] imports,
                                  String[] excludes,
                                  String[] testExcludes,
                                  String stringType,
                                  String templateDirectory,
                                  boolean createSetters,
                                  boolean enableDecimalLogicalType,
                                  List runtimeClasspathElements,
                                  String sourceEncoding) {
        this.includes = includes;
        this.testIncludes = testIncludes;
        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
        this.testSourceDirectory = testSourceDirectory;
        this.testOutputDirectory = testOutputDirectory;
        this.fieldVisibility = fieldVisibility;
        this.imports = imports;
        this.excludes = excludes;
        this.testExcludes = testExcludes;
        this.stringType = stringType;
        this.templateDirectory = templateDirectory;
        this.createSetters = createSetters;
        this.enableDecimalLogicalType = enableDecimalLogicalType;
        this.runtimeClasspathElements = runtimeClasspathElements;
        this.sourceEncoding = sourceEncoding;
    }

    public KotlinGeneratorContext copy(String[] includes, String[] testIncludes) {
        return new KotlinGeneratorContext(
                includes,
                testIncludes,
                this.sourceDirectory,
                this.outputDirectory,
                this.testSourceDirectory,
                this.testOutputDirectory,
                this.fieldVisibility,
                this.imports,
                this.excludes,
                this.testExcludes,
                this.stringType,
                this.templateDirectory,
                this.createSetters,
                this.enableDecimalLogicalType,
                this.runtimeClasspathElements,
                this.sourceEncoding);
    }

    public String[] getIncludes() {
        return includes;
    }

    public String[] getTestIncludes() {
        return testIncludes;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public File getTestSourceDirectory() {
        return testSourceDirectory;
    }

    public File getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public SpecificCompiler.FieldVisibility getFieldVisibility() {
        return fieldVisibility;
    }

    public String[] getImports() {
        return imports;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public String[] getTestExcludes() {
        return testExcludes;
    }

    public String getStringType() {
        return stringType;
    }

    public String getTemplateDirectory() {
        return templateDirectory;
    }

    public boolean isCreateSetters() {
        return createSetters;
    }

    public boolean isEnableDecimalLogicalType() {
        return enableDecimalLogicalType;
    }

    public List getRuntimeClasspathElements() {
        return runtimeClasspathElements;
    }

    public boolean hasSourceDir() {
        return null != sourceDirectory && sourceDirectory.isDirectory();
    }

    public boolean hasImports() {
        return null != imports;
    }

    public boolean hasTestDir() {
        return null != testSourceDirectory && testSourceDirectory.isDirectory();
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }
}
