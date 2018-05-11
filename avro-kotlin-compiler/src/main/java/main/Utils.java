package main;

import java.util.concurrent.Callable;

public class Utils {
    public static <T> T tryOrThrow(Callable<T> value) {
        try {
            return value.call();
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
