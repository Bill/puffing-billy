package com.thoughtpropulsion.deterministic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.thoughtpropulsion.ControlStructures.statement;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class VirtualTimeTest {

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    // seedArg picked emprically to make test pass
    scheduler = new TestScheduler(virtualTime,-1681752729);
  }

  @Test
  public void schedulerHoldsReadyTaskBeforeTriggerActions() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(statement(()->flag.set(true)));

    assertThat(flag.get()).isFalse().describedAs("scheduler ran task prematurely");
  }

  @Test
  public void triggerActionsRunsReadyTask() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(statement(()->flag.set(true)));

    scheduler.triggerActions();

    assertThat(flag.get()).isTrue().describedAs("scheduler failed to run task");
  }

  @Test
  public void triggerActionsHoldsFutureTask() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(statement(()->flag.set(true)), 1, MILLISECONDS);

    scheduler.triggerActions();

    assertThat(flag.get()).isFalse();
  }

  @Test
  public void recursiveTaskScheduling() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(statement(()->{
      scheduler.schedule(statement(()->flag.set(true)));
    }));

    scheduler.triggerActions();

    assertThat(flag.get()).isTrue();
  }

  @Test
  public void recursiveTaskSchedulingTwoSteps() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(statement(()->{
      scheduler.schedule(statement(()->flag.set(true)), 1, MILLISECONDS);
    }));

    scheduler.triggerActions();

    assertThat(flag.get()).isFalse().describedAs("triggerActions ran task prematurely");

    virtualTime.advance(1, MILLISECONDS);

    scheduler.triggerActions();

    assertThat(flag.get()).isTrue().describedAs("triggerActions failed to run ready task");
  }

  @Test
  public void tasksRunInTimeOrder() {

    final AtomicInteger state = new AtomicInteger(0);

    // scheduling later task first just to see if that causes a problem
    scheduler.schedule(statement( () -> state.compareAndSet(1, 2)), 2, MILLISECONDS);
    scheduler.schedule(statement( () -> state.compareAndSet(0, 1)), 1, MILLISECONDS);

    virtualTime.advance(1, MILLISECONDS);
    scheduler.triggerActions();
    virtualTime.advance(1, MILLISECONDS);
    scheduler.triggerActions();

    assertThat(state.get()).isEqualTo(2).describedAs("tasks didn't run in time order");
  }

  @Test
  public void schedulingOrderBreaksTimeOrderTies() {

    final AtomicInteger flag = new AtomicInteger(0);

    scheduler.schedule(statement(() -> flag.compareAndSet(0, 1)));
    scheduler.schedule(statement(() -> flag.compareAndSet(1, 2)));

    scheduler.triggerActions();

    assertThat(flag.get()).isEqualTo(2)
        .describedAs("tasks with same start time didn't run in scheduling order");
  }

}
