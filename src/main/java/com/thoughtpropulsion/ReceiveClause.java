package com.thoughtpropulsion;

public interface ReceiveClause<T> extends SelectClause {
  ChannelReading<T> getChannel();
}
