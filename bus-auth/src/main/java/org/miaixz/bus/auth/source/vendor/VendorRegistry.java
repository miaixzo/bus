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

import java.util.Collection;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Collects one complete Vendor manifest, Options factory, and exact per-variant adapter factories.
 * <p>
 * Binding methods are valid only during the connect callback of a registered {@link VendorConnector}. Static factory
 * methods retain the typed constructor boundary in the Vendor package so the central registry never imports a concrete
 * platform class. This contract performs build-time registration only and never invokes a Vendor API or runtime Roster.
 * </p>
 *
 * @author Kimi Liu
 */
public interface VendorRegistry extends Registry<Vendor.Id, VendorConnector> {

    /**
     * Binds one complete typed Vendor binding to the currently active registration.
     *
     * @param binding complete typed Vendor binding
     * @return this registry
     */
    VendorRegistry bind(VendorBinding<?> binding);

    /**
     * Binds one manifest and its exact Options and adapter factories to the active registration.
     *
     * @param manifest       immutable platform manifest
     * @param optionsFactory Options factory shared by the manifest variants
     * @param adapters       adapter factory declarations covering every manifest variant
     * @param <O>            exact immutable Vendor options type
     * @return this registry
     */
    <O extends VendorOptions<?>> VendorRegistry bind(
            VendorManifest<O> manifest,
            VendorOptions.Factory<O> optionsFactory,
            Collection<? extends Adapter<O>> adapters);

    /**
     * Registers one complete Vendor registration.
     *
     * @param connector Vendor connector to register
     * @return this registry
     */
    @Override
    VendorRegistry register(VendorConnector connector);

    /**
     * Atomically registers all supplied Vendor registrations.
     *
     * @param connectors Vendor connectors to register
     * @return this registry
     */
    @Override
    VendorRegistry registerAll(Collection<? extends VendorConnector> connectors);

    /**
     * Removes the complete Vendor registration owned by one platform key.
     *
     * @param key Vendor platform key
     * @return this registry
     */
    @Override
    VendorRegistry unregister(Vendor.Id key);

    /**
     * Atomically removes all Vendor registrations owned by the supplied platform keys.
     *
     * @param keys Vendor platform keys
     * @return this registry
     */
    @Override
    VendorRegistry unregisterAll(Collection<? extends Vendor.Id> keys);

    /**
     * Adapts one common six-component immutable Options constructor to the complete Options factory contract.
     *
     * @param constructor exact six-component Options constructor
     * @param <O>         concrete immutable Vendor options type
     * @return validated complete Options factory
     */
    static <O extends VendorOptions<?>> VendorOptions.Factory<O> options(final OptionsConstructor<O> constructor) {
        final OptionsConstructor<O> checked = Assert
                .notNull(constructor, "Vendor Options constructor must not be null");
        return (variant, clientId, credential, callback, scopes, parameters) -> {
            if (!parameters.values().isEmpty()) {
                throw new ValidateException("Vendor variant does not declare additional Options parameters");
            }
            return checked.create(variant.platform(), variant.variant(), clientId, credential, callback, scopes);
        };
    }

    /**
     * Creates one typed adapter declaration covering the supplied exact variants.
     *
     * @param manifestType concrete platform manifest type
     * @param constructor  concrete platform adapter constructor
     * @param variants     exact variants served by the constructor
     * @param <M>          concrete platform manifest type
     * @param <O>          concrete immutable Vendor options type
     * @return immutable typed adapter declaration
     */
    static <M extends VendorManifest<O>, O extends VendorOptions<?>> Adapter<O> adapter(
            final Class<M> manifestType,
            final AdapterConstructor<M, O> constructor,
            final Vendor.Variant... variants) {
        final Class<M> type = Assert.notNull(manifestType, "Vendor manifest class must not be null");
        final AdapterConstructor<M, O> checked = Assert
                .notNull(constructor, "Vendor adapter constructor must not be null");
        Assert.notNull(variants, "Vendor adapter variants must not be null");
        return new Adapter<>(List.of(variants), (spaceId, sourceId, manifest, variant, options, services) -> checked
                .create(spaceId, sourceId, type.cast(manifest), variant, options, services));
    }

    /**
     * Reads one required string parameter without coercion.
     *
     * @param parameters exact external parameter object
     * @param name       required parameter name
     * @return non-blank parameter value
     * @throws ValidateException if the parameter is absent, non-string, or blank
     */
    static String requiredString(final JsonValue.ObjectValue parameters, final String name) {
        final JsonValue value = Assert.notNull(parameters, "Vendor Options parameters must not be null").values()
                .get(Assert.notBlank(name, "Vendor Options parameter name must not be blank"));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Vendor Options parameter must be a string: " + name);
        }
        return Assert.notBlank(string.value(), "Vendor Options parameter must not be blank");
    }

    /**
     * Reads one optional string parameter without coercion.
     *
     * @param parameters exact external parameter object
     * @param name       optional parameter name
     * @return optional non-blank parameter value
     * @throws ValidateException if a present parameter is non-string or blank
     */
    static Optional<String> optionalString(final JsonValue.ObjectValue parameters, final String name) {
        final JsonValue.ObjectValue checked = Assert.notNull(parameters, "Vendor Options parameters must not be null");
        final String checkedName = Assert.notBlank(name, "Vendor Options parameter name must not be blank");
        return checked.values().containsKey(checkedName) ? Optional.of(requiredString(checked, checkedName))
                : Optional.empty();
    }

    /**
     * Reads one string parameter or returns a non-sensitive default.
     *
     * @param parameters   exact external parameter object
     * @param name         optional parameter name
     * @param defaultValue non-sensitive fallback value
     * @return present non-blank value or the supplied fallback
     * @throws ValidateException if a present parameter is non-string or blank
     */
    static String string(final JsonValue.ObjectValue parameters, final String name, final String defaultValue) {
        final JsonValue.ObjectValue checked = Assert.notNull(parameters, "Vendor Options parameters must not be null");
        final String checkedName = Assert.notBlank(name, "Vendor Options parameter name must not be blank");
        return checked.values().containsKey(checkedName) ? requiredString(checked, checkedName) : defaultValue;
    }

    /**
     * Reads one boolean parameter or returns a non-sensitive default.
     *
     * @param parameters   exact external parameter object
     * @param name         optional parameter name
     * @param defaultValue fallback boolean value
     * @return present boolean value or the supplied fallback
     * @throws ValidateException if a present parameter is not boolean
     */
    static boolean bool(final JsonValue.ObjectValue parameters, final String name, final boolean defaultValue) {
        final JsonValue value = Assert.notNull(parameters, "Vendor Options parameters must not be null").values()
                .get(Assert.notBlank(name, "Vendor Options parameter name must not be blank"));
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof JsonValue.BooleanValue bool)) {
            throw new ValidateException("Vendor Options parameter must be a boolean: " + name);
        }
        return bool.value();
    }

    /**
     * Declares one immutable adapter factory and the exact variants it serves.
     *
     * @param variants exact non-empty variant list
     * @param factory  typed adapter factory
     * @param <O>      concrete immutable Vendor options type
     * @author Kimi Liu
     */
    record Adapter<O extends VendorOptions<?>>(List<Vendor.Variant> variants, VendorAdapter.Factory<O> factory) {

        /**
         * Validates and freezes one typed adapter declaration.
         */
        public Adapter {
            Assert.notNull(variants, "Vendor adapter variants must not be null");
            variants = List.copyOf(variants);
            if (variants.isEmpty()) {
                throw new ValidateException("Vendor adapter declaration must contain at least one variant");
            }
            if (variants.stream().distinct().count() != variants.size()) {
                throw new ValidateException("Vendor adapter declaration must not contain duplicate variants");
            }
            for (Vendor.Variant variant : variants) {
                Assert.notNull(variant, "Vendor adapter variant must not be null");
            }
            factory = Assert.notNull(factory, "Vendor adapter factory must not be null");
        }

    }

    /**
     * Represents one concrete immutable Options constructor with the common six components.
     *
     * @param <O> concrete immutable Vendor options type
     * @author Kimi Liu
     */
    @FunctionalInterface
    interface OptionsConstructor<O extends VendorOptions<?>> {

        /**
         * Creates one immutable concrete Options value.
         *
         * @param vendor     exact platform identifier
         * @param variant    exact platform variant identifier
         * @param clientId   externally supplied public client identifier
         * @param credential external credential reference
         * @param callback   optional registered callback
         * @param scopes     ordered requested scopes
         * @return validated concrete Options value
         */
        O create(
                Vendor.Id vendor,
                Vendor.Variant variant,
                String clientId,
                Credential.Reference credential,
                Optional<String> callback,
                List<String> scopes);

    }

    /**
     * Represents one concrete platform adapter constructor without weakening its manifest or Options type.
     *
     * @param <M> concrete platform manifest type
     * @param <O> concrete immutable Vendor options type
     * @author Kimi Liu
     */
    @FunctionalInterface
    interface AdapterConstructor<M extends VendorManifest<O>, O extends VendorOptions<?>> {

        /**
         * Creates one Source-bound concrete platform adapter.
         *
         * @param spaceId  Source space identifier
         * @param sourceId Source identifier
         * @param manifest exact concrete platform manifest
         * @param variant  exact selected variant
         * @param options  decoded concrete platform options
         * @param services complete caller-owned runtime dependencies
         * @return non-null concrete adapter
         */
        VendorAdapter create(
                String spaceId,
                String sourceId,
                M manifest,
                VendorManifest.Variant variant,
                O options,
                DriverServices services);

    }

}
