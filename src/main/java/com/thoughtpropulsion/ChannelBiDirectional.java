package com.thoughtpropulsion;

public interface ChannelBiDirectional<T> {
  ChannelReading<T> getReading();
  ChannelWriting<T> getWriting();
}
