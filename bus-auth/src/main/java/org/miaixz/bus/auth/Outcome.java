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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents the closed internal result of an asynchronous authentication operation.
 * <p>
 * {@link Rejected} denotes an expected refusal caused by invalid input, policy, credentials, or protocol state.
 * {@link Failed} denotes an operational failure such as unavailable dependencies or exhausted timeout. Normal
 * authentication failures are values rather than exceptional stage completions; cancellation and unrecoverable
 * programming errors may still complete a {@code CompletionStage} exceptionally.
 * </p>
 * <p>
 * Outcome is an internal orchestration value. Protocol response encoders accept their formal standard response or error
 * models and must never serialize an Outcome directly.
 * </p>
 *
 * @param <T> success value type
 * @author Kimi Liu
 */
public interface Outcome<T> {

    /**
     * Maps a successful value while preserving rejection and operational failure outcomes.
     *
     * @param mapper successful-value mapper
     * @param <R>    mapped success type
     * @return mapped outcome
     */
    default <R> Outcome<R> map(final Function<? super T, ? extends R> mapper) {
        Assert.notNull(mapper, "Outcome mapper must not be null");
        return switch (this) {
            case Succeeded<T> success -> Outcome.succeeded(mapper.apply(success.value()));
            case Rejected<T> rejected -> Outcome.rejected(rejected.failure());
            case Failed<T> failed -> Outcome.failed(failed.failure());
            default -> operationFailed("Unsupported outcome implementation");
        };
    }

    /**
     * Maps a successful value to another Outcome while preserving rejection and operational failure outcomes.
     *
     * @param mapper successful-value outcome mapper
     * @param <R>    mapped success type
     * @return mapped outcome
     */
    default <R> Outcome<R> flatMap(final Function<? super T, ? extends Outcome<R>> mapper) {
        Assert.notNull(mapper, "Outcome mapper must not be null");
        return switch (this) {
            case Succeeded<T> success -> Assert
                    .notNull(mapper.apply(success.value()), "Outcome mapper returned no outcome");
            case Rejected<T> rejected -> Outcome.rejected(rejected.failure());
            case Failed<T> failed -> Outcome.failed(failed.failure());
            default -> operationFailed("Unsupported outcome implementation");
        };
    }

    /**
     * Invokes one asynchronous Outcome operation and maps its successful value while closing ordinary invocation,
     * stage, null-value, and mapper failures into a failed Outcome.
     *
     * @param operation asynchronous Outcome supplier
     * @param mapper    successful-value mapper
     * @param <S>       source success type
     * @param <R>       mapped success type
     * @return asynchronous mapped outcome
     */
    static <S, R> CompletionStage<Outcome<R>> mapStage(
            final Supplier<CompletionStage<Outcome<S>>> operation,
            final Function<? super S, ? extends R> mapper) {
        Assert.notNull(operation, "Outcome operation supplier must not be null");
        Assert.notNull(mapper, "Outcome mapper must not be null");
        final CompletionStage<Outcome<S>> stage;
        try {
            stage = operation.get();
        } catch (RuntimeException ignored) {
            return CompletableFuture.completedFuture(operationFailed("Operation failed before returning a stage"));
        }
        if (stage == null) {
            return CompletableFuture.completedFuture(operationFailed("Operation returned no stage"));
        }
        return stage.handle((outcome, cause) -> {
            if (cause != null) {
                return operationFailed("Operation stage failed");
            }
            if (outcome == null) {
                return operationFailed("Operation returned no outcome");
            }
            try {
                return outcome.map(
                        value -> Assert.notNull(
                                mapper.apply(Assert.notNull(value, "Operation returned no value")),
                                "Outcome mapper returned no value"));
            } catch (RuntimeException ignored) {
                return operationFailed("Operation result could not be mapped");
            }
        });
    }

    /**
     * Creates a safe operational failure when outcome transformation itself fails.
     *
     * @param <R>         expected transformed value type
     * @param description safe transformation failure description
     * @return failed transformed outcome
     */
    private static <R> Outcome<R> operationFailed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a successful outcome.
     *
     * @param value success value; {@code null} is permitted for {@link Void} operations
     * @param <T>   success value type
     * @return successful internal outcome
     */
    static <T> Outcome<T> succeeded(final T value) {
        return new Succeeded<>(value);
    }

    /**
     * Creates an expected authentication or protocol rejection.
     *
     * @param failure safe structured rejection detail
     * @param <T>     success value type that would have been returned
     * @return rejected internal outcome
     * @throws IllegalArgumentException if the failure is {@code null}
     */
    static <T> Outcome<T> rejected(final Failure failure) {
        return new Rejected<>(failure);
    }

    /**
     * Creates an operational authentication failure.
     *
     * @param failure safe structured operational failure detail
     * @param <T>     success value type that would have been returned
     * @return failed internal outcome
     * @throws IllegalArgumentException if the failure is {@code null}
     */
    static <T> Outcome<T> failed(final Failure failure) {
        return new Failed<>(failure);
    }

    /**
     * Carries a successfully produced internal value.
     *
     * @param value success value; {@code null} is permitted for {@link Void} operations
     * @param <T>   success value type
     * @author Kimi Liu
     */
    record Succeeded<T>(T value) implements Outcome<T> {

    }

    /**
     * Carries an expected authentication, validation, policy, or protocol refusal.
     *
     * @param failure safe structured rejection detail
     * @param <T>     success value type that would have been returned
     * @author Kimi Liu
     */
    record Rejected<T>(Failure failure) implements Outcome<T> {

        /**
         * Creates a rejected outcome.
         *
         * @param failure safe structured rejection detail
         * @throws IllegalArgumentException if the failure is {@code null}
         */
        public Rejected {
            Assert.notNull(failure, "Rejected outcome failure must not be null");
        }

    }

    /**
     * Carries an operational failure that prevented the authentication operation from completing.
     *
     * @param failure safe structured operational failure detail
     * @param <T>     success value type that would have been returned
     * @author Kimi Liu
     */
    record Failed<T>(Failure failure) implements Outcome<T> {

        /**
         * Creates a failed outcome.
         *
         * @param failure safe structured operational failure detail
         * @throws IllegalArgumentException if the failure is {@code null}
         */
        public Failed {
            Assert.notNull(failure, "Failed outcome failure must not be null");
        }

    }

    /**
     * Carries a Bus error and safe structured detail without becoming an exception or defining another error code.
     *
     * @param error           existing Bus error definition
     * @param safeDescription non-sensitive description suitable for framework diagnostics
     * @param details         provider-neutral non-sensitive structured details
     * @author Kimi Liu
     */
    record Failure(Errors error, String safeDescription, JsonValue.ObjectValue details) {

        /**
         * Creates an immutable safe failure value.
         *
         * @param error           existing Bus error definition
         * @param safeDescription non-sensitive diagnostic description
         * @param details         non-sensitive structured details
         * @throws IllegalArgumentException if a component is {@code null} or the safe description is blank
         */
        public Failure {
            Assert.notNull(error, "Outcome failure error must not be null");
            Assert.notBlank(safeDescription, "Outcome failure description must not be blank");
            Assert.notNull(details, "Outcome failure details must not be null");
            details = new JsonValue.ObjectValue(details.values());
        }

    }

}
