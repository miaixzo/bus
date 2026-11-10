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
package org.miaixz.bus.errors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Identifies a failure by its stable semantic name across components.
 *
 * <p>
 * Each dot-separated segment starts with a lowercase ASCII letter and contains lowercase ASCII letters, digits, and
 * single hyphens between groups. Components can namespace their own types, such as {@code cache.backend-timeout}.
 * Compare types by value and retain published identifiers as part of the caller's contract.
 * </p>
 *
 * @param id stable identifier in its canonical spelling
 * @author Kimi Liu
 */
public record ErrorType(String id) implements Serializable {

    /**
     * The caller has no visible matching route.
     */
    public static final ErrorType ROUTE_NOT_FOUND = new ErrorType("route-not-found");
    /**
     * The requested method is unavailable on a route visible to the caller.
     */
    public static final ErrorType METHOD_NOT_ALLOWED = new ErrorType("method-not-allowed");
    /**
     * The request media type or content encoding is unsupported by the receiving operation.
     */
    public static final ErrorType UNSUPPORTED_MEDIA_TYPE = new ErrorType("unsupported-media-type");
    /**
     * The inbound payload exceeds a configured size limit.
     */
    public static final ErrorType PAYLOAD_TOO_LARGE = new ErrorType("payload-too-large");
    /**
     * The request exceeds a caller or resource quota.
     */
    public static final ErrorType RATE_LIMITED = new ErrorType("rate-limited");
    /**
     * The gateway has insufficient capacity to admit or continue an operation.
     */
    public static final ErrorType GATEWAY_OVERLOADED = new ErrorType("gateway-overloaded");
    /**
     * A dependency needed to complete an operation is unavailable.
     */
    public static final ErrorType DEPENDENCY_UNAVAILABLE = new ErrorType("dependency-unavailable");
    /**
     * The requested representation cannot be produced.
     */
    public static final ErrorType NOT_ACCEPTABLE = new ErrorType("not-acceptable");
    /**
     * The caller's input failed validation.
     */
    public static final ErrorType INVALID_REQUEST = new ErrorType("invalid-request");
    /**
     * Authentication is missing or invalid.
     */
    public static final ErrorType UNAUTHENTICATED = new ErrorType("unauthenticated");
    /**
     * An authenticated caller lacks permission.
     */
    public static final ErrorType FORBIDDEN = new ErrorType("forbidden");
    /**
     * The operation or a required dependency exceeded a deadline.
     */
    public static final ErrorType TIMEOUT = new ErrorType("timeout");
    /**
     * The operation was cancelled or interrupted.
     */
    public static final ErrorType CANCELLED = new ErrorType("cancelled");
    /**
     * No registered classification applies.
     */
    public static final ErrorType UNKNOWN = new ErrorType("unknown");
    /**
     * Serialization version of the type identifier.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * An input or output operation failed.
     */
    public static final ErrorType IO_FAILURE = new ErrorType("io-failure");

    /**
     * The component could not interpret or validate data.
     */
    public static final ErrorType INVALID_DATA = new ErrorType("invalid-data");

    /**
     * The component could not load or apply its configuration.
     */
    public static final ErrorType CONFIGURATION_FAILURE = new ErrorType("configuration-failure");

    /**
     * A security provider, cryptographic operation or integrity check failed.
     */
    public static final ErrorType SECURITY_FAILURE = new ErrorType("security-failure");

    /**
     * A user-supplied callback failed.
     */
    public static final ErrorType CALLBACK_FAILURE = new ErrorType("callback-failure");

    /**
     * A component could not complete its operation.
     */
    public static final ErrorType EXECUTION_FAILURE = new ErrorType("execution-failure");

    /**
     * A side effect may have occurred and the outcome requires verification.
     */
    public static final ErrorType OUTCOME_UNKNOWN = new ErrorType("outcome-unknown");

    /**
     * Creates a type using its exact identifier.
     *
     * @param id canonical type identifier
     * @throws NullPointerException     if the identifier is {@code null}
     * @throws IllegalArgumentException if the identifier does not match the documented grammar
     */
    public ErrorType {
        Objects.requireNonNull(id, "id");
        if (!Syntax.IDENTIFIER.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid error type identifier");
        }
    }

    /**
     * Initializes the validator independently of the order of the public constants.
     *
     * @author Kimi Liu
     */
    private static final class Syntax {

        /**
         * Grammar for standard and component-qualified error identifiers.
         */
        private static final Pattern IDENTIFIER = Pattern
                .compile("[a-z][a-z0-9]*+(?:-[a-z0-9]++|\\.[a-z][a-z0-9]*+)*+");

    }

}
