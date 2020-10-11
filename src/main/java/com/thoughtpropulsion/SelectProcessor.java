package com.thoughtpropulsion;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface SelectProcessor {

  // selectWhile() continues as long as this returns true
  boolean apply( Map<Channel, Supplier> suppliers, Map<Channel, Consumer> consumers);
}
