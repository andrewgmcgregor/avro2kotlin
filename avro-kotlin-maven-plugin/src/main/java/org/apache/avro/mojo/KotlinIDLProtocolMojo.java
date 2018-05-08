package org.apache.avro.mojo;

import org.apache.avro.Protocol;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.idl.ParseException;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;
import org.apache.maven.artifact.DependencyResolutionRequiredException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate Kotlin (+ Java) classes and interfaces from AvroIDL files (.avdl)
 *
 * @goal kotlin-idl-protocol
 * @requiresDependencyResolution runtime
 * @phase generate-sources
 * @threadSafe
 */
public class KotlinIDLProtocolMojo extends IDLProtocolMojo {
        /**
         * A set of Ant-like inclusion patterns used to select files from the source
         * directory for processing. By default, the pattern
         * <code>**&#47;*.avdl</code> is used to select IDL files.
         *
         * @parameter
         */
        private String[] includes = new String[] { "**/*.avdl" };

        /**
         * A set of Ant-like inclusion patterns used to select files from the source
         * directory for processing. By default, the pattern
         * <code>**&#47;*.avdl</code> is used to select IDL files.
         *
         * @parameter
         */
        private String[] testIncludes = new String[] { "**/*.avdl" };

        @Override
        protected void doCompile(String filename, File sourceDirectory, File outputDirectory) throws IOException {
            try {
                @SuppressWarnings("rawtypes")
                List runtimeClasspathElements = project.getRuntimeClasspathElements();
                Idl parser;

                List<URL> runtimeUrls = new ArrayList<URL>();

                // Add the source directory of avro files to the classpath so that
                // imports can refer to other idl files as classpath resources
                runtimeUrls.add(sourceDirectory.toURI().toURL());

                // If runtimeClasspathElements is not empty values add its values to Idl path.
                if (runtimeClasspathElements != null && !runtimeClasspathElements.isEmpty()) {
                    for (Object runtimeClasspathElement : runtimeClasspathElements) {
                        String element = (String) runtimeClasspathElement;
                        runtimeUrls.add(new File(element).toURI().toURL());
                    }
                }

                URLClassLoader projPathLoader = new URLClassLoader
                        (runtimeUrls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
                parser = new Idl(new File(sourceDirectory, filename), projPathLoader);

                Protocol p = parser.CompilationUnit();
                String json = p.toString(true);
                Protocol protocol = Protocol.parse(json);
                SpecificCompiler compiler = new SpecificCompiler(protocol);
                compiler.setStringType(GenericData.StringType.valueOf(stringType));
                compiler.setTemplateDir(templateDirectory);
                compiler.setFieldVisibility(getFieldVisibility());
                compiler.setCreateSetters(createSetters);
                compiler.setEnableDecimalLogicalType(enableDecimalLogicalType);
                compiler.compileToDestination(null, outputDirectory);
            } catch (ParseException e) {
                throw new IOException(e);
            } catch (DependencyResolutionRequiredException drre) {
                throw new IOException(drre);
            }

            System.out.println("this.imports = " + this.imports);
            System.out.println("this.excludes = " + this.excludes);
            System.out.println("this.createSetters = " + this.createSetters);
            System.out.println("this.enableDecimalLogicalType = " + this.enableDecimalLogicalType);
            System.out.println("filename = " + filename);
            System.out.println("sourceDirectory = " + sourceDirectory);
            System.out.println("outputDirectory = " + outputDirectory);
        }

        @Override
        protected String[] getIncludes() {
            return includes;
        }


        @Override
        protected String[] getTestIncludes() {
            return testIncludes;
        }

//    @Override
//    protected void doCompile(String filename, File sourceDirectory, File outputDirectory) throws IOException {
//        super.doCompile(filename, sourceDirectory, outputDirectory);
//
//        System.out.println("this.imports = " + this.imports);
//        System.out.println("this.excludes = " + this.excludes);
//        System.out.println("this.createSetters = " + this.createSetters);
//        System.out.println("this.enableDecimalLogicalType = " + this.enableDecimalLogicalType);
//        System.out.println("filename = " + filename);
//        System.out.println("sourceDirectory = " + sourceDirectory);
//        System.out.println("outputDirectory = " + outputDirectory);
//    }
}
