package main;

import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl;

import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class SchemaUtils {
    public static Collection<Schema> getSchemasForAvdl(String resourcePath) throws Exception {
        return getSchemaForAvsc(resourcePath, in -> tryOrThrow(() -> new Idl(in).ProtocolDeclaration().getTypes()));
    }

    public static Schema getSchemaForAvsc(String resourcePath) throws Exception {
        return getSchemaForAvsc(resourcePath, in -> tryOrThrow(() -> new Schema.Parser().parse(in)));
    }

    public static <R> R getSchemaForAvsc(String resourcePath, Function<InputStream, R> f) throws Exception {
        ClassLoader classLoader = SchemaUtils.class.getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            return f.apply(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T tryOrThrow(Callable<T> value) {
        try {
            return value.call();
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
