package com.thoughtpropulsion;

import com.thoughtpropulsion.test.TestScheduler;
import com.thoughtpropulsion.test.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

public class ReadModifyWriteTest {

  public static final int NUM_MUTATORS = 2;
  public static final int NUM_MUTATIIONS_PER_MUTATOR = 5;
  public static final int NUM_TOTAL_MUTATIONS = NUM_MUTATORS * NUM_MUTATIIONS_PER_MUTATOR;

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  @Test
  public void foo() {

    // channel for feeding the register
    final Channel<Integer> feedRegister = new ChannelSynchronous<>();

    // channel for reading from the register
    final Channel<Integer> readRegister = new ChannelSynchronous<>();

    scheduler.schedule(newRegister(feedRegister.getReading(), readRegister.getWriting()));

    for (int i = 0; i < NUM_MUTATORS; ++i)
      scheduler.schedule(newMutator(readRegister.getReading(), feedRegister.getWriting()));

    scheduler.triggerActions();
  }

  private Consumer<TaskScheduler> newRegister(final ChannelReading<Integer> readChannel,
                                              final ChannelWriting<Integer> writeChannel) {
    return scheduler -> {

      scheduler.selectWhile(
        singletonList(readChannel),
        singletonList(writeChannel),
        new SelectProcessor() {
          public int value = 0;
          public int iteration = 0;

          @Override
          public boolean apply(final Map<ChannelReading, Supplier> suppliers, final Map<ChannelWriting, Consumer> consumers) {
            final Supplier<Integer> input = suppliers.get(readChannel);
            if (input != null)
              value = input.get();
            final Consumer<Integer> output = consumers.get(writeChannel);
            if (output != null)
              output.accept(input.get());
            return ++iteration < NUM_TOTAL_MUTATIONS;
          }
        });
    };
  }

  private Consumer<TaskScheduler> newMutator(final ChannelReading<Integer> readChannel,
                                             final ChannelWriting<Integer> writeChannel) {
    return scheduler -> {

      scheduler.selectWhile(
        singletonList(readChannel),
        singletonList(writeChannel),
        new SelectProcessor() {
          int iteration = 0;

          @Override
          public boolean apply(final Map<ChannelReading, Supplier> suppliers, final Map<ChannelWriting, Consumer> consumers) {
            final Supplier<Integer> oldValue = suppliers.get(readChannel);
            if (oldValue != null) {
              final Consumer<Integer> output = consumers.get(writeChannel);
              if (output != null) {
                final int newValue = oldValue.get() + 1;
                output.accept(newValue);
              }
            }
            return ++iteration < NUM_MUTATIIONS_PER_MUTATOR;
          }
        });
    };
  }
}
