package com.thoughtpropulsion;

import org.junit.jupiter.api.Test;

import java.util.PriorityQueue;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityQueueTest {

  @Test
  public void holdsDuplicates() {
    final PriorityQueue<Task> queue = new PriorityQueue<>();
    final Task task = new Task(scheduler -> {
    }, 0);
    queue.add(task);
    queue.add(task);
    assertThat(queue.size()).isEqualTo(2);
    assertThat(queue).containsExactly(task,task);
  }

}
