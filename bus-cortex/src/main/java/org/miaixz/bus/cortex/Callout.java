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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.http.Httpv;
import org.miaixz.bus.http.plugin.httpv.CoverResult;

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
    private static final ConcurrentHashMap<Long, Httpv> CLIENTS = new ConcurrentHashMap<>();

    /**
     * Creates a new Callout utility holder.
     */
    private Callout() {

    }

    /**
     * Executes one synchronous GET request.
     *
     * @param url       target URL
     * @param timeoutMs timeout in milliseconds
     * @return normalized response snapshot
     */
    public static Response get(String url, long timeoutMs) {
        return adapt(client(timeoutMs).sync(url).nothrow().get());
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
        return adapt(
                client(timeoutMs).sync(url).nothrow().bodyType("json").setBodyPara(body == null ? "" : body).post());
    }

    /**
     * Cancels outstanding calls and clears all shared HTTP clients.
     */
    public static void shutdown() {
        CLIENTS.values().forEach(Callout::cancelQuietly);
        CLIENTS.clear();
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
    private static Httpv client(long timeoutMs) {
        long normalizedTimeout = Math.max(1L, timeoutMs);
        return CLIENTS.computeIfAbsent(normalizedTimeout, Callout::newHttpv);
    }

    /**
     * Builds one HTTP client configured for the supplied timeout.
     *
     * @param timeoutMs normalized timeout in milliseconds
     * @return configured HTTP client
     */
    private static Httpv newHttpv(long timeoutMs) {
        return Httpv.builder().config(
                builder -> builder.callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                        .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS).readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                        .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS))
                .preprocTimeoutTimes(1).build();
    }

    /**
     * Cancels one HTTP client without making shutdown fragile.
     *
     * @param client HTTP client to cancel
     */
    private static void cancelQuietly(Httpv client) {
        try {
            client.cancelAll();
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * Converts one raw {@link CoverResult} into the small immutable response snapshot used by callers.
     *
     * @param result raw HTTP execution result
     * @return normalized response snapshot
     */
    private static Response adapt(CoverResult result) {
        try {
            if (result == null) {
                return new Response(0, null, "unknown", false);
            }
            if (result.getState() != CoverResult.State.RESPONSED) {
                return new Response(0, null, failureMessage(result), result.getState() == CoverResult.State.TIMEOUT);
            }
            String body = result.getBody() == null ? null : result.getBody().toString();
            return new Response(result.getStatus(), body, null, false);
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }

    /**
     * Extracts a stable failure message from one failed HTTP execution result.
     *
     * @param result failed execution result
     * @return best-effort failure message
     */
    private static String failureMessage(CoverResult result) {
        if (result == null) {
            return "unknown";
        }
        if (result.getError() != null && result.getError().getMessage() != null
                && !result.getError().getMessage().isBlank()) {
            return result.getError().getMessage();
        }
        return result.getState() == null ? "unknown" : result.getState().name();
    }

    /**
     * Small immutable HTTP response snapshot used by runtime callers.
     *
     * @param statusCode   HTTP status code, or {@code 0} when no response was received
     * @param body         response body snapshot
     * @param errorMessage transport or timeout failure message
     * @param timeout      whether the request timed out before receiving a response
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
