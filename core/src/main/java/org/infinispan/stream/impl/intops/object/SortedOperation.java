package org.infinispan.stream.impl.intops.object;

import java.util.stream.Stream;

import org.infinispan.stream.impl.intops.IntermediateOperation;

import io.reactivex.Flowable;

/**
 * Performs sorted operation on a regular {@link Stream}
 */
public class SortedOperation<S> implements IntermediateOperation<S, Stream<S>, S, Stream<S>> {
   private static final SortedOperation<?> OPERATION = new SortedOperation<>();
   private SortedOperation() { }

   public static <S> SortedOperation<S> getInstance() {
      return (SortedOperation<S>) OPERATION;
   }

   @Override
   public Stream<S> perform(Stream<S> stream) {
      return stream.sorted();
   }

   @Override
   public Flowable<S> mapFlowable(Flowable<S> input) {
      return input.sorted();
   }
}
