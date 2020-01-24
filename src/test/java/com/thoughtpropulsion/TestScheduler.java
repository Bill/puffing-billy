package com.thoughtpropulsion;

import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

public class TestScheduler implements TaskScheduler {

  final NanoTime nanoTime;
  final PriorityQueue<Task> tasks;

  public TestScheduler(final NanoTime nanoTime) {
    this.nanoTime = nanoTime;
    tasks = new PriorityQueue<>();
  }

  public void triggerActions() {
    // TODO: I'd rather go directly to the split point in a single operation instead of this loop
    final long now = nanoTime.nanoTime();
    Task head = tasks.peek();
    while (head != null && head.runnableAsOfNanos <= now) {
      tasks.remove(head);
      head.runnable.run();
      head = tasks.peek();
    }
  }

  @Override
  public void schedule(final Runnable runnable, final long afterDelay, final TimeUnit delayUnit) {
    tasks.add(new Task(runnable, nanoTime.nanoTime() + delayUnit.toNanos(afterDelay)));
  }
}
