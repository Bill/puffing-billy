package com.thoughtpropulsion;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ChannelReading<T> extends Readiness {
  default boolean contentAvailable() { return isReady();}

  /*
   Generates a clause suitable for (non-blocking) use with select/whileSelect
   */
  ReceiveClause<T> onReceive( Predicate<T> receiver);

  ReceiveClause<T> onReceive( Consumer<T> receiver);

  ReceiveOrClosedClause<T> onReceiveOrClosed(BiPredicate<T,Boolean> receiver);

  /*
   Low-level get on channel. Prefer non-blocking select/whileSelect with ReceiveClause over this.
   */
  T get();
}
