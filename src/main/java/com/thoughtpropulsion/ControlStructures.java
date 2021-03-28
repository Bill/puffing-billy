package com.thoughtpropulsion;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.thoughtpropulsion.Continuation.NoOp;

public class ControlStructures {
  private ControlStructures() {};

  public static Continuation whileLoop(final BooleanSupplier condition, final Continuation loopBody) {
    return new Continuation() {
      @Override
      public boolean isReady() {
        return condition.getAsBoolean() && loopBody.isReady();
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        loopBody.compute(scheduler); // for side-effects
        scheduler.schedule(this); // iterate
      }
    };
  }

  public static <T> Continuation statement(final Supplier<T> supplier) {
    return statement(() -> {supplier.get();});
  }

  public static Continuation statement(final Runnable runnable) {
    return new Continuation() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        runnable.run();
      }
    };
  }

  public static Continuation sequence(final Continuation... steps) {

    if (steps.length < 1) {
      return NoOp;
    }

    return new Supplier<Continuation> () {

      int i;
      Continuation result;

      @Override
      public Continuation get() {
        i = steps.length - 1; // last step
        result = steps[i--];
        while (i > -1) {
          final Continuation currentStep = steps[i];
          final Continuation subsequentSteps = result;
          result = new Continuation() {

            @Override
            public boolean isReady() {
              return currentStep.isReady();
            }

            @Override
            public void compute(final TaskScheduler scheduler) {
              currentStep.compute(scheduler);
              scheduler.schedule(subsequentSteps);
            }
          };
          --i;
        }
        return result;
      }
    }.get();
  }

  /*
   Returns a select clause which is a predicate: given the scheduler, it returns true/false
   and invokes zero-or-more ready clauses as a side-effect.
   */
  public static Continuation select(
    final SelectClause... clauses) {
    return new Continuation() {
      @Override
      public boolean isReady() {
        return Arrays.stream(clauses).map(SelectClause::getChannel)
          .anyMatch(Readiness::isReady);
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        scheduler.runReadyClauses(clauses);
      }
    };
  }

  public static Continuation whileSelect(
    final SelectClause... clauses) {
    return new Continuation() {
      @Override
      public boolean isReady() {
        return Arrays.stream(clauses).map(SelectClause::getChannel)
          .anyMatch(Readiness::isReady);
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        if (scheduler.runReadyClauses(clauses)) {
          scheduler.schedule(this);
        }
      }
    };
  }

}
