package com.thoughtpropulsion.deterministic;

import com.thoughtpropulsion.ChannelBiDirectional;
import com.thoughtpropulsion.ChannelReading;
import com.thoughtpropulsion.ChannelWriting;
import com.thoughtpropulsion.ReceiveClause;
import com.thoughtpropulsion.ReceiveOrClosedClause;
import com.thoughtpropulsion.SendClause;

import java.lang.reflect.Array;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChannelBounded<T> implements ChannelBiDirectional<T> {

  private final T[] content;
  private int nextRead;
  private int nextWrite;
  private int size; // number of elements currently stored in channel
  private enum ClosingState {
    /*
     initial state. If close() is never called on channelWriting we'll stay in this state
     and coroutines will have to stop due to some other signal
     */
    Open,

    /*
     This is an intermediate state after receiving close() on channelWriting.
     In this state:
       * no more writes are allowed
       * we're always ready for reading
       * first subsequent read results in: onReceiveOrClosed receiver gets false as second arg
         then state moves immediately to Closed
         also: onReceive receiver is never called in this state
     */
    Closing,

    /*
     final closed state
     */
    Closed
  };
  private ClosingState closingState;

  private final ChannelReading<T> channelReading = new ChannelReading<T>() {
    @Override
    public boolean isReady() {
      // in closing state it is as if we have an (one) extra element
      return size > 0 || closingState == ClosingState.Closing;
    }

    @Override
    public ReceiveClause<T> onReceive(final Predicate<T> receiver) {
      final ChannelReading<T> self = this;
      return new ReceiveClause<T>() {
        @Override
        public ChannelReading<T> getChannel() {
          return self;
        }

        @Override
        public BooleanSupplier getClause() {
          return () -> {
            assert isReady() : "activated onReceive clause when channel wasn't ready";
            final T value = get();
            switch(closingState) {
              case Open:
                return receiver.test(value);
              case Closing:
                closingState = ClosingState.Closed;
                return true;
              case Closed:
                throw new IllegalStateException("Bug in scheduler: invoked onReceive() receiver for a closed channel");
            }
            return true; // never get here
          };
        }
      };
    }

    @Override
    public ReceiveClause<T> onReceive(final Consumer<T> receiver) {
      return onReceive( value -> {receiver.accept(value); return true;});
    }

    @Override
    public ReceiveOrClosedClause<T> onReceiveOrClosed(final BiPredicate<T, Boolean> receiver) {
      final ChannelReading<T> self = this;
      return new ReceiveOrClosedClause<T>() {
        @Override
        public ChannelReading<T> getChannel() {
          return self;
        }
        @Override
        public BooleanSupplier getClause() {
          return () -> {
            assert isReady() : "activated onReceiveOrClosed clause when channel wasn't ready";
            switch(closingState) {
              case Open:
                return receiver.test(get(), false);
              case Closing:
                if (size > 0) {
                  return receiver.test(get(), false);
                } else {
                  closingState = ClosingState.Closed;
                  /*
                   Receive the synthetic "closing" value. Receiver should ignore the first argument,
                   but we have to pass something.
                   */
                  return receiver.test(content[0], true);
                }
              case Closed:
                throw new IllegalStateException("Bug in scheduler: invoked onReceiveOrClosed() receiver for a closed channel");
            }
            return true; // never get here
          };
        }
      };
    }

    @Override
    public T get() {
      if (size > 0) {
        final T result = content[nextRead];
        nextRead = (nextRead + 1) % content.length;
        size -= 1;
        return result;
      } else {
        throw new IllegalStateException("Channel is empty.");
      }
    }
  };

  private final ChannelWriting<T> channelWriting = new ChannelWriting<>() {
    @Override
    public void close() {
      closingState = ClosingState.Closing;
    }

    @Override
    public boolean isReady() {
      return size < content.length && closingState == ClosingState.Open;
    }

    @Override
    public SendClause<T> onSend(final Predicate<ChannelWriting<T>> predicate) {
      final ChannelWriting<T> self = this;

      return new SendClause<>() {
        @Override
        public ChannelWriting<T> getChannel() {
          return self;
        }

        @Override
        public BooleanSupplier getClause() {
          return () -> {
            assert isReady() : "activated onSend clause when channel wasn't ready";
            return predicate.test(getChannel());
          };
        }
      };
    }

    @Override
    public SendClause<T> onSend(final Consumer<ChannelWriting<T>> predicate) {
      return onSend(channelWriting -> {predicate.accept(channelWriting); return true;});
    }

    @Override
    public void put(final T value) {
      if (closingState != ClosingState.Open) {
        throw new IllegalStateException("can't put in state: " + closingState);
      }
      if (storageAvailable()) {
        content[nextWrite] = value;
        nextWrite = (nextWrite + 1) % content.length;
        size += 1;
      } else {
        throw new IllegalStateException("Channel is full.");
      }
    }
  };

  public ChannelBounded(final Class<T> clazz, final int bound) {
    /*
     This framework has no blocking reads or writes. That's because we have no way to suspend a reader (writer.)
     As a result all channels must have capacity to store one content item.
     */
    if (bound < 1) {
      throw new IllegalArgumentException("Illegal channel size: " + bound + ". Must be greater than zero.");
    }
    this.content = (T[]) Array.newInstance(clazz, bound);
    nextRead = nextWrite = size = 0;
    closingState = ClosingState.Open;
  }

  @Override
  public ChannelReading<T> getReading() {
    return channelReading;
  }

  @Override
  public ChannelWriting<T> getWriting() {
    return channelWriting;
  }

}
