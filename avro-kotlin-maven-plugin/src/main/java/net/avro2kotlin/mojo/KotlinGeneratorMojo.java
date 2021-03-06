/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.avro2kotlin.mojo;

import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate Kotlin classes and interfaces
 *
 * @goal generate
 * @requiresDependencyResolution runtime
 * @phase generate-sources
 * @threadSafe
 */
public class KotlinGeneratorMojo extends AbstractMojo {
    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.avdl</code> is used to select IDL files.
     *
     * @parameter
     */
    private String[] idlIncludes = new String[]{"**/*.avdl"};

    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.avdl</co  de> is used to select IDL files.
     *
     * @parameter
     */
    private String[] idlTestIncludes = new String[]{"**/*.avdl"};

    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.avsc</code> is used to select Schema files.
     *
     * @parameter
     */
    private String[] schemaIncludes = new String[]{"**/*.avsc"};

    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.avsc</co  de> is used to select Schema files.
     *
     * @parameter
     */
    private String[] schemaTestIncludes = new String[]{"**/*.avsc"};

    /**
     * The source directory of avro files. This directory is added to the
     * classpath at schema compiling time. All files can therefore be referenced
     * as classpath resources following the directory structure under the
     * source directory.
     *
     * @parameter property="sourceDirectory"
     * default-value="${basedir}/src/main/avro"
     */
    private File sourceDirectory;

    /**
     * @parameter property="outputDirectory"
     * default-value="${project.build.directory}/generated-sources/avro"
     */
    private File outputDirectory;

    /**
     * @parameter property="sourceDirectory"
     * default-value="${basedir}/src/test/avro"
     */
    private File testSourceDirectory;

    /**
     * @parameter property="outputDirectory"
     * default-value="${project.build.directory}/generated-test-sources/avro"
     */
    private File testOutputDirectory;

    /**
     * The field visibility indicator for the fields of the generated class, as
     * string values of SpecificCompiler.FieldVisibility.  The text is case
     * insensitive.
     *
     * @parameter default-value="PUBLIC_DEPRECATED"
     */
    private String fieldVisibility;

    /**
     * A list of files or directories that should be compiled first thus making
     * them importable by subsequently compiled schemas. Note that imported files
     * should not reference each other.
     *
     * @parameter
     */
    protected String[] imports;

    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from
     * being processed. By default, this set is empty such that no files are
     * excluded.
     *
     * @parameter
     */
    protected String[] excludes = new String[0];

    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from
     * being processed. By default, this set is empty such that no files are
     * excluded.
     *
     * @parameter
     */
    protected String[] testExcludes = new String[0];

    /**
     * The Java type to use for Avro strings.  May be one of CharSequence,
     * String or Utf8.  CharSequence by default.
     *
     * @parameter property="stringType"
     */
    protected String stringType = "String";

    /**
     * The directory (within the java classpath) that contains the velocity templates
     * to use for code generation. The default value points to the templates included
     * with the avro-maven-plugin.
     *
     * @parameter property="templateDirectory"
     */
    protected String templateDirectory = "/org/apache/avro/compiler/specific/templates/java/classic/";

    /**
     * Determines whether or not to create setters for the fields of the record.
     * The default is to create setters.
     *
     * @parameter default-value="true"
     */
    protected boolean createSetters;

    /**
     * Determines whether or not to use Java classes for decimal types
     *
     * @parameter default-value="true"
     */
    protected boolean enableDecimalLogicalType = true;

    /**
     * The current Maven project.
     *
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        KotlinGeneratorContext context = new KotlinGeneratorContext(
                null,
                null,
                sourceDirectory,
                outputDirectory,
                testSourceDirectory,
                testOutputDirectory,
                getFieldVisibility(fieldVisibility),
                imports,
                excludes,
                testExcludes,
                stringType,
                templateDirectory,
                createSetters,
                enableDecimalLogicalType,
                getOrWrapInMojoExecutionException(() -> project.getRuntimeClasspathElements()),
                project.getProperties().getProperty("project.build.sourceEncoding"));

        KotlinGenerator.generateAll(context.copy(
                concatStringArrays(idlIncludes, schemaIncludes),
                concatStringArrays(idlTestIncludes, schemaTestIncludes)));

        IDLProtocolJavaGenerator.generateAll(context.copy(idlIncludes, idlTestIncludes));

        SchemaJavaGenerator.generateAll(context.copy(schemaIncludes, schemaTestIncludes));

        configureMavenProject(context);
    }

    private String[] concatStringArrays(String[]... strings) {
        return Stream.of(strings)
                .flatMap(Stream::of)
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }

    private void configureMavenProject(KotlinGeneratorContext context) {
        if (context.hasImports() || context.hasSourceDir()) {
            project.addCompileSourceRoot(context.getOutputDirectory().getAbsolutePath());
        }

        if (context.hasTestDir()) {
            project.addTestCompileSourceRoot(context.getTestOutputDirectory().getAbsolutePath());
        }
    }

    private static SpecificCompiler.FieldVisibility getFieldVisibility(String fieldVisibility) {
        try {
            String upper = String.valueOf(fieldVisibility).trim().toUpperCase();
            return SpecificCompiler.FieldVisibility.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return SpecificCompiler.FieldVisibility.PUBLIC_DEPRECATED;
        }
    }

    private List getOrWrapInMojoExecutionException(Callable<List<Object>> action) throws MojoExecutionException {
        try {
            return action.call();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
