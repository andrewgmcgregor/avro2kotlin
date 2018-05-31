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

import org.apache.avro.Protocol;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static net.avro2kotlin.mojo.KotlinGeneratorTaskFinder.findAllTasks;

public class IDLProtocolJavaGenerator {
    public static void generateAll(KotlinGeneratorContext context) {
        findAllTasks(context).forEach(task -> generate(context, task));
    }

    public static void generate(KotlinGeneratorContext context, KotlinGeneratorTask generatorTask) {
        try {
            List<URL> runtimeUrls = getRuntimeUrls(context.getRuntimeClasspathElements(), generatorTask.sourceDirectory);

            URLClassLoader projPathLoader = new URLClassLoader(
                    runtimeUrls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader());
            Idl parser = new Idl(new File(generatorTask.sourceDirectory, generatorTask.filename), projPathLoader);

            Protocol p = parser.CompilationUnit();
            String json = p.toString(true);
            Protocol protocol = Protocol.parse(json);
            SpecificCompiler compiler = new SpecificCompiler(protocol);
            compiler.setStringType(GenericData.StringType.valueOf(context.getStringType()));
            compiler.setTemplateDir(context.getTemplateDirectory());
            compiler.setFieldVisibility(context.getFieldVisibility());
            compiler.setCreateSetters(context.isCreateSetters());
            compiler.setEnableDecimalLogicalType(context.isEnableDecimalLogicalType());
            compiler.compileToDestination(null, generatorTask.outputDirectory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static List<URL> getRuntimeUrls(List runtimeClasspathElements, File sourceDirectory) throws MalformedURLException {
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
        return runtimeUrls;
    }

}
