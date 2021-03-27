package com.thoughtpropulsion;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ControlStructures {
  private ControlStructures() {};

  public static SuspendFunctionVoid whileLoop(final BooleanSupplier condition, final SuspendFunctionVoid loopBody) {
    return new SuspendFunctionVoid() {
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

  /*
     Construct a Runnable (continuation) that invokes a runnable (for side-effects) n times
     This is how you implement a loop using the TaskScheduler framework.

     for(int i = 0; i < n; ++i) {
       do loopBody
     }
     */
  public static SuspendFunctionVoid forLoop(final int n, final BiConsumer<Integer,TaskScheduler> loopBody) {
    return new SuspendFunctionVoid() {

      private int i = 0; // the loop variable

      @Override
      public boolean isReady() {
        // TODO: test forLoop(select())--I don't think that will work without delegation here
        return true;
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        if (i < n) {
          loopBody.accept(i, scheduler); // for side-effects
          ++i;
          scheduler.schedule(this);// iterate
        }
      }
    };
  }

  /*
   Just showing that we can implement the for loop using the while loop.
   This is has one more allocation that the one above:
   the single-element array for the loop counter.
   */
  public static SuspendFunctionVoid forLoopFromWhile(final int n, final BiConsumer<Integer,TaskScheduler> loopBody) {

    return new Supplier<SuspendFunctionVoid>() {

      int i = 0;

      @Override
      public SuspendFunctionVoid get() {
        return whileLoop(() -> i < n,
          new SuspendFunctionVoid() {
            @Override
            public boolean isReady() {
              // TODO: test forLoop(select())--I don't think that will work without delegation here
              return true;
            }

            @Override
            public void compute(final TaskScheduler scheduler) {
              ++i;
              loopBody.accept(i, scheduler); // for side-effets
            }
          }
        );
      }
    }.get();
  }

  public static <T> SuspendFunctionVoid statement(final Supplier<T> supplier) {
    return statement(() -> {supplier.get();});
  }

  public static SuspendFunctionVoid statement(final Runnable runnable) {
    return new SuspendFunctionVoid() {
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

  public static SuspendFunctionVoid sequence(final SuspendFunctionVoid... steps) {

    if (steps.length < 1)
      // TODO: optimize out this no-op statement
      return new SuspendFunctionVoid() {
        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void compute(final TaskScheduler scheduler) {
        }
      }; // no-op

    return new Supplier<SuspendFunctionVoid> () {

      int i;
      SuspendFunctionVoid result;

      @Override
      public SuspendFunctionVoid get() {
        i = steps.length - 1; // last step
        result = steps[i--];
        while (i > -1) {
          final SuspendFunctionVoid currentStep = steps[i];
          final SuspendFunctionVoid subsequentSteps = result;
          result = new SuspendFunctionVoid() {

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
  public static SuspendFunctionVoid select(
    final SelectClause... clauses) {
    return new SuspendFunctionVoid() {
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

  public static SuspendFunctionVoid whileSelect(
    final SelectClause... clauses) {
    return new SuspendFunctionVoid() {
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
