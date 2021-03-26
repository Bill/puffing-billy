package com.thoughtpropulsion;

public interface SendClause<T> extends SelectClause {
  ChannelWriting<T> getChannel();
}
