package com.thoughtpropulsion;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReadModifyWriteTest {

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  @Test
  public void foo() {

    final ChannelSynchronous<Integer> writeChannel = new ChannelSynchronous<>();
    final ChannelSynchronous<Integer> readChannel = new ChannelSynchronous<>();

    scheduler.schedule(newRegister(writeChannel, readChannel));

    scheduler.schedule(newMutator(writeChannel, readChannel));
    scheduler.schedule(newMutator(writeChannel, readChannel));

    scheduler.triggerActions();
  }

  private Runnable newRegister(final ChannelSynchronous<Integer> writeChannel,
                               final ChannelSynchronous<Integer> readChannel) {
    return () -> {
      final AtomicInteger value = new AtomicInteger(0);
      for(int i = 0; i < 5; ++i) {
        Channels.select(
            writeChannel.onReceive(value::set),
            readChannel.onSend(value::get)
        );
      }
    };
  }

  private Runnable newMutator(final ChannelSynchronous<Integer> writeChannel,
                              final ChannelSynchronous<Integer> readChannel) {
    return () -> {
      for(int i = 0; i < 5; ++i) {
        readChannel.receive( oldValue -> {
          final int newValue = oldValue + 1;
          writeChannel.send( () -> newValue);
        });
      }
    };
  }

}
