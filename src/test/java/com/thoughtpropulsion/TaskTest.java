package com.thoughtpropulsion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskTest {

  private ReadyCompute readyCompute;

  @BeforeEach
  private void beforeEach() {
    readyCompute = createReadyCompute();
  }

  @Test
  public void sameTimeDifferentRunnable() {
    final Task task1 = new Task(createReadyCompute(), 0);
    final Task task2 = new Task(createReadyCompute(), 0);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }

  @Test
  public void differentTimeSameRunnable() {
    final Task task1 = new Task(readyCompute, 0);
    final Task task2 = new Task(readyCompute, 1);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isNotEqualByComparingTo(task2);
  }

  @Test
  public void sameTimeSameRunnable() {
    final Task task1 = new Task(readyCompute, 0);
    final Task task2 = new Task(readyCompute, 0);
    assertThat(task1).isEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }

  private ReadyCompute createReadyCompute() {
    return new ReadyCompute() {
      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void compute(final TaskScheduler scheduler) {
      }
    };
  }

}
