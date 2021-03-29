package com.thoughtpropulsion;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.thoughtpropulsion.Continuation.NoOp;

public class ControlStructures {
  private ControlStructures() {};

  public static Continuation whileLoop(final BooleanSupplier condition, final Continuation loopBody) {
    return new Continuation() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        if ( condition.getAsBoolean()) {
          scheduler.schedule(
            sequence(loopBody, this)
          );
        } // else we're done!
      }

      @Override
      public String toString() {
        return String.format("While Loop: %s", System.identityHashCode(this));
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

      @Override
      public String toString() {
        return String.format("Statement: %s", System.identityHashCode(this));
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

            @Override
            public String toString() {
              return String.format("Sequence step: %s", System.identityHashCode(this));
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

      @Override
      public String toString() {
        return String.format("Select Statement: %s", System.identityHashCode(this));
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
        } // else we're done!
      }

      @Override
      public String toString() {
        return String.format("While Select Statement: %s", System.identityHashCode(this));
      }
    };
  }

}
