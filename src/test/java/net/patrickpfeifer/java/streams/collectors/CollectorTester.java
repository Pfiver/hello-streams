package net.patrickpfeifer.java.streams.collectors;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static org.assertj.core.api.Assertions.assertThat;

// https://stackoverflow.com/a/46893946

public class CollectorTester<T, A, R> {

    private final Supplier<A> supplier;
    private final BiConsumer<A, T> accumulator;
    private final Function<A, R> finisher;
    private final BinaryOperator<A> combiner;

    public CollectorTester(Collector<T, A, R> collector) {
        this.supplier = collector.supplier();
        this.accumulator = collector.accumulator();
        this.combiner = collector.combiner();
        this.finisher = collector.finisher();
    }

    // Tests that an accumulator resulting from the inputs supplied
    // meets the identity constraint
    @SafeVarargs
    public final void testIdentity(T... ts) {
        A a = supplier.get();
        Arrays.stream(ts).forEach(
                t -> accumulator.accept(a, t)
        );

        assertThat(combiner.apply(a, supplier.get())).isEqualTo(a);
    }

    // Tests that the combiner meets the associativity constraint
    // for the two inputs supplied
    // (This is verbatim from the Collector JavaDoc)
    // This test might be too strict for UNORDERED collectors
    public void testAssociativity(T t1, T t2) {
        A a1 = supplier.get();
        accumulator.accept(a1, t1);
        accumulator.accept(a1, t2);
        R r1 = finisher.apply(a1); // result without splitting

        A a2 = supplier.get();
        accumulator.accept(a2, t1);
        A a3 = supplier.get();
        accumulator.accept(a3, t2);
        R r2 = finisher.apply(combiner.apply(a2, a3)); // result with splitting

        System.err.println("test: " + r2 + " = " + r1);
        assertThat(r2).isEqualTo(r1);
    }


    public void testAssociativity2(T... ts) {
        var r1 = collectWihoutSplitting(ts);
        testWithSplittingCombiningFromLeft(ts, r1);
        testWithSplittingCombiningFromRight(ts, r1);
    }

    private R collectWihoutSplitting(T[] ts) {
        A a1 = supplier.get();
        Arrays.stream(ts).forEach(
                t -> accumulator.accept(a1, t)
        );
        return finisher.apply(a1); // result without splitting
    }

    private void testWithSplittingCombiningFromLeft(T[] ts, R r1) {

        A a2;
        R r2;
        A[] an = accumulateAllWithIdentity(ts);

        a2 = an[0];
        for (var i = 1; i < ts.length; i++) {
            a2 = combiner.apply(a2, an[i]);
        }
        r2 = finisher.apply(a2); // result with splitting

        System.err.println("test: " + r2 + " = " + r1);
        assertThat(r2).isEqualTo(r1);
    }

    private void testWithSplittingCombiningFromRight(T[] ts, R r1) {

        R r2;
        A a2;
        A[] an = accumulateAllWithIdentity(ts);

        a2 = an[ts.length - 1];
        for (var i = ts.length - 1; i > 0; i--) {
            a2 = combiner.apply(an[i - 1], a2);
        }
        r2 = finisher.apply(a2); // result with splitting 2

        System.err.println("test: " + r2 + " = " + r1);
        assertThat(r2).isEqualTo(r1);
    }

    private A[] accumulateAllWithIdentity(T[] ts) {
        @SuppressWarnings("unchecked")
        A[] an = (A[]) new Object[ts.length];

        for (var i = 0; i < ts.length; i++) {
            an[i] = supplier.get();
            accumulator.accept(an[i], ts[i]);
        }
        return an;
    }
}
