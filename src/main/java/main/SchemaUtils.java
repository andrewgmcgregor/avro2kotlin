package main;

import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl;

import java.io.InputStream;
import java.util.Collection;

public class SchemaUtils {
    public static Collection<Schema> getSchemasForAvdl(String resourcePath) {
        ClassLoader classLoader = KotlinMaker.class.getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            return new Idl(in).ProtocolDeclaration().getTypes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Schema getSchemaForAvsc(String resourcePath) throws Exception {
        ClassLoader classLoader = KotlinMaker.class.getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            return new Schema.Parser().parse(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
