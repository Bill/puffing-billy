package com.thoughtpropulsion;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface TaskScheduler {

  void schedule(final Runnable runnable);
  void schedule(final Runnable runnable, final long afterDelay, final TimeUnit delayUnit);

  void schedule(final Consumer<TaskScheduler> runnable);
  void schedule(final Consumer<TaskScheduler> runnable, final long afterDelay, final TimeUnit delayUnit);

  void schedule(final ReadyCompute runnable);
  void schedule(final ReadyCompute runnable, final long afterDelay, final TimeUnit delayUnit);

  boolean runReadyClauses(SelectClause... clauses);
}
