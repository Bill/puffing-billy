package com.thoughtpropulsion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TaskTest {

  @Test
  public void sameTimeDifferentRunnable() {
    final Task task1 = new Task(() -> {}, 0);
    final Task task2 = new Task(() -> {}, 0);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }

  @Test
  public void differentTimeSameRunnable() {
    final Runnable runnable = () -> {};
    final Task task1 = new Task(runnable, 0);
    final Task task2 = new Task(runnable, 1);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isNotEqualByComparingTo(task2);
  }

  @Test
  public void sameTimeSameRunnable() {
    final Runnable runnable = () -> {};
    final Task task1 = new Task(runnable, 0);
    final Task task2 = new Task(runnable, 0);
    assertThat(task1).isEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }


}
