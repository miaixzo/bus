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

import java.net.URI;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Options;

/**
 * Unified immutable authorization callback contract.
 *
 * <p>
 * Callback-capable protocols and vendors use {@link Inbound} for received browser or user-agent responses and
 * {@link Outbound} for redirects emitted to a relying party. Ordinary asynchronous completion continues to use
 * {@link java.util.concurrent.CompletionStage} and {@link Outcome}.
 * </p>
 *
 * @author Kimi Liu
 */
public sealed interface Callback permits Callback.Inbound, Callback.Outbound {

    /**
     * Starts an inbound callback builder.
     *
     * @param context non-null operation context
     * @return mutable builder owned by the caller
     * @throws ValidateException if {@code context} is null
     */
    static Inbound.Builder inbound(final Context context) {
        return new Inbound.Builder(context);
    }

    /**
     * Starts an outbound callback builder.
     *
     * @param context non-null operation context
     * @return mutable builder owned by the caller
     * @throws ValidateException if {@code context} is null
     */
    static Outbound.Builder outbound(final Context context) {
        return new Outbound.Builder(context);
    }

    /**
     * Validates an optional absolute reference URI without credentials or fragment data.
     *
     * @param value optional reference URI
     * @param name  field label used in validation errors
     * @return validated reference, or {@code null}
     * @throws ValidateException if the URI is relative or exposes user information or a fragment
     */
    private static URI reference(final URI value, final String name) {
        if (value == null) {
            return null;
        }
        if (!value.isAbsolute() || value.getUserInfo() != null || value.getFragment() != null) {
            throw new ValidateException(name + " must be absolute and contain no user information or fragment");
        }
        return value;
    }

    /**
     * Returns the immutable operation context associated with this callback.
     *
     * @return non-null operation context
     */
    Context context();

    /**
     * Returns the authorization interaction represented by this callback.
     *
     * @return non-null callback kind
     */
    Kind kind();

    /**
     * Returns the immutable and size-bounded callback parameters.
     *
     * @return non-null parameter multimap
     */
    Parameters parameters();

    /**
     * Returns protocol-specific typed options carried with this callback.
     *
     * @return non-null immutable options
     */
    Options options();

    /**
     * Returns the protocol problem when the remote party reported a failure.
     *
     * @return problem details, or {@code null} for a successful callback
     */
    Problem problem();

    /**
     * Reports whether this callback contains no protocol problem.
     *
     * @return {@code true} when {@link #problem()} is null
     */
    default boolean successful() {
        return problem() == null;
    }

    /**
     * Reads a callback parameter that must occur at most once.
     *
     * @param name non-blank parameter name
     * @return optional unique parameter value
     * @throws ValidateException if the name is invalid or the parameter occurs more than once
     */
    default Optional<String> value(final String name) {
        return parameters().single(name);
    }

    /**
     * Identifies the user-agent interaction represented by a callback.
     *
     * @author Kimi Liu
     */
    enum Kind {
        /**
         * Authorization response from an authorization server.
         */
        AUTHORIZATION,
        /**
         * Logout response or logout completion.
         */
        LOGOUT,
        /**
         * User consent response.
         */
        CONSENT,
        /**
         * Device authorization verification response.
         */
        DEVICE_VERIFICATION
    }

    /**
     * Defines how outbound callback parameters are transported.
     *
     * @author Kimi Liu
     */
    enum Mode {
        /**
         * Parameters are encoded in the URI query.
         */
        QUERY,
        /**
         * Parameters are encoded in the URI fragment.
         */
        FRAGMENT,
        /**
         * Parameters are submitted by an HTML form POST.
         */
        FORM_POST,
        /**
         * Parameters are represented by a direct JWT value.
         */
        JWT,
        /**
         * A JWT response is encoded in the URI query.
         */
        QUERY_JWT,
        /**
         * A JWT response is submitted by an HTML form POST.
         */
        FORM_POST_JWT,
        /**
         * The response is returned directly without a user-agent redirect.
         */
        DIRECT
    }

    /**
     * Immutable authorization callback received from a user agent.
     *
     * @param context    non-null operation context
     * @param kind       non-null interaction kind
     * @param endpoint   non-null Fabric address that received the callback
     * @param method     non-null HTTP method used by the callback
     * @param parameters non-null immutable callback parameters
     * @param options    non-null immutable typed options
     * @param problem    protocol problem, or {@code null} on success
     * @author Kimi Liu
     */
    record Inbound(Context context, Kind kind, Address endpoint, Http.Method method, Parameters parameters,
            Options options, Problem problem) implements Callback {

        /**
         * Validates and creates an inbound callback snapshot.
         *
         * @throws ValidateException if a required component is null
         */
        public Inbound {
            context = Assert.notNull(context, () -> new ValidateException("Callback context must not be null"));
            kind = Assert.notNull(kind, () -> new ValidateException("Callback kind must not be null"));
            endpoint = Assert.notNull(endpoint, () -> new ValidateException("Callback endpoint must not be null"));
            method = Assert.notNull(method, () -> new ValidateException("Callback method must not be null"));
            parameters = Assert
                    .notNull(parameters, () -> new ValidateException("Callback parameters must not be null"));
            options = Assert.notNull(options, () -> new ValidateException("Callback options must not be null"));
        }

        /**
         * Returns a copy containing one replacement parameter value.
         *
         * @param name  non-blank parameter name
         * @param value non-null parameter value
         * @return immutable callback copy
         * @throws ValidateException if the name, value, or resulting parameter count is invalid
         */
        public Inbound with(final String name, final String value) {
            return new Inbound(context, kind, endpoint, method, parameters.with(name, value), options, problem);
        }

        /**
         * Returns a copy containing one replacement typed option.
         *
         * @param key   non-null typed option key
         * @param value option value
         * @param <T>   option value type
         * @return immutable callback copy
         */
        public <T> Inbound with(final Options.Key<T> key, final T value) {
            return new Inbound(context, kind, endpoint, method, parameters, options.with(key, value), problem);
        }

        /**
         * Returns a redacted representation that never includes callback parameters.
         *
         * @return fixed redacted text
         */
        @Override
        public String toString() {
            return "Callback.Inbound[REDACTED]";
        }

        /**
         * Mutable builder for an immutable inbound callback.
         *
         * @author Kimi Liu
         */
        public static final class Builder {

            /**
             * Operation context shared by all snapshots built from this builder.
             */
            private final Context context;

            /**
             * Interaction kind, defaulting to authorization.
             */
            private Kind kind = Kind.AUTHORIZATION;

            /**
             * Fabric address that received the callback.
             */
            private Address endpoint;

            /**
             * HTTP method, defaulting to GET.
             */
            private Http.Method method = Http.Method.GET;

            /**
             * Immutable parameter snapshot accumulated by the builder.
             */
            private Parameters parameters = Parameters.empty();

            /**
             * Typed options, defaulting to empty.
             */
            private Options options = Options.empty();

            /**
             * Optional protocol problem.
             */
            private Problem problem;

            /**
             * Creates an inbound builder for one operation.
             *
             * @param context non-null operation context
             * @throws ValidateException if {@code context} is null
             */
            private Builder(final Context context) {
                this.context = Assert
                        .notNull(context, () -> new ValidateException("Callback context must not be null"));
            }

            /**
             * Sets the interaction kind.
             *
             * @param value callback kind
             * @return this builder
             */
            public Builder kind(final Kind value) {
                kind = value;
                return this;
            }

            /**
             * Sets the Fabric address that received the callback.
             *
             * @param value callback address
             * @return this builder
             */
            public Builder endpoint(final Address value) {
                endpoint = value;
                return this;
            }

            /**
             * Sets the HTTP method used by the callback.
             *
             * @param value HTTP method
             * @return this builder
             */
            public Builder method(final Http.Method value) {
                method = value;
                return this;
            }

            /**
             * Replaces all callback parameters with a defensive snapshot.
             *
             * @param value parameter multimap
             * @return this builder
             * @throws ValidateException if the map exceeds callback bounds
             */
            public Builder parameters(final Map<String, List<String>> value) {
                parameters = Parameters.from(value);
                return this;
            }

            /**
             * Adds or replaces one callback parameter.
             *
             * @param name  parameter name
             * @param value parameter value
             * @return this builder
             * @throws ValidateException if the parameter exceeds callback bounds
             */
            public Builder parameter(final String name, final String value) {
                parameters = parameters.with(name, value);
                return this;
            }

            /**
             * Replaces the typed option snapshot.
             *
             * @param value non-null options
             * @return this builder
             */
            public Builder options(final Options value) {
                options = value;
                return this;
            }

            /**
             * Sets the optional protocol problem.
             *
             * @param value problem, or {@code null} for success
             * @return this builder
             */
            public Builder problem(final Problem value) {
                problem = value;
                return this;
            }

            /**
             * Builds an immutable validated callback.
             *
             * @return inbound callback snapshot
             * @throws ValidateException if a required component is null
             */
            public Inbound build() {
                return new Inbound(context, kind, endpoint, method, parameters, options, problem);
            }
        }
    }

    /**
     * Authorization callback emitted to a relying party or user agent.
     *
     * @param context     non-null operation context
     * @param kind        non-null interaction kind
     * @param destination optional Fabric redirect address; null is permitted for direct responses
     * @param mode        non-null response transport mode
     * @param parameters  non-null immutable callback parameters
     * @param options     non-null immutable typed options
     * @param problem     protocol problem, or {@code null} on success
     * @author Kimi Liu
     */
    record Outbound(Context context, Kind kind, Address destination, Mode mode, Parameters parameters, Options options,
            Problem problem) implements Callback {

        /**
         * Validates and creates an outbound callback snapshot.
         *
         * @throws ValidateException if a required component is null
         */
        public Outbound {
            context = Assert.notNull(context, () -> new ValidateException("Callback context must not be null"));
            kind = Assert.notNull(kind, () -> new ValidateException("Callback kind must not be null"));
            mode = Assert.notNull(mode, () -> new ValidateException("Callback mode must not be null"));
            parameters = Assert
                    .notNull(parameters, () -> new ValidateException("Callback parameters must not be null"));
            options = Assert.notNull(options, () -> new ValidateException("Callback options must not be null"));
        }

        /**
         * Returns a copy containing one replacement parameter value.
         *
         * @param name  non-blank parameter name
         * @param value non-null parameter value
         * @return immutable callback copy
         * @throws ValidateException if the name, value, or resulting parameter count is invalid
         */
        public Outbound with(final String name, final String value) {
            return new Outbound(context, kind, destination, mode, parameters.with(name, value), options, problem);
        }

        /**
         * Returns a copy without the named parameter.
         *
         * @param name non-blank parameter name
         * @return immutable callback copy
         * @throws ValidateException if the name is invalid
         */
        public Outbound without(final String name) {
            return new Outbound(context, kind, destination, mode, parameters.without(name), options, problem);
        }

        /**
         * Returns a copy containing one replacement typed option.
         *
         * @param key   non-null typed option key
         * @param value option value
         * @param <T>   option value type
         * @return immutable callback copy
         */
        public <T> Outbound with(final Options.Key<T> key, final T value) {
            return new Outbound(context, kind, destination, mode, parameters, options.with(key, value), problem);
        }

        /**
         * Returns a redacted representation that never includes callback parameters.
         *
         * @return fixed redacted text
         */
        @Override
        public String toString() {
            return "Callback.Outbound[REDACTED]";
        }

        /**
         * Mutable builder for an immutable outbound callback.
         *
         * @author Kimi Liu
         */
        public static final class Builder {

            /**
             * Operation context shared by all snapshots built from this builder.
             */
            private final Context context;

            /**
             * Interaction kind, defaulting to authorization.
             */
            private Kind kind = Kind.AUTHORIZATION;

            /**
             * Optional Fabric redirect address.
             */
            private Address destination;

            /**
             * Response transport mode, defaulting to query parameters.
             */
            private Mode mode = Mode.QUERY;

            /**
             * Immutable parameter snapshot accumulated by the builder.
             */
            private Parameters parameters = Parameters.empty();

            /**
             * Typed options, defaulting to empty.
             */
            private Options options = Options.empty();

            /**
             * Optional protocol problem.
             */
            private Problem problem;

            /**
             * Creates an outbound builder for one operation.
             *
             * @param context non-null operation context
             * @throws ValidateException if {@code context} is null
             */
            private Builder(final Context context) {
                this.context = Assert
                        .notNull(context, () -> new ValidateException("Callback context must not be null"));
            }

            /**
             * Sets the interaction kind.
             *
             * @param value callback kind
             * @return this builder
             */
            public Builder kind(final Kind value) {
                kind = value;
                return this;
            }

            /**
             * Sets the optional Fabric redirect address.
             *
             * @param value destination, or {@code null} for a direct response
             * @return this builder
             */
            public Builder destination(final Address value) {
                destination = value;
                return this;
            }

            /**
             * Sets the response transport mode.
             *
             * @param value response mode
             * @return this builder
             */
            public Builder mode(final Mode value) {
                mode = value;
                return this;
            }

            /**
             * Replaces all callback parameters with a defensive snapshot.
             *
             * @param value parameter multimap
             * @return this builder
             * @throws ValidateException if the map exceeds callback bounds
             */
            public Builder parameters(final Map<String, List<String>> value) {
                parameters = Parameters.from(value);
                return this;
            }

            /**
             * Adds or replaces one callback parameter.
             *
             * @param name  parameter name
             * @param value parameter value
             * @return this builder
             * @throws ValidateException if the parameter exceeds callback bounds
             */
            public Builder parameter(final String name, final String value) {
                parameters = parameters.with(name, value);
                return this;
            }

            /**
             * Replaces the typed option snapshot.
             *
             * @param value non-null options
             * @return this builder
             */
            public Builder options(final Options value) {
                options = value;
                return this;
            }

            /**
             * Sets the optional protocol problem.
             *
             * @param value problem, or {@code null} for success
             * @return this builder
             */
            public Builder problem(final Problem value) {
                problem = value;
                return this;
            }

            /**
             * Builds an immutable validated callback.
             *
             * @return outbound callback snapshot
             * @throws ValidateException if a required component is null
             */
            public Outbound build() {
                return new Outbound(context, kind, destination, mode, parameters, options, problem);
            }
        }
    }

    /**
     * Immutable callback parameter multimap with duplicate-safe accessors.
     *
     * @author Kimi Liu
     */
    final class Parameters {

        /**
         * Shared immutable empty parameter set.
         */
        private static final Parameters EMPTY = new Parameters(Map.of());

        /**
         * Immutable insertion-ordered parameter values.
         */
        private final Map<String, List<String>> values;

        /**
         * Creates a parameter snapshot from already validated immutable lists.
         *
         * @param values owned parameter map
         */
        private Parameters(final Map<String, List<String>> values) {
            this.values = Collections.unmodifiableMap(values);
        }

        /**
         * Returns the shared empty parameter set.
         *
         * @return immutable empty parameters
         */
        public static Parameters empty() {
            return EMPTY;
        }

        /**
         * Creates a bounded defensive snapshot of a parameter multimap.
         *
         * @param source non-null source map containing at most 64 names
         * @return immutable parameter snapshot
         * @throws ValidateException if a name, value, list, or bound is invalid
         */
        public static Parameters from(final Map<String, List<String>> source) {
            final Map<String, List<String>> input = Assert
                    .notNull(source, () -> new ValidateException("Callback parameter map must not be null"));
            if (input.isEmpty()) {
                return empty();
            }
            if (input.size() > Normal._64) {
                throw new ValidateException("Callback contains too many parameters");
            }
            final LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
            input.forEach((name, entries) -> result.put(name(name), values(entries)));
            return new Parameters(result);
        }

        /**
         * Validates a callback parameter name.
         *
         * @param value non-blank name of at most 128 UTF-16 code units
         * @return validated name
         * @throws ValidateException if the name is blank or too long
         */
        private static String name(final String value) {
            final String name = Assert.notBlank(value, () -> new ValidateException("Callback parameter name is blank"));
            if (name.length() > Normal._128) {
                throw new ValidateException("Callback parameter name is too long");
            }
            return name;
        }

        /**
         * Validates one callback parameter value.
         *
         * @param value non-null value of at most 8192 UTF-16 code units
         * @return validated value
         * @throws ValidateException if the value is null or too long
         */
        private static String value(final String value) {
            final String checked = Assert
                    .notNull(value, () -> new ValidateException("Callback parameter value must not be null"));
            if (checked.length() > Normal._8192) {
                throw new ValidateException("Callback parameter value is too long");
            }
            return checked;
        }

        /**
         * Creates an immutable validated copy of a value list.
         *
         * @param source non-null source list
         * @return immutable value list
         * @throws ValidateException if the list or any value is invalid
         */
        private static List<String> values(final List<String> source) {
            final List<String> input = Assert
                    .notNull(source, () -> new ValidateException("Callback parameter values must not be null"));
            final ArrayList<String> result = new ArrayList<>(input.size());
            for (final String entry : input) {
                result.add(value(entry));
            }
            return List.copyOf(result);
        }

        /**
         * Returns the immutable parameter multimap.
         *
         * @return immutable insertion-ordered map
         */
        public Map<String, List<String>> values() {
            return values;
        }

        /**
         * Reads a parameter that must occur at most once.
         *
         * @param name non-blank parameter name
         * @return optional unique value
         * @throws ValidateException if the name is invalid or multiple values exist
         */
        public Optional<String> single(final String name) {
            final List<String> entries = values.get(name(name));
            if (entries == null || entries.isEmpty()) {
                return Optional.empty();
            }
            if (entries.size() != 1) {
                throw new ValidateException("Callback parameter must occur at most once: " + name);
            }
            return Optional.of(entries.get(0));
        }

        /**
         * Reads a required parameter that must occur exactly once.
         *
         * @param name non-blank parameter name
         * @return required value
         * @throws ValidateException if the name is invalid, absent, or repeated
         */
        public String require(final String name) {
            return single(name)
                    .orElseThrow(() -> new ValidateException("Required callback parameter is missing: " + name));
        }

        /**
         * Selects the only present parameter among a set of aliases.
         *
         * @param aliases non-null alias array whose entries are non-blank
         * @return optional selected value
         * @throws ValidateException if aliases are invalid, repeated, or conflicting
         */
        public Optional<String> unique(final String... aliases) {
            Assert.notNull(aliases, () -> new ValidateException("Callback aliases must not be null"));
            String selected = null;
            String selectedName = null;
            for (final String alias : aliases) {
                final Optional<String> candidate = single(alias);
                if (candidate.isPresent()) {
                    if (selected != null) {
                        throw new ValidateException(
                                "Conflicting callback parameter aliases: " + selectedName + " and " + alias);
                    }
                    selected = candidate.get();
                    selectedName = alias;
                }
            }
            return Optional.ofNullable(selected);
        }

        /**
         * Returns every value for one parameter name.
         *
         * @param name non-blank parameter name
         * @return immutable values, possibly empty
         * @throws ValidateException if the name is invalid
         */
        public List<String> all(final String name) {
            return values.getOrDefault(name(name), List.of());
        }

        /**
         * Tests whether a validated parameter name is present.
         *
         * @param name non-blank parameter name
         * @return {@code true} when the name is present
         * @throws ValidateException if the name is invalid
         */
        public boolean contains(final String name) {
            return values.containsKey(name(name));
        }

        /**
         * Returns a copy containing one replacement value.
         *
         * @param name  non-blank parameter name
         * @param value non-null parameter value
         * @return immutable parameter copy
         * @throws ValidateException if the name, value, or resulting count is invalid
         */
        public Parameters with(final String name, final String value) {
            final String key = name(name);
            final String checked = value(value);
            final LinkedHashMap<String, List<String>> updated = new LinkedHashMap<>(values);
            if (!updated.containsKey(key) && updated.size() >= Normal._64) {
                throw new ValidateException("Callback contains too many parameters");
            }
            updated.put(key, List.of(checked));
            return new Parameters(updated);
        }

        /**
         * Returns a copy without one parameter name.
         *
         * @param name non-blank parameter name
         * @return immutable parameter copy, or this instance when absent
         * @throws ValidateException if the name is invalid
         */
        public Parameters without(final String name) {
            final String key = name(name);
            if (!values.containsKey(key)) {
                return this;
            }
            final LinkedHashMap<String, List<String>> updated = new LinkedHashMap<>(values);
            updated.remove(key);
            return updated.isEmpty() ? empty() : new Parameters(updated);
        }

        /**
         * Returns a redacted representation that never includes parameter values.
         *
         * @return fixed redacted text
         */
        @Override
        public String toString() {
            return "Callback.Parameters[REDACTED]";
        }
    }

    /**
     * Structured protocol error carried by a valid callback.
     *
     * @param code        non-blank protocol error code
     * @param description optional bounded human-readable description
     * @param reference   optional absolute problem information URI without user information or fragment
     * @author Kimi Liu
     */
    record Problem(String code, String description, URI reference) {

        /**
         * Validates and creates protocol problem details.
         *
         * @throws ValidateException if the code, description, or reference is invalid
         */
        public Problem {
            if (StringKit.isBlank(code)) {
                throw new ValidateException("Callback problem code must not be blank");
            }
            code = code.trim();
            description = description == null ? "" : description;
            if (description.length() > Normal._8192) {
                throw new ValidateException("Callback problem description is too long");
            }
            reference = Callback.reference(reference, "Callback problem reference");
        }

        /**
         * Returns a redacted representation that omits error details.
         *
         * @return fixed redacted text
         */
        @Override
        public String toString() {
            return "Callback.Problem[REDACTED]";
        }
    }

}
