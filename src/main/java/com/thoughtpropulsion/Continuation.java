package com.thoughtpropulsion;

public interface Continuation {

  // If you use this NoOp where you need a no-op then it will get optimized out for you
  Continuation NoOp = new Continuation() {
    @Override
    public boolean isReady () {
      return true;
    }

    @Override
    public void compute ( final TaskScheduler scheduler){
    }

    @Override
    public String toString() {
      return String.format("The NoOp Continuation");
    }
  };

  /*
    The scheduler runs only coroutines that have reached (or passed) their
    scheduled time to run AND which return true from isReady().
    This protocol lets a coroutine step, such as select(), decide for itself
    whether or not (based on channel readiness) it's actually ready to run.
    This is important because a blocking call like select() must block forever
    until it has a clause for a ready channel.
     */
  boolean isReady();

  /*
   Compute. Won't be called until after isReady() returns true.
   */
  void compute(TaskScheduler scheduler);
}
