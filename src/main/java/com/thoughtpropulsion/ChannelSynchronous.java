package com.thoughtpropulsion;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChannelSynchronous<T> implements Channel<T> {
  @Override
  public void receive(final Consumer<T> consumer) {

  }

  @Override
  public void send(final Supplier<T> supplier) {

  }

  @Override
  public T receiveBlocking() {
    return null;
  }

  @Override
  public void sendBlocking(final T val) {

  }

  @Override
  public ChannelOperation<T> onReceive(final Consumer<T> consumer) {
    return new ChannelOperation<T>() {
      @Override
      public void operate(final Channel<T> channel) {
        // channel is guaranteed to succeed with a non-blocking read here
        consumer.accept(channel.receiveBlocking());
      }
    };
  }

  @Override
  public ChannelOperation<T> onSend(final Supplier<T> supplier) {
    return new ChannelOperation<T>() {
      @Override
      public void operate(final Channel<T> channel) {
        // channel is guaranteed to succeed with a non-blocking write here
        channel.sendBlocking(supplier.get());
      }
    };
  }
}
