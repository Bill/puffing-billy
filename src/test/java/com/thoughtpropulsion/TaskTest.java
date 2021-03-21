package com.thoughtpropulsion;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskTest {

  @Test
  public void sameTimeDifferentRunnable() {
    final Task task1 = new Task(scheduler -> {}, 0);
    final Task task2 = new Task(scheduler -> {}, 0);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }

  @Test
  public void differentTimeSameRunnable() {
    final Consumer<TaskScheduler> runnable = scheduler -> {};
    final Task task1 = new Task(runnable, 0);
    final Task task2 = new Task(runnable, 1);
    assertThat(task1).isNotEqualTo(task2);
    assertThat(task1).isNotEqualByComparingTo(task2);
  }

  @Test
  public void sameTimeSameRunnable() {
    final Consumer<TaskScheduler> runnable = scheduler -> {};
    final Task task1 = new Task(runnable, 0);
    final Task task2 = new Task(runnable, 0);
    assertThat(task1).isEqualTo(task2);
    assertThat(task1).isEqualByComparingTo(task2);
  }


}
