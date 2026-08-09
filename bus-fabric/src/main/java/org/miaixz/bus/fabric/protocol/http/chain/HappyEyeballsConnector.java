/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.protocol.http.chain;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Dispatcher-owned address-candidate race using one shared monotonic deadline.
 *
 * @author Kimi Liu
 */
final class HappyEyeballsConnector {

    /**
     * RFC 8305 fallback delay between the first and second candidate.
     */
    private static final Duration FALLBACK_DELAY = Duration.ofMillis(250);

    /**
     * Dispatcher owning every candidate task.
     */
    private final Dispatcher dispatcher;

    /**
     * Creates a connector.
     *
     * @param dispatcher runtime dispatcher
     */
    HappyEyeballsConnector(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Races the selected address candidates and cancels every loser immediately after the first success.
     *
     * @param candidates stable candidate order
     * @param offset     first candidate index
     * @param count      maximum candidates in this race
     * @param deadline   shared absolute deadline
     * @param attempt    blocking candidate connection function
     * @return winning connection and address
     */
    Result race(
            final List<InetAddress> candidates,
            final int offset,
            final int count,
            final long deadline,
            final Attempt attempt) {
        final CompletableFuture<Result> winner = new CompletableFuture<>();
        final List<CompletableFuture<Result>> tasks = new CopyOnWriteArrayList<>();
        final AtomicInteger failures = new AtomicInteger();
        final AtomicReference<RuntimeException> primary = new AtomicReference<>();
        for (int index = 0; index < count; index++) {
            if (winner.isDone()) {
                break;
            }
            final InetAddress candidate = candidates.get(offset + index);
            final String key = "http:connect-candidate:" + candidate.getHostAddress();
            final CompletableFuture<Result> task = index == 0 ? connect(key, winner, candidate, deadline, attempt)
                    : delayedConnect(key, winner, candidate, deadline, attempt);
            tasks.add(task);
            task.whenComplete((result, cause) -> {
                if (result != null) {
                    if (winner.complete(result)) {
                        for (final CompletableFuture<Result> other : tasks) {
                            if (other != task) {
                                other.cancel(true);
                            }
                        }
                    } else {
                        result.connection().close();
                    }
                    return;
                }
                if (cause == null || task.isCancelled()) {
                    return;
                }
                final RuntimeException failure = unwrap(cause);
                final RuntimeException first = primary.get();
                if (first == null) {
                    primary.compareAndSet(null, failure);
                } else if (first != failure) {
                    first.addSuppressed(failure);
                }
                if (failures.incrementAndGet() == count) {
                    winner.completeExceptionally(primary.get());
                }
            });
        }
        final long remaining = deadline == Long.MAX_VALUE ? Long.MAX_VALUE : deadline - System.nanoTime();
        if (remaining <= 0L) {
            tasks.forEach(task -> task.cancel(true));
            throw new TimeoutException("Socket connect timed out");
        }
        try {
            return remaining == Long.MAX_VALUE ? winner.get() : winner.get(remaining, TimeUnit.NANOSECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            tasks.forEach(task -> task.cancel(true));
            throw new TimeoutException("Socket connect timed out", e);
        } catch (final InterruptedException e) {
            tasks.forEach(task -> task.cancel(true));
            Thread.currentThread().interrupt();
            throw new SocketException("Socket connect race was interrupted", e);
        } catch (final ExecutionException e) {
            throw unwrap(e.getCause());
        }
    }

    /**
     * Starts one blocking candidate on the Dispatcher background channel.
     *
     * @param key       dispatch key
     * @param tag       shared race cancellation tag
     * @param candidate resolved candidate
     * @param deadline  shared deadline
     * @param attempt   connect operation
     * @return cancellable result
     */
    private CompletableFuture<Result> connect(
            final String key,
            final Object tag,
            final InetAddress candidate,
            final long deadline,
            final Attempt attempt) {
        return dispatcher.backgroundSupply(key, tag, () -> new Result(attempt.connect(candidate, deadline), candidate));
    }

    /**
     * Schedules the fallback candidate without occupying a bounded background permit during the stagger.
     *
     * @param key       dispatch key
     * @param tag       shared race cancellation tag
     * @param candidate resolved candidate
     * @param deadline  shared deadline
     * @param attempt   connect operation
     * @return cancellable result spanning both delayed and background phases
     */
    private CompletableFuture<Result> delayedConnect(
            final String key,
            final Object tag,
            final InetAddress candidate,
            final long deadline,
            final Attempt attempt) {
        final CompletableFuture<Result> result = new CompletableFuture<>();
        final AtomicReference<CompletableFuture<Result>> running = new AtomicReference<>();
        final DispatchHandle delayed = dispatcher
                .schedule(key + ":delay", FALLBACK_DELAY, Activity.of(key + ":delay", () -> {
                    if (result.isDone()) {
                        return;
                    }
                    final CompletableFuture<Result> operation = connect(key, tag, candidate, deadline, attempt);
                    running.set(operation);
                    operation.whenComplete((value, cause) -> {
                        if (cause == null) {
                            result.complete(value);
                        } else {
                            result.completeExceptionally(cause);
                        }
                    });
                }));
        result.whenComplete((value, cause) -> {
            if (result.isCancelled()) {
                dispatcher.cancel(delayed);
                final CompletableFuture<Result> operation = running.get();
                if (operation != null) {
                    operation.cancel(true);
                }
            }
        });
        return result;
    }

    /**
     * Converts asynchronous wrapper failures to the protocol runtime exception.
     */
    private static RuntimeException unwrap(final Throwable cause) {
        Throwable current = cause;
        while (current instanceof CompletionException || current instanceof ExecutionException) {
            current = current.getCause();
        }
        return current instanceof RuntimeException runtime ? runtime
                : new SocketException("Socket connect failed", current);
    }

    /**
     * Blocking attempt executed only on the Dispatcher background channel.
     */
    @FunctionalInterface
    interface Attempt {

        /**
         * Opens one candidate.
         *
         * @param candidate resolved address
         * @param deadline  shared deadline
         * @return connection
         */
        Connection connect(InetAddress candidate, long deadline);
    }

    /**
     * Winning candidate.
     *
     * @param connection opened connection
     * @param candidate  resolved winning address
     */
    record Result(Connection connection, InetAddress candidate) {
    }

}
