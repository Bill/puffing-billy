package com.thoughtpropulsion;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface SelectProcessor {

  // selectWhile() continues as long as this returns true
  boolean apply(Set<ChannelReading> readables, Set<ChannelWriting> writeables);

  default boolean isEntered() {return false;};
}
