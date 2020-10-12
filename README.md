# Puffing Billy&mdash;A Java Concurrency Framework Supporting Deterministic Simulation Testing

Java provides insufficient control of thread scheduling, particularly with regard to testing concurrent applications. This project provides an alternative concurrency framework, one that can be run on Java's native thread scheduling in production, but which can be run deterministically, in a single thread, during testing.

#Notes
Channel framework invariants:

* if a processor is triggered to read (write) a channel, and is fired, but does not read
   (write) that channel, then the value (opportunity) is offered to the next waiting processor

Synchronous channel invariants:

1. until a processor calls select with an intention to read (write) a channel, no processor
   is triggered to write to (read) that channel.

Generalization to channels with storage:

1. until space is available in a channel (data is available in a channel), or a processor
    calls select with an intention to read (write) a channel, no processor is triggered to
    write to (read) that channel.

Processing triggers:

* reader waiting -> ready for write
* writer waiting -> ready for read
* space available in channel -> ready for write
* data available in channel -> ready for read

It turns out that without the possibility of preemption, there can be no real notion of a "synchronous" channel. In this framework, no blocking is allowed. This means that all communication (over channels) is inherrently asynchronous. This in turn means that the minimum amount of buffering in a channel is 1 (not zero).