package com.thoughtpropulsion.deterministic;

import com.thoughtpropulsion.ChannelReading;
import com.thoughtpropulsion.ChannelWriting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChannelBoundedTest {

  private ChannelBounded<Integer> channel1;
  private ChannelBounded<Integer> channel2;

  @BeforeEach
  private void beforeEach() {
    channel1 = new ChannelBounded<>(Integer.class,1);
    channel2 = new ChannelBounded<>(Integer.class, 2);
  }

  @Test
  public void mustNotBeSynchronous() {
    assertThatThrownBy(() -> new ChannelBounded<>(Integer.class, 0)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void canPutAndGet() {
    channel1.getWriting().put(1);
    assertThat(channel1.getReading().get()).isEqualTo(1);
  }

  @Test
  public void overflowThrows() {
    channel1.getWriting().put(1);
    assertThatThrownBy(() -> channel1.getWriting().put(2)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void underflowThrows() {
    assertThatThrownBy(() -> channel1.getReading().get()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void wraps() {
    final ChannelWriting<Integer> writing = channel1.getWriting();
    writing.put(1);
    final ChannelReading<Integer> reading = channel1.getReading();
    assertThat(reading.get()).isEqualTo(1);
    writing.put(2);
    assertThat(reading.get()).isEqualTo(2);
    assertThatThrownBy(() -> reading.get()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void wraps2() {
    final ChannelWriting<Integer> writing = channel2.getWriting();
    writing.put(1);
    writing.put(2);
    final ChannelReading<Integer> reading = channel2.getReading();
    assertThat(reading.get()).isEqualTo(1);
    writing.put(3);
    assertThat(reading.get()).isEqualTo(2);
    writing.put(4);
    assertThat(reading.get()).isEqualTo(3);
    assertThat(reading.get()).isEqualTo(4);
    assertThatThrownBy(() -> reading.get()).isInstanceOf(IllegalStateException.class);
  }
}