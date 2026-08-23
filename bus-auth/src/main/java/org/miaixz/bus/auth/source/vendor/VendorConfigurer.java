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
package org.miaixz.bus.auth.source.vendor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.logger.Logger;

/**
 * Coordinates one short-lived client-side Vendor configuration into immutable concrete Options.
 * <p>
 * This service is the sole bridge between plaintext management input and project-owned credential storage. It performs
 * exact manifest lookup, input normalization, one credential write, and one bound Options factory invocation. It does
 * not create a Source, mutate the Roster, load credentials, start authorization, or implement project CRUD.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorConfigurer {

    /**
     * Common form keys carried by typed Configuration components rather than the parameter object.
     */
    private static final Set<String> COMMON_FIELDS = Set.of("clientId", "credential", "redirectUri", "scopes");

    /**
     * Immutable exact Options factory bindings.
     */
    private final OptionsBindings bindings;

    /**
     * Project-owned recoverable credential storage port.
     */
    private final VendorCredentialWriter writer;

    /**
     * Creates one client-side configuration coordinator for an immutable Vendor module.
     *
     * @param bindings immutable exact Options factory bindings
     * @param writer   project-owned credential storage port
     */
    public VendorConfigurer(final OptionsBindings bindings, final VendorCredentialWriter writer) {
        this.bindings = Assert.notNull(bindings, "Vendor configuration bindings must not be null");
        this.writer = Assert.notNull(writer, "Vendor credential writer must not be null");
    }

    /**
     * Validates browser and direct callback shape from immutable manifest targets.
     *
     * @param variant  selected immutable Vendor variant
     * @param callback optional configured callback URI
     */
    private static void callback(final VendorManifest.Variant variant, final Optional<String> callback) {
        final boolean browser = variant.targets().authorization().isPresent();
        if (browser != callback.isPresent()) {
            throw new ValidateException(browser ? "Vendor browser variant requires a callback"
                    : "Vendor direct variant does not accept a callback");
        }
    }

    /**
     * Freezes explicit scopes or selects immutable manifest defaults.
     *
     * @param variant   selected immutable Vendor variant
     * @param requested explicitly requested scopes
     * @return immutable selected scope list
     */
    private static List<String> scopes(final VendorManifest.Variant variant, final List<String> requested) {
        final List<String> selected = requested.isEmpty() ? variant.defaultScopes() : requested;
        final List<String> copy = new ArrayList<>(selected.size());
        for (String scope : selected) {
            final String checked = Assert.notBlank(scope, "Vendor configuration scope must not be blank");
            if (copy.contains(checked)) {
                throw new ValidateException("Vendor configuration scopes must not contain duplicates");
            }
            copy.add(checked);
        }
        return List.copyOf(copy);
    }

    /**
     * Applies manifest form defaults and rejects undeclared or missing parameter fields.
     *
     * @param form      immutable Vendor management form
     * @param submitted submitted parameter object
     * @return validated immutable parameter object
     */
    private static JsonValue.ObjectValue parameters(final Scheme.Form form, final JsonValue.ObjectValue submitted) {
        final Map<String, Scheme.Form.Field> declared = new LinkedHashMap<>();
        for (Scheme.Form.Section section : form.sections()) {
            for (Scheme.Form.Field field : section.fields()) {
                if (!COMMON_FIELDS.contains(field.key()) && declared.putIfAbsent(field.key(), field) != null) {
                    throw new ValidateException("Vendor form declares a duplicate parameter field");
                }
            }
        }
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        submitted.values().forEach((name, value) -> {
            if (!declared.containsKey(name)) {
                throw new ValidateException("Vendor configuration contains an undeclared parameter");
            }
            values.put(name, value);
        });
        for (Scheme.Form.Field field : declared.values()) {
            if (!values.containsKey(field.key()) && field.defaultValue().isPresent()) {
                values.put(field.key(), field.defaultValue().getOrNull());
            }
            if (field.required() && !values.containsKey(field.key())) {
                throw new ValidateException("Vendor configuration omits a required parameter");
            }
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Invokes one bound immutable Options factory after validating the project response.
     *
     * @param binding    exact manifest and Options factory binding
     * @param clientId   configured public client identifier
     * @param credential stored external credential reference
     * @param callback   optional validated callback URI
     * @param scopes     immutable selected scopes
     * @param parameters validated Vendor-specific parameters
     * @return immutable Vendor options outcome
     */
    private static Outcome<VendorOptions<?>> create(
            final OptionsBindings.Binding binding,
            final String clientId,
            final Credential.Reference credential,
            final Optional<String> callback,
            final List<String> scopes,
            final JsonValue.ObjectValue parameters) {
        try {
            final Credential.Reference checked = Assert
                    .notNull(credential, "Vendor credential writer returned no reference");
            if (checked.type() != binding.variant().credentialType()) {
                return failed("Vendor credential writer returned an incompatible reference type");
            }
            final VendorOptions<?> options = Assert.notNull(
                    binding.factory().create(binding.variant(), clientId, checked, callback, scopes, parameters),
                    "Vendor Options factory returned no value");
            if (!options.vendor().equals(binding.variant().platform())
                    || !options.variant().equals(binding.variant().variant())
                    || options.credential().type() != binding.variant().credentialType()) {
                return failed("Vendor Options factory returned inconsistent routing data");
            }
            Logger.debug(
                    false,
                    "Auth",
                    "Vendor Options created: vendor={}, variant={}, options={}",
                    options.vendor().value(),
                    options.variant().value(),
                    options.getClass().getName());
            return Outcome.succeeded(options);
        } catch (RuntimeException cause) {
            Logger.warn(
                    false,
                    "Auth",
                    "Vendor Options creation rejected: vendor={}, variant={}, exception={}",
                    binding.variant().platform().value(),
                    binding.variant().variant().value(),
                    cause.getClass().getSimpleName());
            return rejected("Vendor Options validation failed");
        }
    }

    /**
     * Creates a completed asynchronous outcome.
     *
     * @param <T>     outcome value type
     * @param outcome outcome to expose
     * @return completed outcome stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates one expected configuration rejection without sensitive details.
     *
     * @param <T>         outcome value type
     * @param description safe rejection description
     * @return immutable rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates one operational configuration failure without sensitive details.
     *
     * @param <T>         outcome value type
     * @param description safe failure description
     * @return immutable failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Returns the one-time management form for an exact Vendor platform variant.
     *
     * @param vendor  exact platform identifier
     * @param variant exact platform variant identifier
     * @return immutable one-time configuration form
     */
    public Scheme.Form form(final Vendor.Id vendor, final Vendor.Variant variant) {
        final OptionsBindings.Binding binding = bindings.resolve(vendor, variant);
        return binding.manifest().form(binding.variant().variant());
    }

    /**
     * Stores one plaintext Vendor credential and constructs its immutable concrete Options value.
     *
     * @param configuration short-lived client-side configuration command
     * @param context       immutable non-secret invocation context
     * @param timeout       shared end-to-end operation timeout
     * @return asynchronous configuration outcome containing only immutable Options and a credential reference
     */
    public CompletionStage<Outcome<VendorOptions<?>>> configure(
            final Vendor.Configuration configuration,
            final Context context,
            final Timeout timeout) {
        final Vendor.Configuration checked = Assert.notNull(configuration, "Vendor configuration must not be null");
        final Context checkedContext = Assert.notNull(context, "Vendor configuration context must not be null");
        final Timeout checkedTimeout = Assert.notNull(timeout, "Vendor configuration timeout must not be null");
        Logger.debug(
                true,
                "Auth",
                "Vendor configuration started: vendor={}, variant={}",
                checked.vendor().value(),
                checked.variant().value());
        final OptionsBindings.Binding binding;
        final List<String> scopes;
        final JsonValue.ObjectValue parameters;
        try {
            binding = bindings.resolve(checked.vendor(), checked.variant());
            callback(binding.variant(), checked.callback());
            scopes = scopes(binding.variant(), checked.scopes());
            parameters = parameters(binding.manifest().form(binding.variant().variant()), checked.parameters());
        } catch (RuntimeException cause) {
            checked.credential().close();
            Logger.warn(
                    false,
                    "Auth",
                    "Vendor configuration rejected: vendor={}, variant={}, exception={}",
                    checked.vendor().value(),
                    checked.variant().value(),
                    cause.getClass().getSimpleName());
            return completed(rejected("Vendor client configuration is invalid"));
        }

        final CompletionStage<Outcome<Credential.Reference>> stage;
        final SecretLease credential = checked.credential();
        try {
            stage = writer.write(
                    checked.vendor(),
                    checked.variant(),
                    binding.variant().credentialType(),
                    checked.clientId(),
                    credential,
                    checkedContext,
                    checkedTimeout);
        } catch (RuntimeException cause) {
            Logger.error(
                    false,
                    "Auth",
                    cause,
                    "Vendor credential storage invocation failed: vendor={}, variant={}, exception={}",
                    checked.vendor().value(),
                    checked.variant().value(),
                    cause.getClass().getSimpleName());
            return completed(failed("Vendor credential storage failed"));
        } finally {
            credential.close();
        }
        if (stage == null) {
            return completed(failed("Vendor credential storage returned no stage"));
        }
        return stage.handle((outcome, cause) -> {
            if (cause != null || outcome == null) {
                return failed("Vendor credential storage did not complete normally");
            }
            return switch (outcome) {
                case Outcome.Succeeded<Credential.Reference> success -> create(
                        binding,
                        checked.clientId(),
                        success.value(),
                        checked.callback(),
                        scopes,
                        parameters);
                case Outcome.Rejected<Credential.Reference> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<Credential.Reference> failed -> Outcome.failed(failed.failure());
                default -> failed("Unsupported Vendor credential outcome");
            };
        });
    }

}
