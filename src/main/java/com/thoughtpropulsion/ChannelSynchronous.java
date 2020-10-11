package com.thoughtpropulsion;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChannelSynchronous<T> {

  public ChannelReading<T> getReadable() {
    return new ChannelReading<T>() {

    };
  }
  public ChannelWriting<T> getWritable() {
    return new ChannelWriting<T>() {
    };
  }

}
