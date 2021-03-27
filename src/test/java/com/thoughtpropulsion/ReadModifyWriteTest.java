package com.thoughtpropulsion;

import com.thoughtpropulsion.deterministic.TestScheduler;
import com.thoughtpropulsion.deterministic.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.thoughtpropulsion.ControlStructures.select;
import static com.thoughtpropulsion.ControlStructures.sequence;
import static com.thoughtpropulsion.ControlStructures.whileLoop;

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
    final ChannelBiDirectional<Integer> feedRegister = scheduler.createBoundedChannel(Integer.class, NUM_TOTAL_MUTATIONS);

    // channel for reading from the register
    final ChannelBiDirectional<Integer> readRegister = scheduler.createBoundedChannel(Integer.class, NUM_TOTAL_MUTATIONS);

    scheduler.schedule(newRegister(feedRegister.getReading(), readRegister.getWriting()));

    for (int i = 0; i < NUM_MUTATORS; ++i)
      scheduler.schedule(newMutator(readRegister.getReading(), feedRegister.getWriting(), i));

    scheduler.triggerActions();
  }

  private Continuation newRegister(final ChannelReading<Integer> readChannel,
                                   final ChannelWriting<Integer> writeChannel) {
    return
      // introducing the Supplier around whileSelect() is a convenient way of introducing variables
      new Supplier<Continuation>() {

        int value = 0;
        int iteration = 0;

        @Override
        public Continuation get() {
          return
            whileLoop(
              () -> iteration < NUM_TOTAL_MUTATIONS,
              sequence(
                select(
                  readChannel.onReceive(newValue -> {
                    value = newValue;
                    log("received: " + value);
                  })),
                select(
                  writeChannel.onSend(channelWriting -> {
                    log("sending: " + value);
                    channelWriting.put(value);
                    log("sent: " + value);
                    ++iteration;
                  }))));
        }

        private void log(final String msg) {
          System.out.println(String.format("register iteration: %d: ", iteration) + msg);
        }
      }.get(); // return whileLoop() with captured variables
  }

  private Continuation newMutator(final ChannelReading<Integer> readChannel,
                                  final ChannelWriting<Integer> writeChannel, final int mutatorId) {

    return
      // introducing the Supplier around whileSelect() is a convenient way of introducing variables
      new Supplier<Continuation>() {

        int value = 0;
        int iteration = 0;

        @Override
        public Continuation get() {
          return
            whileLoop(
              () -> iteration < NUM_MUTATIONS_PER_MUTATOR,
              sequence(
                select(
                  readChannel.onReceive(newValue -> {
                    value = newValue;
                    log("received: " + value);
                  })),
                select(
                  writeChannel.onSend(channelWriting -> {
                    final int toSend = value + 1;
                    log("sending: " + toSend);
                    channelWriting.put(toSend);
                    log("sent: " + toSend);
                    ++iteration;
                  }))));
        }

        private void log(final String msg) {
          System.out.println(String.format("mutator %d iteration: %d: ", mutatorId, iteration) + msg);
        }
      }.get(); // return whileSelect() with captured variables
  }
}
