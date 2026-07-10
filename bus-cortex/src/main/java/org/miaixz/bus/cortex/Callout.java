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
package org.miaixz.bus.cortex;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.codec.DataCodec;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.logger.Logger;

/**
 * Shared static HTTP entry points for the small set of synchronous cortex runtime calls.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Callout {

    /**
     * Reusable HTTP clients keyed by normalized timeout.
     */
    private static final ConcurrentHashMap<Long, Client> CLIENTS = new ConcurrentHashMap<>();

    /**
     * Strict UTF-8 text decoder used through the current fabric response decode API.
     */
    private static final DataCodec<String> TEXT_CODEC = new DataCodec<>() {

        @Override
        public Payload encode(String value) {
            return Payload.of(value == null ? "" : value, StandardCharsets.UTF_8);
        }

        @Override
        public String decode(Payload payload) {
            return decodeText(payload);
        }

        @Override
        public MediaType media() {
            return MediaType.TEXT_PLAIN_TYPE.withCharset(StandardCharsets.UTF_8);
        }

    };

    /**
     * Creates a new Callout utility holder.
     */
    private Callout() {
        // No initialization required.
    }

    /**
     * Executes one synchronous GET request.
     *
     * @param url       target URL
     * @param timeoutMs timeout in milliseconds
     * @return normalized response snapshot
     */
    public static Response get(String url, long timeoutMs) {
        return adapt(client(timeoutMs).get(url));
    }

    /**
     * Executes one synchronous JSON POST request.
     *
     * @param url       target URL
     * @param body      request body
     * @param timeoutMs timeout in milliseconds
     * @return normalized response snapshot
     */
    public static Response postJson(String url, String body, long timeoutMs) {
        return adapt(client(timeoutMs).postJson(url, body));
    }

    /**
     * Cancels outstanding calls and clears all shared HTTP clients.
     */
    public static void shutdown() {
        Logger.info(true, "Cortex", "Callout shutdown requested: clientCount={}", CLIENTS.size());
        CLIENTS.values().forEach(Client::closeQuietly);
        CLIENTS.clear();
        Logger.info(false, "Cortex", "Callout shutdown completed: clientCount={}", CLIENTS.size());
    }

    /**
     * Returns a diagnostics snapshot for the shared HTTP client pool.
     *
     * @return callout diagnostics snapshot
     */
    public static DiagnosticsSnapshot diagnostics() {
        List<Long> timeoutBuckets = new ArrayList<>(CLIENTS.keySet());
        Collections.sort(timeoutBuckets);
        DiagnosticsSnapshot snapshot = new DiagnosticsSnapshot();
        snapshot.setComponent("callout");
        snapshot.setStatus("running");
        snapshot.setMetrics(Map.of("clientCount", CLIENTS.size(), "timeoutBuckets", List.copyOf(timeoutBuckets)));
        snapshot.setUpdatedAt(System.currentTimeMillis());
        return snapshot;
    }

    /**
     * Returns a shared HTTP client for the normalized timeout bucket.
     *
     * @param timeoutMs requested timeout in milliseconds
     * @return reusable HTTP client
     */
    private static Client client(long timeoutMs) {
        long normalizedTimeout = Math.max(1L, timeoutMs);
        return CLIENTS.computeIfAbsent(normalizedTimeout, Client::new);
    }

    /**
     * Converts one local HTTP result into the small immutable response snapshot used by callers.
     *
     * @param result raw HTTP execution result
     * @return normalized response snapshot
     */
    private static Response adapt(Result result) {
        if (result == null) {
            return new Response(0, null, "unknown", false);
        }
        if (result.state() != ResultState.RESPONDED) {
            return new Response(result.status(), null, failureMessage(result), result.state() == ResultState.TIMEOUT);
        }
        return new Response(result.status(), result.body(), null, false);
    }

    /**
     * Extracts a stable failure message from one failed HTTP execution result.
     *
     * @param result failed execution result
     * @return best-effort failure message
     */
    private static String failureMessage(Result result) {
        if (result == null) {
            return "unknown";
        }
        if (result.error() != null && result.error().getMessage() != null && !result.error().getMessage().isBlank()) {
            return result.error().getMessage();
        }
        return result.state() == null ? "unknown" : result.state().name();
    }

    /**
     * Decodes payload bytes strictly as UTF-8.
     *
     * @param payload response payload
     * @return decoded text
     */
    private static String decodeText(Payload payload) {
        byte[] bytes = payload == null ? new byte[0] : payload.bytes();
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new ConvertException("Unable to decode HTTP response as UTF-8", e);
        }
    }

    /**
     * Returns whether a failure came from a timeout path.
     *
     * @param error failure candidate
     * @return true when timeout-related
     */
    private static boolean isTimeout(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof TimeoutException || current instanceof java.util.concurrent.TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Shared current-fabric HTTP execution context for one timeout bucket.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class Client {

        /**
         * Normalized timeout in milliseconds.
         */
        private final long timeoutMs;

        /**
         * Shared current fabric context.
         */
        private final Context context;

        /**
         * Active calls owned by this client bucket.
         */
        private final Set<Call<HttpResponse>> calls;

        /**
         * Creates one timeout-scoped client bucket.
         *
         * @param timeoutMs normalized timeout in milliseconds
         */
        private Client(long timeoutMs) {
            Logger.debug(true, "Cortex", "Callout HTTP client creation started: timeoutMs={}", timeoutMs);
            this.timeoutMs = timeoutMs;
            Duration timeout = Duration.ofMillis(timeoutMs);
            this.context = Context.create().withOptions(Options.of("timeout", Timeout.of(timeout)));
            this.calls = ConcurrentHashMap.newKeySet();
            Logger.debug(false, "Cortex", "Callout HTTP client created: timeoutMs={}", timeoutMs);
        }

        /**
         * Executes one GET request.
         *
         * @param url target URL
         * @return local result snapshot
         */
        private Result get(String url) {
            return execute(() -> Fabric.http(context).timeout(Duration.ofMillis(timeoutMs)).get(url).build().call());
        }

        /**
         * Executes one JSON POST request.
         *
         * @param url  target URL
         * @param body request body
         * @return local result snapshot
         */
        private Result postJson(String url, String body) {
            return execute(
                    () -> Fabric.http(context).timeout(Duration.ofMillis(timeoutMs)).post(url)
                            .json(body == null ? "" : body).build().call());
        }

        /**
         * Executes one request and normalizes current fabric failures into local result states.
         *
         * @param supplier call supplier
         * @return local result snapshot
         */
        private Result execute(Supplier<Call<HttpResponse>> supplier) {
            Call<HttpResponse> call = null;
            try {
                call = supplier.get();
                calls.add(call);
                HttpResponse response = call.execute();
                int status = response.code();
                try {
                    return new Result(ResultState.RESPONDED, status, response.decode(TEXT_CODEC, String.class), null);
                } catch (RuntimeException e) {
                    return new Result(ResultState.FAILED, status, null, e);
                }
            } catch (RuntimeException e) {
                return new Result(isTimeout(e) ? ResultState.TIMEOUT : ResultState.FAILED, 0, null, e);
            } finally {
                if (call != null) {
                    calls.remove(call);
                }
            }
        }

        /**
         * Cancels active calls and closes current fabric resources without making shutdown fragile.
         */
        private void closeQuietly() {
            RuntimeException failure = null;
            for (Call<HttpResponse> call : calls) {
                try {
                    call.cancel();
                } catch (RuntimeException e) {
                    failure = e;
                }
            }
            calls.clear();
            try {
                context.reactor().close();
            } catch (RuntimeException e) {
                failure = e;
            }
            if (failure != null) {
                Logger.warn(
                        false,
                        "Cortex",
                        failure,
                        "Callout HTTP client close failed: exception={}",
                        failure.getClass().getSimpleName());
            }
        }

    }

    /**
     * Local replacement for the previous HTTP result state categories used by Callout.
     */
    private enum ResultState {

        /**
         * A response was received and decoded.
         */
        RESPONDED,

        /**
         * The call timed out before producing a usable response.
         */
        TIMEOUT,

        /**
         * Transport, cancellation, protocol, or decode failure.
         */
        FAILED

    }

    /**
     * Cortex-local HTTP execution result.
     *
     * @param state  execution state
     * @param status response status, or 0 when no status was available
     * @param body   decoded response body
     * @param error  failure cause
     */
    private record Result(ResultState state, int status, String body, Throwable error) {

    }

    /**
     * Small immutable HTTP response snapshot used by runtime callers.
     *
     * @param statusCode   HTTP status code, or {@code 0} when no response was received
     * @param body         response body snapshot
     * @param errorMessage transport or timeout failure message
     * @param timeout      whether the request timed out before receiving a response
     * @author Kimi Liu
     * @since Java 21+
     */
    public record Response(int statusCode, String body, String errorMessage, boolean timeout) {

        /**
         * Returns whether the response status code indicates success.
         *
         * @return {@code true} when the response is in the 2xx range
         */
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }

    }

}
