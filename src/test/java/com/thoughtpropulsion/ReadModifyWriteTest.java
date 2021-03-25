package com.thoughtpropulsion;

import com.thoughtpropulsion.test.TestScheduler;
import com.thoughtpropulsion.test.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.thoughtpropulsion.ControlStructures.whileSelect;

public class ReadModifyWriteTest {

  public static final int NUM_MUTATORS = 2;
  public static final int NUM_MUTATIONS_PER_MUTATOR = 5;
  public static final int NUM_TOTAL_MUTATIONS = NUM_MUTATORS * NUM_MUTATIONS_PER_MUTATOR;

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  @Test
  public void runRegisterAndMutatorsTest() {

    // channel for feeding the register
    final ChannelBiDirectional<Integer> feedRegister = new ChannelBounded<>(Integer.class, NUM_TOTAL_MUTATIONS);

    // channel for reading from the register
    final ChannelBiDirectional<Integer> readRegister = new ChannelBounded<>(Integer.class, NUM_TOTAL_MUTATIONS);

    scheduler.schedule(newRegister(feedRegister.getReading(), readRegister.getWriting()));

    for (int i = 0; i < NUM_MUTATORS; ++i)
      scheduler.schedule(newMutator(readRegister.getReading(), feedRegister.getWriting(), i));

    scheduler.triggerActions();
  }

  private Consumer<TaskScheduler> newRegister(final ChannelReading<Integer> readChannel,
                                              final ChannelWriting<Integer> writeChannel) {
    return
      // introducing the Supplier around whileSelect() is a convenient way of introducing variables
      new Supplier<Consumer<TaskScheduler>>() {

        int value = 0;
        int iteration = 0;

        @Override
        public Consumer<TaskScheduler> get() {
          return whileSelect(
            readChannel.onReceive(newValue -> {
              System.out.println(String.format("register iteration: %d", iteration));
              value = newValue;
              System.out.println(String.format("register received: %d", value));
              return ++iteration < NUM_TOTAL_MUTATIONS;
            }),
            writeChannel.onSend(channelWriting -> {
              System.out.println(String.format("register iteration: %d", iteration));
              System.out.println(String.format("register sending: %d", value));
              channelWriting.put(value);
              System.out.println(String.format("register sent: %d", value));
              return iteration < NUM_TOTAL_MUTATIONS;
            })
          );
        }
      }.get(); // return whileSelect() with captured variables
  }

  private Consumer<TaskScheduler> newMutator(final ChannelReading<Integer> readChannel,
                                             final ChannelWriting<Integer> writeChannel, final int mutatorId) {

    return
      // introducing the Supplier around whileSelect() is a convenient way of introducing variables
      new Supplier<Consumer<TaskScheduler>>() {

        int value = 0;
        int iteration = 0;

        @Override
        public Consumer<TaskScheduler> get() {
          return whileSelect(
            readChannel.onReceive(newValue -> {
              System.out.println(String.format("mutator %d iteration: %d", mutatorId, iteration));
              System.out.println(String.format("mutator %d received: %d", mutatorId, value));
              value = newValue;
              return iteration < NUM_MUTATIONS_PER_MUTATOR;
            }),
            writeChannel.onSend(channelWriting -> {
              System.out.println(String.format("mutator %d iteration: %d", mutatorId, iteration));
              System.out.println(String.format("mutator %d sending: %d", mutatorId, value));
              channelWriting.put(value+1);
              System.out.println(String.format("mutator %d sent: %d", mutatorId, value+1));
              return ++iteration < NUM_MUTATIONS_PER_MUTATOR;
            })
          );
        }
      }.get(); // return whileSelect() with captured variables
  }
}
