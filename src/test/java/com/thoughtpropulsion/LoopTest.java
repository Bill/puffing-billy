package com.thoughtpropulsion;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class LoopTest {

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  @Test
  public void loopOnce() {

    final AtomicInteger actualIterations = new AtomicInteger(0);

    scheduler.schedule( () -> {

      // for(int i = 0; i < 1; ++i)

      final AtomicInteger i = new AtomicInteger(0);

      if (i.get() < 1) {
        scheduler.schedule( () -> {
          actualIterations.incrementAndGet();
          i.incrementAndGet();
        });
      }
    });

    scheduler.triggerActions();

    assertThat(actualIterations.get()).isEqualTo(1);
  }
}
