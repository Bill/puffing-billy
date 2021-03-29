package com.thoughtpropulsion;

import com.thoughtpropulsion.deterministic.TestScheduler;
import com.thoughtpropulsion.deterministic.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.thoughtpropulsion.ControlStructures.select;
import static com.thoughtpropulsion.ControlStructures.sequence;
import static com.thoughtpropulsion.ControlStructures.statement;
import static com.thoughtpropulsion.ControlStructures.whileLoop;
import static com.thoughtpropulsion.ControlStructures.whileSelect;
import static org.assertj.core.api.Assertions.assertThat;

public class ReadModifyWriteTest {

  public static final int NUM_MUTATORS = 1;
  public static final int NUM_MUTATIONS_PER_MUTATOR = 2;
  public static final int NUM_TOTAL_MUTATIONS = NUM_MUTATORS * NUM_MUTATIONS_PER_MUTATOR;

  private VirtualTime virtualTime;
  private TestScheduler scheduler;
  private boolean doLogging;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime, -445583651);
    doLogging = false;
  }

  @Test
  public void runRegisterAndMutatorsTest() {

    // channel for reading from the register
    final ChannelBiDirectional<ChannelWriting> readRegister =
      scheduler.createBoundedChannel(ChannelWriting.class, 1);

    // channel for writing the register
    final ChannelBiDirectional<Integer> writeRegister =
      scheduler.createBoundedChannel(Integer.class, 1);

    scheduler.schedule(newRegister(readRegister.getReading(), writeRegister.getReading()));

    for (int i = 0; i < NUM_MUTATORS; ++i)
      scheduler.schedule(newMutator(readRegister.getWriting(), writeRegister.getWriting(), i, scheduler));

    scheduler.triggerActions();
  }

  private Continuation newRegister(
    final ChannelReading<ChannelWriting> readRequests,
    final ChannelReading<Integer> writeValues) {
    return
      // introducing the Supplier around whileSelect() is a convenient way of introducing variables
      new Supplier<Continuation>() {

        int value = 0;

        @Override
        public Continuation get() {
          return
            whileSelect(
              readRequests.onReceive( responseChannel -> {
                log("responding with: " + value);
                // can't put directly to the responseChannel--have to do it in an onSend()
                scheduler.schedule(select(responseChannel.onSend(_ignored -> {responseChannel.put(value);})));
              }),
              writeValues.onReceive( newValue -> {
                log("setting register: " + newValue);
                assertThat(newValue).as("lost update").isEqualTo(value + 1);
                value = newValue;
              }));
        }

        private void log(final String msg) {
          ReadModifyWriteTest.this.log("register: " + msg);
        }
      }.get(); // return continuation with captured variables
  }

  private Continuation newMutator(final ChannelWriting<ChannelWriting> readRequests,
                                  final ChannelWriting<Integer> writeValues,
                                  final int mutatorId,
                                  final TaskScheduler scheduler) {
    return
      // introducing the Supplier around whileSelect() is a convenient way of introducing variables
      new Supplier<Continuation>() {

        int recentValue = 0;
        int iteration = 0;
        final ChannelBiDirectional<Integer> readResponses =
          scheduler.createBoundedChannel(Integer.class, 1);

        @Override
        public Continuation get() {
          return whileLoop( () -> iteration < NUM_TOTAL_MUTATIONS,
            sequence(
              // send read request
              // TODO: overload onSend on Runnable and BooleanSupplier since call site already has ChannelWriting
              select( readRequests.onSend( _ignored -> {
                log("requesting new value");
                readRequests.put(readResponses.getWriting());
              })),
              // read the response
              select( readResponses.getReading().onReceive( newValue -> {
                log("new value received: " + newValue);
                recentValue = newValue;
              })),
              // write a new value
              select(writeValues.onSend( _ignored -> {
                final int updateValue = recentValue + 1;
                log("writing: " + updateValue);
                writeValues.put(updateValue);
              })),
              statement( () -> ++iteration)
            ));
        }

        private void log(final String msg) {
          ReadModifyWriteTest.this.log(String.format("mutator %d iteration: %d: ", mutatorId, iteration) + msg);
        }
      }.get(); // return whileSelect() with captured variables
  }

  private void log(final String msg) {
    if (doLogging) {
      System.out.println(msg);
    }
  }
}
