package com.thoughtpropulsion.deterministic;

import com.thoughtpropulsion.Continuation;

import java.util.Objects;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Task implements Comparable<Task> {
  public final Continuation continuation;
  public final long readyAsOfNanos;

  public Task(final Continuation continuation, final long readyAsOfNanos) {
    this.continuation = continuation;
    this.readyAsOfNanos = readyAsOfNanos;
  }

  @Override
  public int compareTo(final Task o) {
    if (readyAsOfNanos > o.readyAsOfNanos)
      return 1;
    if (readyAsOfNanos < o.readyAsOfNanos)
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
    return readyAsOfNanos == task.readyAsOfNanos &&
        continuation.equals(task.continuation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(continuation, readyAsOfNanos);
  }

  @Override
  public String toString() {
    return String.format("Task %s: continuation: %s", System.identityHashCode(this), continuation);
  }

}
