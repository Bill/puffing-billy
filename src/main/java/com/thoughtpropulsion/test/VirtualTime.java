package com.thoughtpropulsion.test;

import java.util.concurrent.TimeUnit;

import com.thoughtpropulsion.NanoTime;

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
