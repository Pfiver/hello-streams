package net.patrickpfeifer.java.streams;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class JdkReducer {

    public static <T> Collector<T, ?, T> reducing(BinaryOperator<T> op) {

        class Box implements Consumer<T> {
            T value = null;
            boolean present = false;

            @Override
            public void accept(T t) {
                if (present) {
                    value = op.apply(value, t);
                }
                else {
                    value = t;
                    present = true;
                }
            }
        }

        return Collector.of(
                Box::new,
                Box::accept,
                (a, b) -> {
                    if (b.present) a.accept(b.value);
                    return a;
                },
                a -> a.value
        );
    }
}
