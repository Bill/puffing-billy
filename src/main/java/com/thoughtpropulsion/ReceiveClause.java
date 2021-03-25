package com.thoughtpropulsion;

import java.util.function.Predicate;

public interface ReceiveClause<T> extends SelectClause {
  ChannelReading<T> getChannel();
  Predicate<T> getConsumer();
}
