package main;

import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;
import org.apache.avro.mojo.SchemaMojo;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JavaMain {
    public static class MySchemaMojo extends SchemaMojo {
        public void compile(String filename, File sourceDirectory, File outputDirectory) throws IOException {
            doCompile(filename, sourceDirectory, outputDirectory);
        }
    }

    public static void main(String[] args) throws Exception {
//        MySchemaMojo mySchemaMojo = new MySchemaMojo();

        ClassLoader classLoader = JavaMain.class.getClassLoader();
        try (InputStream avscFile = classLoader.getResourceAsStream("example.avsc")) {
            File outputDirectory = new File(System.getProperty("user.dir") + "/src/main/java");
            doCompile(avscFile, outputDirectory);
        }
//            Idl idl = new Idl(avscFile);
//            System.out.println("idl = " + idl);
//
//
//
//        }
    }

    private static Schema.Parser schemaParser = new Schema.Parser();
    private static String[] imports;
    private static String[] excludes = new String[0];
    private static String[] testExcludes = new String[0];
    private static String stringType = "CharSequence";
    private static String templateDirectory = "/org/apache/avro/compiler/specific/templates/java/classic/";
    private static boolean createSetters;
    private static boolean enableDecimalLogicalType;
    private static String fieldVisibility;

    private static void doCompile(InputStream inputFile, File outputDirectory) throws IOException {
        Schema schema;

        // This is necessary to maintain backward-compatibility. If there are
        // no imported files then isolate the schemas from each other, otherwise
        // allow them to share a single schema so resuse and sharing of schema
        // is possible.
        if (imports == null) {
            schema = new Schema.Parser().parse(inputFile);
        } else {
            schema = schemaParser.parse(inputFile);
        }

        SpecificCompiler compiler = new SpecificCompiler(schema);
        compiler.setTemplateDir(templateDirectory);
        compiler.setStringType(GenericData.StringType.valueOf(stringType));
        compiler.setFieldVisibility(getFieldVisibility());
        compiler.setCreateSetters(createSetters);
        compiler.setEnableDecimalLogicalType(enableDecimalLogicalType);
        compiler.setOutputCharacterEncoding("UTF-8");
        compiler.compileToDestination(null, outputDirectory);
    }

    private static SpecificCompiler.FieldVisibility getFieldVisibility() {
        try {
            String upper = String.valueOf(JavaMain.fieldVisibility).trim().toUpperCase();
            return SpecificCompiler.FieldVisibility.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return SpecificCompiler.FieldVisibility.PUBLIC_DEPRECATED;
        }
    }

}
