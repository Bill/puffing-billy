package com.thoughtpropulsion;


import com.thoughtpropulsion.test.TestScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SelectWhileTest {

  private ChannelForTest<Integer> channelForTest;

  static class ChannelForTest<T> implements Channel<T> {

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


  private TaskScheduler scheduler;

  @BeforeEach
  public void before() {
    final NanoTime timeForTest = new NanoTime() {
      @Override
      public long nanoTime() {
        return 0;
      }
    };
    scheduler = new TestScheduler(timeForTest);
    channelForTest = new ChannelForTest<>();
  }

  @Test
  public void selectWhileIsEntrant() {
    final SelectProcessor noOpSelectProcessor = (suppliers, consumers) -> false;
    scheduler.selectWhile(singletonList(channelForTest.channelReading),
        singletonList(channelForTest.channelWriting), noOpSelectProcessor);
  }

  @Test
  public void selectWhileIsNotReEntrant() {
    final SelectProcessor noOpSelectProcessor = (suppliers, consumers) -> false;

    final SelectProcessor reEntrantSelectProcessor = (suppliers, consumers) -> {
      scheduler.selectWhile(singletonList(channelForTest.channelReading),
          singletonList(channelForTest.channelWriting), noOpSelectProcessor);
      return false;
    };

    assertThatThrownBy(() ->
        scheduler.selectWhile(
            singletonList(channelForTest.channelReading),
            singletonList(channelForTest.channelWriting),
            reEntrantSelectProcessor))
        .isInstanceOf(IllegalStateException.class);
  }
}
