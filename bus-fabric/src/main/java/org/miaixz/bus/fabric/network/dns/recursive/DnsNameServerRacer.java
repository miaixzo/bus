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
package org.miaixz.bus.fabric.network.dns.recursive;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.forward.DnsForwarder;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsDecodedResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;

/**
 * Concurrent name-server racer used by recursive referral resolution.
 *
 * <p>
 * A racer sends the same DNS request to the current healthy name servers concurrently and returns the first response
 * that can be decoded and is not {@code SERVFAIL}. Failed name servers are recorded in a shared health table and are
 * tried only after all currently healthy candidates fail.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DnsNameServerRacer {

    /**
     * Delay before a failed name server re-enters the healthy racing group.
     */
    private static final Duration FAILED_RETRY_DELAY = Duration.ofSeconds(10);

    /**
     * EWMA old-sample weight used for round-trip time smoothing.
     */
    private static final int RTT_EWMA_OLD_WEIGHT = 7;

    /**
     * EWMA divisor used for round-trip time smoothing.
     */
    private static final int RTT_EWMA_DIVISOR = 8;

    /**
     * Shared health state keyed by name-server endpoint.
     */
    private static final ConcurrentHashMap<String, HealthState> HEALTH = new ConcurrentHashMap<>();

    /**
     * Candidate name servers.
     */
    private final List<DnsUpstream> nameServers;

    /**
     * Creates a name-server racer.
     *
     * @param nameServers candidate name-server upstreams
     */
    public DnsNameServerRacer(final List<DnsUpstream> nameServers) {
        if (nameServers == null || nameServers.isEmpty()) {
            throw new ValidateException("DNS name-server racer candidates must not be empty");
        }
        for (final DnsUpstream nameServer : nameServers) {
            if (nameServer == null) {
                throw new ValidateException("DNS name-server racer candidates must not contain null");
            }
        }
        this.nameServers = List.copyOf(nameServers);
    }

    /**
     * Races one DNS request across candidate name servers.
     *
     * @param query   decoded DNS query used to build SERVFAIL when every candidate fails
     * @param request original DNS request wire bytes
     * @return first legal DNS response, or a SERVFAIL response when every candidate fails
     */
    public byte[] race(final DnsQuery query, final byte[] request) {
        return race(query, request, DnsRetryBudget.recursive()).response();
    }

    /**
     * Races one DNS request across candidate name servers while consuming a retry budget.
     *
     * @param query   decoded DNS query used to build SERVFAIL when every candidate fails
     * @param request original DNS request wire bytes
     * @param budget  retry budget shared by the recursive flow
     * @return race response and remaining budget
     */
    public DnsNameServerRace race(final DnsQuery query, final byte[] request, final DnsRetryBudget budget) {
        if (query == null) {
            throw new ValidateException("DNS name-server race query must not be null");
        }
        if (request == null || request.length == 0 || request.length > DnsCodec.MAX_MESSAGE_BYTES) {
            throw new ValidateException("DNS name-server race request length is invalid");
        }
        if (budget == null) {
            throw new ValidateException("DNS name-server race retry budget must not be null");
        }
        final List<DnsUpstream> healthy = healthyCandidates(nameServers);
        final List<DnsUpstream> failed = failedCandidates(nameServers);
        DnsRetryBudget cursor = budget;
        RaceGroup result = new RaceGroup(RaceResult.failure(null), cursor);
        if (!healthy.isEmpty()) {
            result = raceGroup(healthy, request, cursor);
            cursor = result.budget();
            if (result.result().success()) {
                return new DnsNameServerRace(result.result().response(), cursor);
            }
        }
        if (!failed.isEmpty()) {
            result = raceGroup(failed, request, cursor);
            cursor = result.budget();
            if (result.result().success()) {
                return new DnsNameServerRace(result.result().response(), cursor);
            }
        }
        return new DnsNameServerRace(servfail(query), cursor);
    }

    /**
     * Returns the current health snapshot for one name server.
     *
     * @param nameServer name-server upstream
     * @return immutable health snapshot
     */
    public DnsNameServerHealth health(final DnsUpstream nameServer) {
        if (nameServer == null) {
            throw new ValidateException("DNS name-server health upstream must not be null");
        }
        final HealthState state = HEALTH.get(healthKey(nameServer));
        final long now = System.nanoTime();
        if (state == null) {
            return new DnsNameServerHealth(true, 0, Long.MAX_VALUE);
        }
        return new DnsNameServerHealth(state.retryAfterNanos <= now, state.consecutiveFailures, state.rttNanos);
    }

    /**
     * Returns the immutable candidate name servers.
     *
     * @return candidate name servers
     */
    public List<DnsUpstream> nameServers() {
        return nameServers;
    }

    /**
     * Races a single healthy or failed candidate group.
     *
     * @param candidates candidate group
     * @param request    DNS request wire bytes
     * @param budget     retry budget available to this group
     * @return race result
     */
    private static RaceGroup raceGroup(
            final List<DnsUpstream> candidates,
            final byte[] request,
            final DnsRetryBudget budget) {
        final ExecutorService executor = Executors.newFixedThreadPool(candidates.size(), threadFactory());
        final ExecutorCompletionService<RaceResult> completion = new ExecutorCompletionService<>(executor);
        final ArrayList<Future<RaceResult>> futures = new ArrayList<>(candidates.size());
        DnsRetryBudget cursor = budget;
        try {
            for (final DnsUpstream candidate : candidates) {
                if (cursor.exhausted()) {
                    break;
                }
                final DnsRetryBudget.Attempt attempt = cursor.reserve(candidate.timeout());
                cursor = attempt.budget();
                markProbe(candidate, attempt.timeout());
                futures.add(completion.submit(task(candidate, request, attempt.timeout())));
            }
            if (futures.isEmpty()) {
                return new RaceGroup(RaceResult.failure(null), cursor);
            }
            RaceResult failure = RaceResult.failure(null);
            for (int remaining = 0; remaining < futures.size(); remaining++) {
                final RaceResult result = take(completion);
                if (result.success()) {
                    cancel(futures);
                    return new RaceGroup(result, cursor);
                }
                failure = failure.merge(result);
            }
            return new RaceGroup(failure, cursor);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Creates one callable candidate race task.
     *
     * @param candidate candidate name server
     * @param request   DNS request wire bytes
     * @param timeout   effective request timeout
     * @return callable race task
     */
    private static Callable<RaceResult> task(
            final DnsUpstream candidate,
            final byte[] request,
            final Duration timeout) {
        return () -> {
            final long started = System.nanoTime();
            try {
                final byte[] response = new DnsForwarder(List.of(candidate))
                        .forward(request, DnsRetryBudget.singleAttempt(timeout));
                validateResponse(response);
                markSuccess(candidate, System.nanoTime() - started);
                return RaceResult.success(response);
            } catch (final RuntimeException e) {
                markFailure(candidate);
                return RaceResult.failure(e);
            }
        };
    }

    /**
     * Waits for the next completed race task.
     *
     * @param completion completion service
     * @return completed race result
     */
    private static RaceResult take(final ExecutorCompletionService<RaceResult> completion) {
        try {
            return completion.take().get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return RaceResult.failure(new SocketException("DNS name-server race was interrupted", e));
        } catch (final ExecutionException e) {
            return RaceResult.failure(new SocketException("DNS name-server race task failed", e));
        }
    }

    /**
     * Cancels unfinished race tasks after a legal response wins.
     *
     * @param futures submitted futures
     */
    private static void cancel(final List<Future<RaceResult>> futures) {
        for (final Future<RaceResult> future : futures) {
            future.cancel(true);
        }
    }

    /**
     * Validates that a candidate response is legal for racing.
     *
     * @param response DNS response wire bytes
     */
    private static void validateResponse(final byte[] response) {
        if (response == null || response.length == 0 || response.length > DnsCodec.MAX_MESSAGE_BYTES) {
            throw new SocketException("DNS name-server returned an invalid response length");
        }
        final DnsDecodedResponse decoded = DnsCodec.decodeResponse(response);
        if (decoded.responseCode() == DnsResponseCode.SERVFAIL) {
            throw new SocketException("DNS name-server returned SERVFAIL");
        }
    }

    /**
     * Builds a SERVFAIL response for an exhausted race.
     *
     * @param query decoded DNS query
     * @return SERVFAIL response wire bytes
     */
    private static byte[] servfail(final DnsQuery query) {
        return DnsCodec.encodeResponse(DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false));
    }

    /**
     * Returns healthy candidates ordered by observed RTT.
     *
     * @param candidates source candidates
     * @return healthy candidates
     */
    private static List<DnsUpstream> healthyCandidates(final List<DnsUpstream> candidates) {
        final long now = System.nanoTime();
        final ArrayList<DnsUpstream> healthy = new ArrayList<>();
        for (final DnsUpstream candidate : candidates) {
            if (healthy(candidate, now)) {
                healthy.add(candidate);
            }
        }
        healthy.sort(Comparator.comparingLong(DnsNameServerRacer::rttNanos));
        return List.copyOf(healthy);
    }

    /**
     * Returns failed candidates ordered by their configured order.
     *
     * @param candidates source candidates
     * @return failed candidates
     */
    private static List<DnsUpstream> failedCandidates(final List<DnsUpstream> candidates) {
        final long now = System.nanoTime();
        final ArrayList<DnsUpstream> failed = new ArrayList<>();
        for (final DnsUpstream candidate : candidates) {
            if (!healthy(candidate, now)) {
                failed.add(candidate);
            }
        }
        return List.copyOf(failed);
    }

    /**
     * Returns whether a candidate is in the healthy racing group.
     *
     * @param candidate candidate name server
     * @param now       current monotonic time
     * @return true when the candidate is healthy
     */
    private static boolean healthy(final DnsUpstream candidate, final long now) {
        final HealthState state = HEALTH.get(healthKey(candidate));
        return state == null || state.retryAfterNanos <= now;
    }

    /**
     * Marks a candidate as healthy after a successful legal response.
     *
     * @param candidate candidate name server
     * @param rttNanos  observed round-trip time
     */
    private static void markSuccess(final DnsUpstream candidate, final long rttNanos) {
        HEALTH.compute(healthKey(candidate), (key, state) -> new HealthState(0L, 0, ewmaRttNanos(state, rttNanos)));
    }

    /**
     * Marks a candidate as already probed for the current timeout window.
     *
     * @param candidate candidate name server
     * @param timeout   probe timeout
     */
    private static void markProbe(final DnsUpstream candidate, final Duration timeout) {
        HEALTH.compute(
                healthKey(candidate),
                (key, state) -> new HealthState(System.nanoTime() + durationToNanos(timeout),
                        state == null ? 0 : state.consecutiveFailures,
                        state == null ? Long.MAX_VALUE : state.rttNanos));
    }

    /**
     * Marks a candidate as failed after an exception or SERVFAIL response.
     *
     * @param candidate candidate name server
     */
    private static void markFailure(final DnsUpstream candidate) {
        HEALTH.compute(
                healthKey(candidate),
                (key, state) -> new HealthState(System.nanoTime() + FAILED_RETRY_DELAY.toNanos(),
                        state == null ? 1 : state.consecutiveFailures + 1,
                        state == null ? Long.MAX_VALUE : state.rttNanos));
    }

    /**
     * Returns the observed EWMA round-trip time for a candidate.
     *
     * @param candidate candidate name server
     * @return EWMA round-trip time in nanoseconds
     */
    private static long rttNanos(final DnsUpstream candidate) {
        final HealthState state = HEALTH.get(healthKey(candidate));
        return state == null ? Long.MAX_VALUE : state.rttNanos;
    }

    /**
     * Updates a fixed-point RTT EWMA.
     *
     * @param state    previous health state, or {@code null}
     * @param rttNanos latest RTT sample
     * @return updated RTT EWMA
     */
    private static long ewmaRttNanos(final HealthState state, final long rttNanos) {
        final long safeRtt = Math.max(0L, rttNanos);
        if (state == null || state.rttNanos == Long.MAX_VALUE) {
            return safeRtt;
        }
        return ((state.rttNanos * RTT_EWMA_OLD_WEIGHT) + safeRtt) / RTT_EWMA_DIVISOR;
    }

    /**
     * Converts a timeout to nanoseconds with saturation.
     *
     * @param timeout timeout duration
     * @return timeout nanoseconds
     */
    private static long durationToNanos(final Duration timeout) {
        try {
            return timeout.toNanos();
        } catch (final ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Builds a stable health key for one candidate.
     *
     * @param candidate candidate name server
     * @return health key
     */
    private static String healthKey(final DnsUpstream candidate) {
        return candidate.healthKey();
    }

    /**
     * Creates a daemon thread factory for one race.
     *
     * @return thread factory
     */
    private static ThreadFactory threadFactory() {
        final AtomicInteger cursor = new AtomicInteger();
        return task -> {
            final Thread thread = new Thread(task, "fabric-dns-racer-" + cursor.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Immutable public health snapshot for one name server.
     *
     * @author Kimi Liu
     */
    public static final class DnsNameServerHealth {

        /**
         * Whether the name server is currently in the healthy racing group.
         */
        private final boolean healthy;

        /**
         * Consecutive failed racing attempts.
         */
        private final int consecutiveFailures;

        /**
         * EWMA round-trip time in nanoseconds.
         */
        private final long rttNanos;

        /**
         * Creates a health snapshot.
         *
         * @param healthy             whether the name server is currently healthy
         * @param consecutiveFailures consecutive failed racing attempts
         * @param rttNanos            EWMA round-trip time in nanoseconds
         */
        private DnsNameServerHealth(final boolean healthy, final int consecutiveFailures, final long rttNanos) {
            this.healthy = healthy;
            this.consecutiveFailures = consecutiveFailures;
            this.rttNanos = rttNanos;
        }

        /**
         * Returns whether the name server is currently healthy.
         *
         * @return true when the name server is healthy
         */
        public boolean healthy() {
            return healthy;
        }

        /**
         * Returns the consecutive failure count.
         *
         * @return consecutive failures
         */
        public int consecutiveFailures() {
            return consecutiveFailures;
        }

        /**
         * Returns EWMA round-trip time in nanoseconds.
         *
         * @return RTT EWMA
         */
        public long rttNanos() {
            return rttNanos;
        }

    }

    /**
     * Mutable internal health state stored in the shared map.
     *
     * @author Kimi Liu
     */
    private static final class HealthState {

        /**
         * Monotonic time after which the candidate can re-enter healthy racing.
         */
        private final long retryAfterNanos;

        /**
         * Consecutive failed racing attempts.
         */
        private final int consecutiveFailures;

        /**
         * EWMA round-trip time in nanoseconds.
         */
        private final long rttNanos;

        /**
         * Creates an internal health state.
         *
         * @param retryAfterNanos     monotonic retry time
         * @param consecutiveFailures consecutive failed racing attempts
         * @param rttNanos            EWMA round-trip time in nanoseconds
         */
        private HealthState(final long retryAfterNanos, final int consecutiveFailures, final long rttNanos) {
            this.retryAfterNanos = retryAfterNanos;
            this.consecutiveFailures = consecutiveFailures;
            this.rttNanos = rttNanos;
        }

    }

    /**
     * Immutable race result exposed to recursive callers that need the remaining retry budget.
     *
     * @author Kimi Liu
     */
    public static final class DnsNameServerRace {

        /**
         * Response wire bytes.
         */
        private final byte[] response;

        /**
         * Remaining retry budget.
         */
        private final DnsRetryBudget budget;

        /**
         * Creates an exposed race result.
         *
         * @param response response wire bytes
         * @param budget   remaining retry budget
         */
        private DnsNameServerRace(final byte[] response, final DnsRetryBudget budget) {
            if (response == null || response.length == 0 || response.length > DnsCodec.MAX_MESSAGE_BYTES) {
                throw new ValidateException("DNS name-server race response length is invalid");
            }
            if (budget == null) {
                throw new ValidateException("DNS name-server race budget must not be null");
            }
            this.response = response.clone();
            this.budget = budget;
        }

        /**
         * Returns a defensive copy of response wire bytes.
         *
         * @return response wire bytes
         */
        public byte[] response() {
            return response.clone();
        }

        /**
         * Returns remaining retry budget.
         *
         * @return remaining budget
         */
        public DnsRetryBudget budget() {
            return budget;
        }

    }

    /**
     * Immutable internal group result.
     *
     * @author Kimi Liu
     */
    private static final class RaceGroup {

        /**
         * Internal race result.
         */
        private final RaceResult result;

        /**
         * Remaining budget after group submissions.
         */
        private final DnsRetryBudget budget;

        /**
         * Creates an internal group result.
         *
         * @param result internal race result
         * @param budget remaining retry budget
         */
        private RaceGroup(final RaceResult result, final DnsRetryBudget budget) {
            if (result == null) {
                throw new ValidateException("DNS name-server race group result must not be null");
            }
            if (budget == null) {
                throw new ValidateException("DNS name-server race group budget must not be null");
            }
            this.result = result;
            this.budget = budget;
        }

        /**
         * Returns the internal race result.
         *
         * @return race result
         */
        private RaceResult result() {
            return result;
        }

        /**
         * Returns remaining retry budget.
         *
         * @return remaining budget
         */
        private DnsRetryBudget budget() {
            return budget;
        }

    }

    /**
     * Immutable result from one race task or group.
     *
     * @author Kimi Liu
     */
    private static final class RaceResult {

        /**
         * Winning response bytes, or {@code null} on failure.
         */
        private final byte[] response;

        /**
         * Failure cause, or {@code null} on success.
         */
        private final RuntimeException failure;

        /**
         * Creates a race result.
         *
         * @param response winning response bytes
         * @param failure  failure cause
         */
        private RaceResult(final byte[] response, final RuntimeException failure) {
            this.response = response == null ? null : response.clone();
            this.failure = failure;
        }

        /**
         * Creates a successful race result.
         *
         * @param response response bytes
         * @return successful result
         */
        private static RaceResult success(final byte[] response) {
            return new RaceResult(response, null);
        }

        /**
         * Creates a failed race result.
         *
         * @param failure failure cause
         * @return failed result
         */
        private static RaceResult failure(final RuntimeException failure) {
            return new RaceResult(null, failure);
        }

        /**
         * Returns whether this result succeeded.
         *
         * @return true when response bytes are present
         */
        private boolean success() {
            return response != null;
        }

        /**
         * Returns a defensive copy of response bytes.
         *
         * @return response bytes
         */
        private byte[] response() {
            return response.clone();
        }

        /**
         * Merges another failed result into this failed result.
         *
         * @param other other result
         * @return merged failed result
         */
        private RaceResult merge(final RaceResult other) {
            if (failure == null) {
                return other;
            }
            if (other.failure != null) {
                failure.addSuppressed(other.failure);
            }
            return this;
        }

    }

}
