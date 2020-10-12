package com.thoughtpropulsion;

public class ChannelSynchronous<T> implements Channel<T> {

  private ChannelReading<T> channelReading = new ChannelReading<>() {};
  private ChannelWriting<T> channelWriting = new ChannelWriting<>() {};

  @Override
  public ChannelReading<T> getReading() {
    return channelReading;
  }

  @Override
  public ChannelWriting<T> getWriting() {
    return channelWriting;
  }

}
