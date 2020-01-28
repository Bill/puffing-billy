package com.thoughtpropulsion;

public interface ChannelOperation<T> {
  void operate(Channel<T> channel);
}
