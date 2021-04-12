![Steam locomotive Puffing Billy shortly before it was handed over to the museum in 1862; public domain image from Wikipedia](documentation/images/puffing-billy.jpg)

# Puffing Billy&mdash;A Java Concurrency Framework Supporting Deterministic Simulation Testing

Java provides insufficient control of thread scheduling, particularly with regard to testing concurrent applications. This project provides an alternative concurrency framework, one that can be run on Java's native thread scheduling in production, but which can be run deterministically, in a single thread, during testing.

This framework implements parts of a "modern CSP" (communicating sequential processes) model à la Clojure (`core.async`), Kotlin, and Go. The `Continuation` concept here maps closely to Coroutines/goroutines. Continuations are scheduled to run and they communicate with one another exclusively through channels.

Puffing Billy is not recommended for anyone, ever. It is tedious and painful to use. You should use Kotlin instead.

Well actually, while Kotlin and the alternative modern CSP's are all very good and much nicer to program than Puffing Billy, none of them provides deterministic simulation testing yet. I entered a bug report ([kotlinx.coroutines issue #1630](https://github.com/Kotlin/kotlinx.coroutines/issues/1630)) and pull request ([kotlinx.coroutines pull request #1629](https://github.com/Kotlin/kotlinx.coroutines/pull/1629)) on kotlinx.coroutines in 2019 to begin adding support, but so far no one has taken notice. Maybe Puffing Billy will help generate support for the idea.

# Things That Work In Deterministic Mode
* schedule tasks to run now or in the future
* time is "virtual", i.e. under the control of your test&mdash;move it forward (or not) as needed
* while loops, for loops, sequences (of statements) are all available and work deterministically
* coroutines can communicate through channels: the `select(…)` statement takes a bunch of read/write clauses for channels and completes only after one of the clauses has run
* tests can be started with a time-based (nondeterministic) PRNG seed by specifying `-Dnondeterministic` (Java system property) to the Java VM. Doing this multiple times constitutes a "state space search" of various possible (legal) interleavings of the (concurrent) coroutines. The seed will be logged to standard output so when you find a failing seed you can capture it in your regression tests.
* the `Channel` class is generic; most tests use `Channel<Integer>` for simplicity
* channels can be _closed_; once a channel is closed, no more writes are allowed to it; after all channel content is consumed an `onReceiveOrClosed()` will be called with `isClosed` set to `true`
* (there is currently no real/parallelizing/nondeterministic mode implemented)

# TODO
* Test the deterministic simulation for nondeterminism by adding test at end (of each test) to generate random number and verify it matches number generated from previous run. This needs to be part of the testing framework itself. Provide a way for a user to provide the expected value.
* implement real/parallelizing/nondeterministic runtime on `ScheduleThreadPoolExecutor` with a concurrency-capable channel implementation.
* implement structured concurrency

# FAQ

1. Q: What is [Billy](https://github.com/Bill/) puffing? A: This project was named for a neat little train: [Puffing Billy](https://en.wikipedia.org/wiki/Puffing_Billy_(locomotive))!
2. Q: But why? A: Puffing Billy is not recommended for anyone, ever. It is tedious and painful to use. Puffing Billy was implemented solely as an experiment to see how much work it would be, and how it would look. Trust me: it's awful.
