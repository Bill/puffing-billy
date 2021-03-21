package com.thoughtpropulsion;

import static com.thoughtpropulsion.ControlStructures.nTimes;
import static org.assertj.core.api.Assertions.assertThat;

import com.thoughtpropulsion.test.TestScheduler;
import com.thoughtpropulsion.test.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    final int N = 3;
    /*
     For communicating a result we need a final (or at least "effectively final") variable
     that can be referenced from within the inner class. We could use an AtomicInteger, but
     we don't need the concurrency control it provides. A single-element array suffices.
     */
    final int[] actualIterations = {0};

    scheduler.schedule(nTimes(N, _ignoredScheduler -> {
      actualIterations[0] += 1; // side-effect
    }));

    scheduler.triggerActions();

    assertThat(actualIterations[0]).isEqualTo(N);
  }

}
