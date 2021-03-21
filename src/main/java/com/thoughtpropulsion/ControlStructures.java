package com.thoughtpropulsion;

import java.util.function.Consumer;

public class ControlStructures {
  private ControlStructures() {};

  /*
     Construct a Runnable (continuation) that invokes a runnable (for side-effects) n times
     This is how you implement a loop using the TaskScheduler framework.

     for(int i = 0; i < n; ++i) {
       do loopBody
     }
     */
  public static Consumer<TaskScheduler> nTimes(final int n, final Consumer<TaskScheduler> loopBody) {
    return new Consumer<>() {

      private int i = 0; // the loop variable

      @Override
      public void accept(final TaskScheduler scheduler) {
        if (i < n) {
          i++;
          loopBody.accept(scheduler); // for side-effects
          scheduler.schedule(this);// iterate
        }
      }
    };
  }
}
