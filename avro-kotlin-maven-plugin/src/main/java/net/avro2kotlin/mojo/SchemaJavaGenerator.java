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

import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData.StringType;

import java.io.File;
import java.util.concurrent.Callable;

import static net.avro2kotlin.mojo.KotlinGeneratorTaskFinder.findAllTasks;

public class SchemaJavaGenerator {

    public static void generateAll(KotlinGeneratorContext context) {
        Schema.Parser schemaParser = new Schema.Parser();
        findAllTasks(context).forEach(task -> generate(context, task, schemaParser));
    }

    public static void generate(KotlinGeneratorContext context,
                                KotlinGeneratorTask task,
                                Schema.Parser schemaParser) {
        File src = new File(task.sourceDirectory, task.filename);
        Schema schema;

        // This is necessary to maintain backward-compatibility. If there are
        // no imported files then isolate the schemas from each other, otherwise
        // allow them to share a single schema so resuse and sharing of schema
        // is possible.
        if (context.getImports() == null) {
            schema = rethrowAsRuntime(() -> new Schema.Parser().parse(src));
        } else {
            schema = rethrowAsRuntime(() -> schemaParser.parse(src));
        }

        SpecificCompiler compiler = new SpecificCompiler(schema);
        compiler.setTemplateDir(context.getTemplateDirectory());
        compiler.setStringType(StringType.valueOf(context.getStringType()));
        compiler.setFieldVisibility(context.getFieldVisibility());
        compiler.setCreateSetters(context.isCreateSetters());
        compiler.setEnableDecimalLogicalType(context.isEnableDecimalLogicalType());
        compiler.setOutputCharacterEncoding(context.getSourceEncoding());
        rethrowAsRuntime((Callable<Void>) (() -> {
            compiler.compileToDestination(src, context.getOutputDirectory());
            return null;
        }));
    }

    public static <T> T rethrowAsRuntime(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
