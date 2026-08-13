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
package org.miaixz.bus.auth;

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Closed result algebra shared by authentication providers, protocol handlers, and runtime operations.
 *
 * @param <T> successful value type
 * @author Kimi Liu
 */
public sealed interface Outcome<T> permits Outcome.Success, Outcome.Rejected, Outcome.Failed {

    /**
     * Creates the successful result of an operation without a value.
     *
     * @return successful void outcome
     */
    static Outcome<Void> completed() {
        return new Success<>(null);
    }

    /**
     * Stable failure classifications shared by every protocol mapper.
     *
     * @author Kimi Liu
     */
    enum Kind {
        /**
         * Input or structural validation failure.
         */
        VALIDATION,
        /**
         * Credential or proof authentication failure.
         */
        AUTHENTICATION,
        /**
         * Authenticated caller lacks authorization.
         */
        AUTHORIZATION,
        /**
         * Operation conflicts with current state.
         */
        CONFLICT,
        /**
         * Policy or remote service rate limit was exceeded.
         */
        RATE_LIMIT,
        /**
         * Remote authentication dependency failed.
         */
        REMOTE,
        /**
         * Required authentication configuration is invalid or absent.
         */
        CONFIGURATION,
        /**
         * Unclassified internal implementation failure.
         */
        INTERNAL
    }

    /**
     * Immutable safe failure description.
     *
     * @param kind      failure classification
     * @param error     copied Bus error entry
     * @param retryable whether a later attempt may succeed
     * @author Kimi Liu
     */
    record Failure(Kind kind, Errors error, boolean retryable) {

        /**
         * Validates the classification and copies the public error entry.
         *
         * @throws ValidateException if required failure metadata is missing
         */
        public Failure {
            kind = Assert.notNull(kind, () -> new ValidateException("Failure kind must not be null"));
            final Errors current = Assert.notNull(error, () -> new ValidateException("Failure error must not be null"));
            error = new Errors.Entry(required(current.getKey(), "Failure error key"),
                    required(current.getValue(), "Failure error value"));
        }

        /**
         * Validates required safe error text.
         *
         * @param value error text
         * @param label field label
         * @return trimmed safe text
         * @throws ValidateException if the value is null or blank
         */
        private static String required(final String value, final String label) {
            return StringKit.trim(Assert.notBlank(value, () -> new ValidateException(label + " must not be blank")));
        }

    }

    /**
     * Successful operation result.
     *
     * @param value successful value, including null for completed void operations
     * @param <T>   successful value type
     * @author Kimi Liu
     */
    record Success<T>(T value) implements Outcome<T> {
    }

    /**
     * Rejected operation result without a system cause.
     *
     * @param failure safe failure description
     * @param <T>     expected success value type
     * @author Kimi Liu
     */
    record Rejected<T>(Failure failure) implements Outcome<T> {

        /**
         * Validates the required failure description.
         *
         * @throws ValidateException if {@code failure} is null
         */
        public Rejected {
            failure = Assert.notNull(failure, () -> new ValidateException("Rejected failure must not be null"));
        }

    }

    /**
     * Failed operation result retaining its non-serialized cause.
     *
     * @param failure safe failure description
     * @param cause   non-serialized internal cause
     * @param <T>     expected success value type
     * @author Kimi Liu
     */
    record Failed<T>(Failure failure, Throwable cause) implements Outcome<T> {

        /**
         * Validates required failure details.
         *
         * @throws ValidateException if {@code failure} or {@code cause} is null
         */
        public Failed {
            failure = Assert.notNull(failure, () -> new ValidateException("Failed failure must not be null"));
            cause = Assert.notNull(cause, () -> new ValidateException("Failed cause must not be null"));
        }

        /**
         * Returns a representation that excludes the internal cause.
         *
         * @return redacted failure representation
         */
        @Override
        public String toString() {
            return "Failed[REDACTED]";
        }

    }

}
