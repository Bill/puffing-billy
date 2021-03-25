package com.thoughtpropulsion;

import com.thoughtpropulsion.test.TestScheduler;
import com.thoughtpropulsion.test.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.thoughtpropulsion.ControlStructures.forLoop;
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
  public void testLoopN() {
    final int N = 4;
    /*
     For communicating a result we need a final (or at least "effectively final") variable
     that can be referenced from within the inner class. We could use an AtomicInteger, but
     we don't need the concurrency control it provides. A single-element array suffices.
     */
    final int[] actualIterations = {0};

    scheduler.schedule(forLoop(N, (i, _ignoredScheduler) -> {
      actualIterations[0] += i; // side-effect
    }));

    scheduler.triggerActions();

    assertThat(actualIterations[0]).isEqualTo(6);
  }

}
