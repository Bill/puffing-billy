package com.thoughtpropulsion;

import java.util.function.BooleanSupplier;

public interface SelectClause {
  Readiness getChannel();
  BooleanSupplier getClause();
}
