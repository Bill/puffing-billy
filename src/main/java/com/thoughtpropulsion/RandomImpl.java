package com.thoughtpropulsion;

public class RandomImpl implements Random {

  private final java.util.Random generator;

  public RandomImpl(final int seed) {
    generator = new java.util.Random(seed);
  }

  @Override
  public int nextInt(final int bound) {
    return generator.nextInt(bound);
  }
}
