package net.patrickpfeifer.java.streams;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class YiJiCollector {

    public static <T, R> Collector<T, ?, R> foldLeft(
            R init,
            BiFunction<? super R, ? super T, ? extends R> f
    ) {

        return Collectors.collectingAndThen(

                Collectors.reducing(

                        Function.<R>identity(),

                        a -> b -> f.apply(b, a),

                        Function::andThen
                ),

                endo -> endo.apply(init)
        );
    }
}
