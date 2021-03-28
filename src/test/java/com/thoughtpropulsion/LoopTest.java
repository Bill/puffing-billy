package com.thoughtpropulsion;

import com.thoughtpropulsion.deterministic.TestScheduler;
import com.thoughtpropulsion.deterministic.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.thoughtpropulsion.ControlStructures.statement;
import static com.thoughtpropulsion.ControlStructures.whileLoop;
import static org.assertj.core.api.Assertions.assertThat;

public class LoopTest {

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  /*
   This is how you implement a for loop using Puffing Billy. Introduce the loop variable, i, into
   a new closure so it's visible to the while loop condition and body. Test the loop variable in
   the condition and be sure to increment it in the body.
   */
  @Test
  public void testForLoopViaWhileLoop() {
    final int N = 4;

    final int[] actualIterations = {0};

    scheduler.schedule(new Supplier<Continuation>() {

      int i;

      @Override
      public Continuation get() {
        return whileLoop( () -> i < N, statement( () -> {
          ++actualIterations[0];
          ++i;
        }));
      }
    }.get());

    scheduler.triggerActions();

    assertThat(actualIterations[0]).isEqualTo(N);
  }
}
