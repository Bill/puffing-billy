package com.thoughtpropulsion;

import static java.util.Collections.singletonList;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.thoughtpropulsion.test.TestScheduler;
import com.thoughtpropulsion.test.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  private static class RegisterData {
    public int value;
    public int iteration;
  }

  private Consumer<TaskScheduler> newRegister(final ChannelReading<Integer> readChannel,
                                              final ChannelWriting<Integer> writeChannel) {
    return scheduler -> {
      /*
       No concurrency control is required on the register data. Because it's accessed in
       a lambda, we have to have a final reference. But the object referred to can be
       mutated in the lambda with no concurrency control because at most one thread will
       be in the lambda at a time.

       Question: will it be the _same_ thread though? If it's a different thread then we
       will have _visibility_ problems unless we use volatiles (or AtomicXXX.)
       What if the framework could guarantee that the _same_ thread always ran a particular
       lambda (instance)? That would free us from having to use volatiles etc.
       */
      final RegisterData registerData = new RegisterData();

      scheduler.selectWhile(
          singletonList(readChannel),
          singletonList(writeChannel),
          (suppliers, consumers) -> {
            final Supplier<Integer> input = suppliers.get(readChannel);
            if (input != null)
              registerData.value = input.get();
            final Consumer<Integer> output = consumers.get(writeChannel);
            if (output != null)
              output.accept(input.get());
            return ++registerData.iteration < NUM_TOTAL_MUTATIONS;
          });
    };
  }

  private Consumer<TaskScheduler> newMutator(final ChannelReading<Integer> readChannel,
                                             final ChannelWriting<Integer> writeChannel) {
    return scheduler -> {
      // We need a final reference to share with the lambda. This array suffices.
      final int[] iteration = {0};

      scheduler.selectWhile(
          singletonList(readChannel),
          singletonList(writeChannel),
          (suppliers, consumers) -> {
            final Supplier<Integer> oldValue = suppliers.get(readChannel);
            if (oldValue != null) {
              final Consumer<Integer> output = consumers.get(writeChannel);
              if (output != null) {
                final int newValue = oldValue.get() + 1;
                output.accept(newValue);
              }
            }
            return ++iteration[0] < NUM_MUTATIIONS_PER_MUTATOR;
          });
    };
  }
}
