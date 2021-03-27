package com.thoughtpropulsion;

import java.util.Objects;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Task implements Comparable<Task> {
  public final ReadyCompute runnable;
  public final long runnableAsOfNanos;

  public Task(final ReadyCompute runnable, final long runnableAsOfNanos) {
    this.runnable = runnable;
    this.runnableAsOfNanos = runnableAsOfNanos;
  }

  @Override
  public int compareTo(final Task o) {
    if (runnableAsOfNanos > o.runnableAsOfNanos)
      return 1;
    if (runnableAsOfNanos < o.runnableAsOfNanos)
      return -1;
    return 0;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Task task = (Task) o;
    return runnableAsOfNanos == task.runnableAsOfNanos &&
        runnable.equals(task.runnable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runnable, runnableAsOfNanos);
  }
}
