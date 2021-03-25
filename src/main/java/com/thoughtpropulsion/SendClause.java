package com.thoughtpropulsion;

import java.util.function.Predicate;

public interface SendClause<T> extends SelectClause {
  ChannelWriting<T> getChannel();

  Predicate<ChannelWriting<T>> getPredicate();
}
