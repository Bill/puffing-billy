package com.thoughtpropulsion;

import java.util.function.Function;

@FunctionalInterface
public interface SuspendFunction<R> extends Function<TaskScheduler, R> {
}
