package com.thoughtpropulsion;

import java.lang.reflect.Array;
import java.util.function.Predicate;

public class ChannelBounded<T> implements ChannelBiDirectional<T> {

  private final T[] content;
  private int nextRead;
  private int nextWrite;
  private int size; // number of elements currently stored in channel

  private final ChannelReading<T> channelReading = new ChannelReading<T>() {
    @Override
    public boolean isReady() {
      return size > 0;
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
        public Predicate<T> getConsumer() {
          return receiver;
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
    public boolean isReady() {
      return size < content.length;
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
        public Predicate<ChannelWriting<T>> getPredicate() {
          return predicate;
        }
      };
    }

    @Override
    public void put(final T value) {
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
