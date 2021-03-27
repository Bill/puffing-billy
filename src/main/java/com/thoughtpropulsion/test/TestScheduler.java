package com.thoughtpropulsion.test;

import com.google.common.collect.Streams;
import com.thoughtpropulsion.ChannelReading;
import com.thoughtpropulsion.ChannelWriting;
import com.thoughtpropulsion.NanoTime;
import com.thoughtpropulsion.Random;
import com.thoughtpropulsion.RandomImpl;
import com.thoughtpropulsion.ReadyCompute;
import com.thoughtpropulsion.SelectClause;
import com.thoughtpropulsion.SelectProcessor;
import com.thoughtpropulsion.Task;
import com.thoughtpropulsion.TaskScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TestScheduler implements TaskScheduler {

  final NanoTime nanoTime;
  final Random random;
  final PriorityQueue<Task> tasks;
  final Map<ChannelReading,Set<SelectProcessor>> readInterest;
  final Map<ChannelWriting,Set<SelectProcessor>> writeInterest;

  public TestScheduler(final NanoTime nanoTime) {
    this(nanoTime, 0);
  }

  /*
  Launch Java VM with -Dnondeterministic to use random seed
  (overriding randomSeedArg)
  */
  public TestScheduler(final NanoTime nanoTime, final int randomSeedArg) {
    this(nanoTime, getRandom(randomSeedArg));
  }

  private TestScheduler(final NanoTime nanoTime, final Random random) {
    this.nanoTime = nanoTime;
    this.random = random;
    tasks = new PriorityQueue<>();
    readInterest = new HashMap<>();
    writeInterest = new HashMap<>();
  }

  private static RandomImpl getRandom(final int seedArg) {
    int seed = seedArg;
    if (System.getProperty("nondeterministic") != null) {
      seed = (int) System.nanoTime();
      System.out.println("TestScheduler using time-derived random seed: " + seed);
    }
    return new RandomImpl(seed);
  }

  public void triggerActions() {
    final long now = nanoTime.nanoTime();

    Task head = tasks.peek();

    // as long as there is a runnable task now
    while( head != null && head.runnableAsOfNanos <= now) {

      // grab the list of tasks that are potentially runnable now
      final List<Task> runnableList = new ArrayList<>();
      do {
        tasks.remove(head);
        runnableList.add(head);
        head = tasks.peek();
      } while (head != null && head.runnableAsOfNanos <= now);

      // filter the list further: to tasks that say they are ready
      final Task[] runnables = runnableList.stream().filter(task -> task.runnable.isReady()).toArray(Task[]::new);
      if (runnables.length > 0) {
        // randomly pick one task to run
        final int i = random.nextInt(runnables.length);
        runnables[i].runnable.compute(this);

        // re-queue the tasks we didn't run
        Streams.concat(
          Arrays.stream(Arrays.copyOfRange(runnables, 0, i)),
          Arrays.stream(Arrays.copyOfRange(runnables, i + 1, runnables.length))).forEach(tasks::add);
      }
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
    schedule(new ReadyCompute() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        runnable.accept(scheduler);
      }
    });
  }

  @Override
  public void schedule(final Consumer<TaskScheduler> runnable, final long afterDelay,
                       final TimeUnit delayUnit) {
    schedule(new ReadyCompute() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
        runnable.accept(scheduler);
      }
    }, afterDelay, delayUnit);
  }

  @Override
  public void schedule(final ReadyCompute runnable) {
    schedule(runnable, 0, TimeUnit.SECONDS);
  }

  @Override
  public void schedule(final ReadyCompute runnable, final long afterDelay, final TimeUnit delayUnit) {
    tasks.add(new Task(runnable, nanoTime.nanoTime() + delayUnit.toNanos(afterDelay)));
  }

  @Override
  public boolean runReadyClauses(final SelectClause[] clauses) {
    final SelectClause[] readyClauses = Arrays.stream(clauses)
      .filter(clause -> clause.getChannel().isReady()).toArray(SelectClause[]::new);
    assert readyClauses.length > 0 : "Select expression can't run with no ready send/receive clauses";
    final int i = random.nextInt(readyClauses.length);
    final SelectClause readyClause = readyClauses[i];
    return readyClause.getClause().getAsBoolean();
  }
}
