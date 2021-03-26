package com.thoughtpropulsion;

public interface ReceiveOrClosedClause<T> extends SelectClause {
  ChannelReading<T> getChannel();
}