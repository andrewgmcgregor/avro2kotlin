package main;

import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl;

import java.io.InputStream;

public class KotlinMaker {
    public static void main(String[] args) throws Exception {
        ClassLoader classLoader = KotlinMaker.class.getClassLoader();

        try (InputStream in = classLoader.getResourceAsStream("example.avsc")) {
            Schema topLevelSchema = new Schema.Parser().parse(in);
            showSchema(topLevelSchema);
        }

        System.out.println("--------");

        try (InputStream in = classLoader.getResourceAsStream("example.avdl")) {
            new Idl(in).ProtocolDeclaration()
                    .getTypes()
                    .forEach(KotlinMaker::showSchema);
        }
    }

    private static void showSchema(Schema topLevelSchema) {
        System.out.println("topLevelSchema: name = " + topLevelSchema.getName() + ", type = " + topLevelSchema.getType().getName());
        if (topLevelSchema.getType() == Schema.Type.RECORD) {
            for (Schema.Field field : topLevelSchema.getFields()) {
                System.out.println("field: name = " + field.name() + ", type = " + field.schema().getType().getName());
                if (field.schema().getType() == Schema.Type.UNION) {
                    for (Schema subSchema : field.schema().getTypes()) {
                        System.out.println("union type: " + subSchema.getType().getName());
                    }
                }
            }
        }
        System.out.println("*********");
    }
}
