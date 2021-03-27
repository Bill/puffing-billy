package com.thoughtpropulsion;

import com.thoughtpropulsion.test.TestScheduler;
import com.thoughtpropulsion.test.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.thoughtpropulsion.ControlStructures.sequence;
import static com.thoughtpropulsion.ControlStructures.statement;
import static org.assertj.core.api.Assertions.assertThat;

class SequenceTest {

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  @Test
  public void sequenceTest() {

    final AtomicInteger state = new AtomicInteger(0);

    scheduler.schedule(
      sequence(
        statement(() -> state.compareAndSet(0,1)),
        statement(() -> state.compareAndSet(1,2))
      )
    );

    scheduler.triggerActions();

    assertThat(state.get()).isEqualTo(2);
  }


}