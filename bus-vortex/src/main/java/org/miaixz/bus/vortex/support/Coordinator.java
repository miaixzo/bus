/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.support;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Executor;

import reactor.core.publisher.Mono;

/**
 * Coordinator base class for all Vortex service executors.
 * <p>
 * This class coordinates common functionality shared across different service types such as REST, WebSocket, gRPC, MQ,
 * and MCP. It implements the {@link Executor} interface and provides default implementations for all methods,
 * encapsulating:
 * <ul>
 * <li>Common URL building utilities for constructing service endpoints</li>
 * <li>GraalVM Native Image compatibility fixes for JSON encoding</li>
 * <li>Shared logging patterns and utilities</li>
 * <li>Resource management lifecycle hooks</li>
 * </ul>
 * <p>
 * As a coordinator, this class centralizes cross-cutting concerns and provides a unified framework for all service
 * executors. Services extending this class inherit these coordinated utilities while maintaining their specific
 * execution logic. This abstract class follows the Template Method pattern, allowing subclasses to override specific
 * behaviors while inheriting the common execution framework.
 * <p>
 * Generic type parameters are passed through from the {@link Executor} interface:
 * <ul>
 * <li>{@code I} - The input type expected by implementations extending this coordinator</li>
 * <li>{@code O} - The output type produced by implementations extending this coordinator</li>
 * </ul>
 *
 * @param <I> The input type expected by this coordinator
 * @param <O> The output type produced by this coordinator
 * @author Kimi Liu
 * @since Java 17+
 * @see Executor
 * @see org.miaixz.bus.vortex.support.rest.RestExecutor
 * @see org.miaixz.bus.vortex.support.ws.WsExecutor
 * @see org.miaixz.bus.vortex.support.grpc.GrpcExecutor
 * @see org.miaixz.bus.vortex.support.mq.MqExecutor
 * @see org.miaixz.bus.vortex.support.mcp.McpExecutor
 */
public abstract class Coordinator<I, O> implements Executor<I, O> {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation builds a URL string from the assets configuration. Subclasses that need different build
     * behavior should override this method.
     */
    @Override
    public Mono<String> build(Context context) {
        return Mono.fromCallable(() -> {
            Assets assets = context.getAssets();
            StringBuilder baseUrlBuilder = new StringBuilder(assets.getHost());
            if (assets.getPort() != null && assets.getPort() > 0) {
                baseUrlBuilder.append(Symbol.COLON).append(assets.getPort());
            }
            if (assets.getPath() != null && !assets.getPath().isEmpty()) {
                if (!assets.getPath().startsWith(Symbol.SLASH)) {
                    baseUrlBuilder.append(Symbol.SLASH);
                }
                baseUrlBuilder.append(assets.getPath());
            }
            return baseUrlBuilder.toString();
        });
    }

    protected String fixJsonEncoding(String json) {
        return json;
    }

    protected String fixFastJsonEncoding(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        // Only apply GraalVM-specific fixes if running in Native Image mode
        if (!Keys.IS_GRAALVM_NATIVE) {
            // Running in normal JVM mode, no fix needed
            return json;
        }

        // DEBUG: Log the original JSON before any processing
        Logger.debug(
                true,
                getClass().getSimpleName(),
                "GraalVM Native Image detected - Fixing JSON encoding (first 500 chars): {}",
                json.length() > 500 ? json.substring(0, 500) + "..." : json);

        // Step 1: Remove NULL bytes
        StringBuilder result = new StringBuilder(json.length());
        int nullCount = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c != '\u0000') {
                result.append(c);
            } else {
                nullCount++;
            }
        }

        if (nullCount > 0) {
            Logger.info(
                    true,
                    getClass().getSimpleName(),
                    "GraalVM JSON fix - Removed {} NULL bytes: {} -> {}",
                    nullCount,
                    json.length(),
                    result.length());
        }

        json = result.toString();

        // Step 2: Fix invalid escape sequences and escape literal newlines in JSON string values
        // NOTE: This should only be needed for GraalVM Native Image
        // IMPORTANT: Fix FastJSON's failure to escape control characters in JSON
        result = new StringBuilder(json.length() * 2);
        boolean inString = false;
        int escapeFixCount = 0;
        int controlCharFixCount = 0;
        int i = 0;

        while (i < json.length()) {
            char c = json.charAt(i);

            if (c == '"') {
                // Check if this quote is escaped by counting preceding backslashes
                boolean isEscaped = false;
                for (int j = i - 1; j >= 0 && json.charAt(j) == '\\'; j--) {
                    isEscaped = !isEscaped;
                }
                if (!isEscaped) {
                    inString = !inString;
                }
                result.append(c);
                i++;
            } else if (c == '\\' && inString) {
                // We're inside a JSON string and found a backslash
                if (i + 1 < json.length()) {
                    char nextChar = json.charAt(i + 1);
                    // Valid JSON escape sequences: ", \, /, b, f, n, r, t, u
                    boolean isValidEscape = nextChar == '"' || nextChar == '\\' || nextChar == '/' || nextChar == 'b'
                            || nextChar == 'f' || nextChar == 'n' || nextChar == 'r' || nextChar == 't'
                            || nextChar == 'u';

                    if (isValidEscape) {
                        // Already valid escape sequence, keep both backslash and next char
                        result.append(c);
                        result.append(nextChar);
                        i += 2;
                    } else {
                        // Invalid escape like \部 or \8
                        // The backslash is NOT supposed to be there, so we need to either:
                        // 1. Remove it entirely (keep only the character after it)
                        // 2. Escape it as \\ to make it a literal backslash in JSON
                        // Since FastJSON in GraalVM incorrectly adds backslashes, we remove them
                        result.append(nextChar);
                        escapeFixCount++;
                        i += 2; // Skip both the backslash and the next character

                        if (escapeFixCount <= 5) {
                            Logger.info(
                                    true,
                                    getClass().getSimpleName(),
                                    "Removed invalid backslash before '{}' (char code: {}) - kept character",
                                    nextChar,
                                    (int) nextChar);
                        }
                    }
                } else {
                    // Backslash at end of string without valid escape char, remove it
                    escapeFixCount++;
                    i++;
                }
            } else if (inString && (c == '\n' || c == '\r' || c == '\t')) {
                // **CRITICAL FIX**: Escape literal control characters inside JSON strings
                // FastJSON in GraalVM Native Image mode fails to escape these properly
                String escapeSeq;
                if (c == '\n') {
                    escapeSeq = "\\n";
                } else if (c == '\r') {
                    escapeSeq = "\\r";
                } else { // \t
                    escapeSeq = "\\t";
                }
                result.append(escapeSeq);
                controlCharFixCount++;
                i++;
            } else {
                // Keep all other characters as-is
                result.append(c);
                i++;
            }
        }

        if (escapeFixCount > 0) {
            Logger.info(
                    true,
                    getClass().getSimpleName(),
                    "GraalVM JSON fix - Processed {} invalid backslash sequences. Input length: {}, Output length: {}",
                    escapeFixCount,
                    json.length(),
                    result.length());
        }

        if (controlCharFixCount > 0) {
            Logger.info(
                    true,
                    getClass().getSimpleName(),
                    "GraalVM JSON fix - Escaped {} literal control characters (newlines/tabs) in JSON string values",
                    controlCharFixCount);
        }

        // DEBUG: Log the result
        Logger.debug(
                true,
                getClass().getSimpleName(),
                "GraalVM JSON fix - Output JSON (first 500 chars): {}",
                result.length() > 500 ? result.substring(0, 500) + "..." : result);

        return result.toString();
    }

}
