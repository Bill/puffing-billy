package com.thoughtpropulsion;

import com.thoughtpropulsion.test.TestScheduler;
import com.thoughtpropulsion.test.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.thoughtpropulsion.ControlStructures.ignoreResult;
import static com.thoughtpropulsion.ControlStructures.select;
import static com.thoughtpropulsion.ControlStructures.sequence;
import static org.assertj.core.api.Assertions.assertThat;

public class ChannelBoundedClosedTest {
  private VirtualTime virtualTime;
  private TestScheduler scheduler;
  private ChannelBounded<Integer> channel1;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
    channel1 = new ChannelBounded<>(Integer.class,1);
  }

  @Test
  public void close() {
    final ChannelWriting<Integer> writing = channel1.getWriting();
    writing.put(1);
    writing.close();
    final ChannelReading<Integer> reading = channel1.getReading();

    final int[] results = {0,0,0};
    final boolean[] closedResults = {false,false,false};

    scheduler.schedule(
      sequence(
        ignoreResult(select(reading.onReceiveOrClosed((value, isClosed) -> {
          results[0] = value;
          closedResults[0] = isClosed;
          return true;
        }))),
        ignoreResult(select(reading.onReceiveOrClosed((value, isClosed) -> {
          // don't care about value because isClosed
          closedResults[1] = isClosed;
          return true;
        }))),
        ignoreResult(select(reading.onReceiveOrClosed((value, isClosed) -> {
          throw new AssertionError("onReceive should not be invoked after close");
        })))));

    scheduler.triggerActions();

    assertThat(results[0]).isEqualTo(1);
    assertThat(closedResults[0]).isEqualTo(false);
    assertThat(closedResults[1]).isEqualTo(true);
  }

}
