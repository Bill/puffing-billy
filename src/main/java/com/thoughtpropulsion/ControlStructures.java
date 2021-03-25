package com.thoughtpropulsion;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ControlStructures {
  private ControlStructures() {};

  public static Consumer<TaskScheduler> whileLoop(final BooleanSupplier condition, final Consumer<TaskScheduler> loopBody) {
    return new Consumer<>() {
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
  public static Consumer<TaskScheduler> forLoop(final int n, final Consumer<TaskScheduler> loopBody) {
    return new Consumer<>() {

      private int i = 0; // the loop variable

      @Override
      public void accept(final TaskScheduler scheduler) {
        if (i < n) {
          ++i;
          loopBody.accept(scheduler); // for side-effects
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
  public static Consumer<TaskScheduler> forLoopFromWhile(final int n, final Consumer<TaskScheduler> loopBody) {

    final int[] i = {0}; // the loop variable

    return whileLoop(() -> i[0] < n, scheduler -> {
      ++i[0];
      loopBody.accept(scheduler); // for side-effets
    });
  }

  /*
   Returns a select clause which is a predicate: given the scheduler, it returns true/false
   and invokes zero-or-more ready clauses as a side-effect.
   */
  public static Predicate<TaskScheduler> select(
    final SelectClause... clauses) {
    return taskScheduler -> taskScheduler.select(clauses);
  }

  public static Consumer<TaskScheduler> whileSelect(
    final SelectClause... clauses) {
    return new Consumer<>() {
      @Override
      public void accept(final TaskScheduler scheduler) {
        if (select(clauses).test(scheduler)) { // let processor work
          scheduler.schedule(this); // iterate
        }
      }};
  }

}
