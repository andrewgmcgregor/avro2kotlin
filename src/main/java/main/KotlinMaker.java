package main;

import org.apache.avro.Schema;

import java.util.Collection;

public class KotlinMaker {
    public static void main(String[] args) throws Exception {
        Schema topLevelSchema = SchemaUtils.getSchemaForAvsc("src/main/avro/example.avsc");
        showSchema(topLevelSchema);

        System.out.println("--------");

        String resourcePath = "src/main/avro/example.avdl";
        Collection<Schema> schemas = SchemaUtils.getSchemasForAvdl(resourcePath);
        schemas.forEach(KotlinMaker::showSchema);
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
