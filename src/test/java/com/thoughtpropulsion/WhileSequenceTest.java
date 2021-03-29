package com.thoughtpropulsion;

import com.thoughtpropulsion.deterministic.TestScheduler;
import com.thoughtpropulsion.deterministic.VirtualTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.thoughtpropulsion.ControlStructures.sequence;
import static com.thoughtpropulsion.ControlStructures.statement;
import static com.thoughtpropulsion.ControlStructures.whileLoop;
import static org.assertj.core.api.Assertions.assertThat;

public class WhileSequenceTest {

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  private void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  @Test
  public void foo() {
    final AtomicInteger state = new AtomicInteger(0);
    final int N = 4;

    scheduler.schedule(
      new Supplier<Continuation>() {

        int i = 0;

        @Override
        public Continuation get() {
          return whileLoop(
            () -> i < N,
            sequence(
              statement(() -> state.compareAndSet(i + 0,i + 1)),
              statement(() -> state.compareAndSet(i + 1,i + 2)),
              statement(() -> ++i)
            ));
        }
      }.get()
    );

    scheduler.triggerActions();

    assertThat(state.get()).isEqualTo((N - 1) + 2);

  }
}
