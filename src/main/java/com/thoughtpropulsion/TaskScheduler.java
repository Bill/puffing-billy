package com.thoughtpropulsion;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface TaskScheduler {

  void schedule(final Runnable runnable);
  void schedule(final Runnable runnable, final long afterDelay, final TimeUnit delayUnit);

  void schedule(final Consumer<TaskScheduler> runnable);
  void schedule(final Consumer<TaskScheduler> runnable, final long afterDelay, final TimeUnit delayUnit);

  /**
   * Apply processor when any channels are ready. Keep going until processor
   * returns false.
   *
   * This method is not reentrant.
   *
   * @throws IllegalStateException if called inside another call to this method
   *
   * TODO: make this method return something
   */
  void selectWhile( final List<ChannelReading> readingChannels,
                    final List<ChannelWriting> writingChannels,
                    final SelectProcessor processor);
}
