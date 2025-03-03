package net.patrickpfeifer.java.streams.collectors;

import net.patrickpfeifer.java.streams.DudyczCollector;
import net.patrickpfeifer.java.streams.JdkReducer;
import net.patrickpfeifer.java.streams.MyReducer;
import net.patrickpfeifer.java.streams.YiJiCollector;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class CollectorTest {

    @TestFactory
    Stream<DynamicTest> testCollector() {

        BinaryOperator<String> nonAssociativeStringJoiner =
                (String r, String e) -> r.isEmpty() ? e : "(%s ~ %s)".formatted(r, e);

        BiFunction<List<List<Object>>, Object, List<List<Object>>> cumulativeListReducer =
                (r, e) -> Stream.of(
                                r.stream(),
                                Stream.of(
                                        Stream.of(
                                                        r.getLast().stream(),
                                                        Stream.of(e)
                                                )
                                                .flatMap(Function.identity())
                                                .toList()
                                )
                        )
                        .flatMap(Function.identity())
                        .toList();

        return Stream.of(

                dynamicTest("JDK.joining", () -> assert_collector_works(Collectors.joining(
                        " ~ "
                ))),
                dynamicTest("JDK.reducing", () -> assert_collector_works(JdkReducer.reducing(
                        nonAssociativeStringJoiner
                ))),
                dynamicTest("MyReducer.reducing", () -> assert_collector_works(MyReducer.reducing(
                        "", (r, e) -> "(%s ~ %s)".formatted(r, e)
                ))),
                dynamicTest("Dudycz", () -> assert_collector_works(new DudyczCollector.FoldLeft<>(
                        () -> "",
                        nonAssociativeStringJoiner
                ))),
                dynamicTest("Dudycz.cumulative", () -> assert_collector_works(new DudyczCollector.FoldLeft<>(
                        () -> List.of(List.of()),
                        cumulativeListReducer
                ))),
                dynamicTest("YiJi", () -> assert_collector_works(YiJiCollector.foldLeft(
                        "",
                        nonAssociativeStringJoiner
                ))),
                dynamicTest("YiJi.cumulative", () -> assert_collector_works(YiJiCollector.foldLeft(
                        List.of(List.of()),
                        cumulativeListReducer
                )))
//                dynamicTest("PFiver.reduce", () -> assert_collector_works(
//
//                ))
        );
    }

    @Test
    void mine_works() {

        BinaryOperator<String> nonAssociativeStringJoiner =
                (String r, String e) -> r == null ? e : "(%s ~ %s)".formatted(r, e);

        System.out.println(
                Stream.of("a", "b", "c", "d")
                        .reduce(
                                Function.<String>identity(),
                                (a, e1) -> r -> nonAssociativeStringJoiner.apply(a.apply(r), e1),
                                Function::andThen
                        )
                        .apply(null)
        );

        System.out.println(
                Stream.of("a", "b", "c", "d")
                        .reduce(
                                (Supplier<String>) () -> null,
                                (a, e) -> () -> nonAssociativeStringJoiner.apply(a.get(), e),
                                (a, b) -> () -> a.get() + b.get()
                        )
                        .get()
        );
    }

    private void assert_collector_works(Collector<? super String, ?, ?> c) {
        var ct = new CollectorTester<>(c);
        ct.testIdentity("a", "b", "c", "d");
        ct.testAssociativity("a", "b");
        ct.testAssociativity2("a", "b", "c", "d");
    }
}
