package com.thoughtpropulsion;

import java.util.function.Predicate;

public interface ChannelReading<T> extends Readiness {
  default boolean contentAvailable() { return isReady();}

  /*
   Generates a clause suitable for (non-blocking) use with select/whileSelect
   */
  ReceiveClause<T> onReceive( Predicate<T> receiver);

  /*
   Low-level get on channel. Prefer non-blocking select/whileSelect with ReceiveClause over this.
   */
  T get();
}
