package com.thoughtpropulsion.deterministic;

import com.thoughtpropulsion.ChannelBiDirectional;
import com.thoughtpropulsion.Continuation;
import com.thoughtpropulsion.NanoTime;
import com.thoughtpropulsion.Random;
import com.thoughtpropulsion.RandomImpl;
import com.thoughtpropulsion.SelectClause;
import com.thoughtpropulsion.TaskScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import static com.thoughtpropulsion.Continuation.NoOp;

public class TestScheduler implements TaskScheduler {

  final NanoTime nanoTime;
  final Random random;
  final PriorityQueue<Task> tasks;

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
    while( head != null && head.readyAsOfNanos <= now) {
      // grab the list of tasks that are potentially runnable now
      final List<Task> timeToRun = new ArrayList<>();
      do {
        tasks.remove(head);
        timeToRun.add(head);
        head = tasks.peek();
      } while (head != null && head.readyAsOfNanos <= now);

      // filter the list further: to tasks that say they are ready
      final Task[] readyToRun = timeToRun.stream().filter(task -> task.continuation.isReady()).toArray(Task[]::new);
      if (readyToRun.length > 0) {
        // randomly pick one task to run
        final int i = random.nextInt(readyToRun.length);
        final Task taskToRun = readyToRun[i];
        taskToRun.continuation.compute(this);

        timeToRun.forEach(task -> {
          if (task != taskToRun) {
            tasks.add(task);
          }
        });
      }
      head = tasks.peek();
    }
  }

  @Override
  public void schedule(final Continuation continuation) {
    schedule(continuation, 0, TimeUnit.SECONDS);
  }

  @Override
  public void schedule(final Continuation continuation, final long afterDelay, final TimeUnit delayUnit) {
    if (continuation != NoOp) {
      tasks.add(new Task(continuation, nanoTime.nanoTime() + delayUnit.toNanos(afterDelay)));
    }
  }

  @Override
  public <T> ChannelBiDirectional<T> createBoundedChannel(final Class<T> clazz, final int n) {
    return new ChannelBounded<>(clazz, n);
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
