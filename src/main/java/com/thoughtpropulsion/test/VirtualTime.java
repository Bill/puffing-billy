package com.thoughtpropulsion.test;

import com.thoughtpropulsion.NanoTime;

import java.util.concurrent.TimeUnit;

public class VirtualTime implements NanoTime {
  private long nanoTime = 0;

  @Override
  public long nanoTime() {
    return nanoTime;
  }

  public long advance(final long duration, final TimeUnit durationUnit) {
    return nanoTime += durationUnit.toNanos(duration);
  }
}
