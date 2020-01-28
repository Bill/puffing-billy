package com.thoughtpropulsion;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VirtualTimeTest {

  private VirtualTime virtualTime;
  private TestScheduler scheduler;

  @BeforeEach
  public void before() {
    virtualTime = new VirtualTime();
    scheduler = new TestScheduler(virtualTime);
  }

  @Test
  public void schedulerHoldsReadyTaskBeforeTriggerActions() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(()->flag.set(true));

    assertThat(flag.get()).isFalse().describedAs("scheduler ran task prematurely");
  }

  @Test
  public void triggerActionsRunsReadyTask() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(()->flag.set(true));

    scheduler.triggerActions();

    assertThat(flag.get()).isTrue().describedAs("scheduler failed to run task");
  }

  @Test
  public void triggerActionsHoldsFutureTask() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(()->flag.set(true), 1, MILLISECONDS);

    scheduler.triggerActions();

    assertThat(flag.get()).isFalse();
  }

  @Test
  public void recursiveTaskScheduling() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(()->{
      scheduler.schedule(()->flag.set(true));
    });

    scheduler.triggerActions();

    assertThat(flag.get()).isTrue();
  }

  @Test
  public void recursiveTaskSchedulingTwoSteps() {

    final AtomicBoolean flag = new AtomicBoolean(false);

    scheduler.schedule(()->{
      scheduler.schedule(()->flag.set(true), 1, MILLISECONDS);
    });

    scheduler.triggerActions();

    assertThat(flag.get()).isFalse().describedAs("triggerActions ran task prematurely");

    virtualTime.advance(1, MILLISECONDS);

    scheduler.triggerActions();

    assertThat(flag.get()).isTrue().describedAs("triggerActions failed to run ready task");
  }

  @Test
  public void tasksRunInTimeOrder() {

    final AtomicInteger flag = new AtomicInteger(0);

    // scheduling later task first just to see if that causes a problem
    scheduler.schedule(()->flag.compareAndSet(1,2), 2, MILLISECONDS);
    scheduler.schedule(()->flag.compareAndSet(0,1), 1, MILLISECONDS);

    virtualTime.advance(2, MILLISECONDS);

    scheduler.triggerActions();

    assertThat(flag.get()).isEqualTo(2).describedAs("tasks didn't run in time order");
  }

  @Test
  public void schedulingOrderBreaksTimeOrderTies() {

    final AtomicInteger flag = new AtomicInteger(0);

    scheduler.schedule(()->flag.compareAndSet(0,1));
    scheduler.schedule(()->flag.compareAndSet(1,2));

    scheduler.triggerActions();

    assertThat(flag.get()).isEqualTo(2)
        .describedAs("tasks with same start time didn't run in scheduling order");
  }

}
