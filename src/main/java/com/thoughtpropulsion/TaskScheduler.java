package com.thoughtpropulsion;

import java.util.concurrent.TimeUnit;

public interface TaskScheduler {

  void schedule(final Continuation continuation);
  void schedule(final Continuation continuation, final long afterDelay, final TimeUnit delayUnit);

  <T> ChannelBiDirectional<T> createBoundedChannel(Class<T> clazz, int n);

  boolean runReadyClauses(SelectClause... clauses);
}
