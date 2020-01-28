package com.thoughtpropulsion;

import java.util.concurrent.TimeUnit;

public interface TaskScheduler {

  void schedule(final Runnable runnable);
  void schedule(final Runnable runnable, final long afterDelay, final TimeUnit delayUnit);

}
