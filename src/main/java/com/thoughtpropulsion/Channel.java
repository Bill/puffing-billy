package com.thoughtpropulsion;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Channel<T> {

  // nonblocking interaction with a single channel

  void receive(Consumer<T> consumer);
  void send(Supplier<T> supplier);

  // blocking interaction with a single channel

  T receiveBlocking();
  void sendBlocking(T val);

  // for select block (operating on multiple channels at once)

  ChannelOperation onReceive(Consumer<T> consumer);
  ChannelOperation onSend(Supplier<T> supplier);
}
