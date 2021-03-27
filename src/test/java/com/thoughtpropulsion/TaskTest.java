package com.thoughtpropulsion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskTest {

  private Continuation continuation;

  @BeforeEach
  private void beforeEach() {
    continuation = createContinuation();
  }

  @Test
  public void sameTimeDifferentRunnable() {
    final Task task1 = new Task(createContinuation(), 0);
    final Task task2 = new Task(createContinuation(), 0);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }

  @Test
  public void differentTimeSameRunnable() {
    final Task task1 = new Task(continuation, 0);
    final Task task2 = new Task(continuation, 1);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isNotEqualByComparingTo(task2);
  }

  @Test
  public void sameTimeSameRunnable() {
    final Task task1 = new Task(continuation, 0);
    final Task task2 = new Task(continuation, 0);
    assertThat(task1).isEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }

  private Continuation createContinuation() {
    return new Continuation() {
      public boolean isReady() {
        return false;
      }

      public void compute(final TaskScheduler scheduler) {
      }
    };
  }

}
