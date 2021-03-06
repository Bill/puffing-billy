package com.thoughtpropulsion;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ChannelWriting<T> extends Readiness, Closeable {
  default boolean storageAvailable() { return isReady();}

  /*
   Generates a clause suitable for (non-blocking) use with select/whileSelect
   */
  SendClause<T> onSend(final Predicate<ChannelWriting<T>> predicate);

  SendClause<T> onSend(final Runnable predicate);

  /*
   Low-level put on channel. Prefer non-blocking select/whileSelect with SendClause over this.
  */
  void put(T value);
}
