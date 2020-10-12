package com.thoughtpropulsion.test;

import java.security.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.thoughtpropulsion.ChannelReading;
import com.thoughtpropulsion.ChannelWriting;
import com.thoughtpropulsion.NanoTime;
import com.thoughtpropulsion.SelectProcessor;
import com.thoughtpropulsion.Task;
import com.thoughtpropulsion.TaskScheduler;

public class TestScheduler implements TaskScheduler {

  final NanoTime nanoTime;
  final PriorityQueue<Task> tasks;
  boolean isInsideSelectWhen;

  public TestScheduler(final NanoTime nanoTime) {
    this.nanoTime = nanoTime;
    tasks = new PriorityQueue<>();
    isInsideSelectWhen = false;
  }

  public void triggerActions() {
    // TODO: I'd rather go directly to the split point in a single operation instead of this loop
    final long now = nanoTime.nanoTime();
    Task head = tasks.peek();
    while (head != null && head.runnableAsOfNanos <= now) {
      tasks.remove(head);
      head.runnable.accept(this);
      head = tasks.peek();
    }
  }

  @Override
  public void schedule(final Runnable runnable) {
    schedule( scheduler -> runnable.run());
  }

  @Override
  public void schedule(final Runnable runnable, final long afterDelay, final TimeUnit delayUnit) {
    schedule( scheduler -> runnable.run(), afterDelay, delayUnit);
  }

  @Override
  public void schedule(final Consumer<TaskScheduler> runnable) {
    schedule(runnable, 0, TimeUnit.SECONDS);
  }

  @Override
  public void schedule(final Consumer<TaskScheduler> runnable, final long afterDelay,
                       final TimeUnit delayUnit) {
    tasks.add(new Task(runnable, nanoTime.nanoTime() + delayUnit.toNanos(afterDelay)));
  }

  @Override
  public void selectWhile(final List<ChannelReading> readingChannels,
                          final List<ChannelWriting> writingChannels,
                          final SelectProcessor processor) {
    if (isInsideSelectWhen)
      throw new IllegalStateException("Aready in selectWhile(). Re-entrant calls are prohibited");

    try {
      isInsideSelectWhen = true;

      final Map<ChannelReading, Supplier> channelReadingSupplierMap = new HashMap<>();
      final Map<ChannelWriting, Consumer> channelWritingConsumerMap = new HashMap<>();

      processor.apply(channelReadingSupplierMap, channelWritingConsumerMap);

    } finally {
      isInsideSelectWhen = false;
    }
  }
}
