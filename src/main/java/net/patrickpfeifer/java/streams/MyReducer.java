package net.patrickpfeifer.java.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collector;

public class MyReducer {

    public static <T, R> Collector<T, ?, R> reducing(R identity, BiFunction<R, T, R> operator) {

        class Box {
            final List<T> values = new ArrayList<>();

            public void accumulate(T t) {
                values.add(t);
            }

            private Box combine(Box other) {
                values.addAll(other.values);
                return this;
            }

            R finish() {
                R r = identity;
                for (T v : values) {
                    r = operator.apply(r, v);
                }
                return r;
            }
        }

        return Collector.of(
                Box::new,
                Box::accumulate,
                Box::combine,
                Box::finish
        );
    }
}
