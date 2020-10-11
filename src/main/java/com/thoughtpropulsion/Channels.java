package com.thoughtpropulsion;

import java.util.List;

public class Channels {
  private Channels() {}

  // Apply processor when any channels are ready. Keep going until processor
  // returns false.
  public static void selectWhile( final List<ChannelReading> readingChannels,
                                  final List<ChannelWriting> writingChannels,
                                  final SelectProcessor processor) {

  }
}
