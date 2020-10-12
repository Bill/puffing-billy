package com.thoughtpropulsion;

public interface Channel<T> {
  ChannelReading<T> getReading();
  ChannelWriting<T> getWriting();
}
