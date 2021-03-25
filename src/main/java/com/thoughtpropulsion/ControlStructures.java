package com.thoughtpropulsion;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ControlStructures {
  private ControlStructures() {};

  public static SuspendFunctionVoid whileLoop(final BooleanSupplier condition, final Consumer<TaskScheduler> loopBody) {
    return new SuspendFunctionVoid() {
      @Override
      public void accept(final TaskScheduler scheduler) {
        if (condition.getAsBoolean()) {
          loopBody.accept(scheduler); // for side-effects
          scheduler.schedule(this); // iterate
        }
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
      public void accept(final TaskScheduler scheduler) {
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
        return whileLoop(() -> i < n, scheduler -> {
          ++i;
          loopBody.accept(i, scheduler); // for side-effets
        });
      }
    }.get();
  }

  // Useful for using result-returning (functions) in sequences
  public static <R> SuspendFunctionVoid ignoreResult(final SuspendFunction<R> f) {
    return f::apply;
  }

  public static <R> SuspendFunctionVoid sequence(final SuspendFunctionVoid... steps) {

    if (steps.length < 1)
      return scheduler -> {}; // no-op

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
          result = scheduler -> {
            currentStep.accept(scheduler);
            subsequentSteps.accept(scheduler);
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
  public static SuspendFunction<Boolean> select(
    final SelectClause... clauses) {
    return taskScheduler -> taskScheduler.select(clauses);
  }

  public static SuspendFunctionVoid whileSelect(
    final SelectClause... clauses) {
    return new SuspendFunctionVoid() {
      @Override
      public void accept(final TaskScheduler scheduler) {
        if (select(clauses).apply(scheduler)) { // let processor work
          scheduler.schedule(this); // iterate
        }
      }};
  }

}
