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

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds exact platform, variant, manifest, and adapter-factory bindings.
 * <p>
 * Concrete options identify their own runtime type through {@link VendorOptions#type()}; this binding index
 * deliberately stores no second options class token.
 * </p>
 *
 * @author Kimi Liu
 */
final class AdapterBindings {

    /**
     * Immutable lookup from an exact Vendor variant key to its checked factory invocation.
     */
    private final Map<Key, Binding> bindings;

    /**
     * Validates and freezes the complete Vendor adapter binding index.
     *
     * @param bindings exact binding map assembled for the runtime
     */
    AdapterBindings(final Map<Key, Binding> bindings) {
        Assert.notNull(bindings, "Vendor adapter bindings must not be null");
        final Map<Key, Binding> copy = new LinkedHashMap<>(bindings.size());
        bindings.forEach(
                (key, binding) -> copy.put(
                        Assert.notNull(key, "Vendor adapter factory key must not be null"),
                        Assert.notNull(binding, "Vendor adapter binding must not be null")));
        this.bindings = Map.copyOf(copy);
    }

    /**
     * Creates a checked binding for one built-in manifest class.
     *
     * @param <D>          concrete Vendor manifest type
     * @param <O>          concrete Vendor options type
     * @param manifestType runtime manifest class used to validate the selected manifest
     * @param factory      typed adapter factory bound to the manifest
     * @return checked erased binding stored by the index
     */
    static <D extends VendorManifest<O>, O extends VendorOptions<?>> Binding binding(
            final Class<D> manifestType,
            final VendorAdapter.Factory<O> factory) {
        final Class<D> checkedManifestType = Assert.notNull(manifestType, "Vendor manifest type must not be null");
        final VendorAdapter.Factory<O> checkedFactory = Assert
                .notNull(factory, "Vendor adapter factory must not be null");
        return new Binding((spaceId, sourceId, manifest, variant, options, services) -> checkedFactory
                .create(spaceId, sourceId, checkedManifestType.cast(manifest), variant, narrow(options), services));
    }

    /**
     * Creates a checked binding for one externally registered manifest instance.
     *
     * @param <O>      concrete Vendor options type
     * @param manifest exact externally registered manifest instance
     * @param factory  typed adapter factory bound to that instance
     * @return checked erased binding stored by the index
     */
    static <O extends VendorOptions<?>> Binding binding(
            final VendorManifest<O> manifest,
            final VendorAdapter.Factory<O> factory) {
        final VendorManifest<O> checkedManifest = Assert.notNull(manifest, "Vendor manifest must not be null");
        final VendorAdapter.Factory<O> checkedFactory = Assert
                .notNull(factory, "Vendor adapter factory must not be null");
        return new Binding((spaceId, sourceId, selected, variant, options, services) -> {
            if (selected != checkedManifest) {
                throw new ValidateException("Vendor binding received a different manifest instance");
            }
            return checkedFactory.create(spaceId, sourceId, checkedManifest, variant, narrow(options), services);
        });
    }

    /**
     * Narrows through the type fact declared by the options value itself.
     * <p>
     * Java erases the generic relationship retained by the binding map, so one audited cast is unavoidable unless a
     * second options-class token is added to Factory or Manifest. The framework deliberately keeps
     * {@link VendorOptions#type()} as the only runtime type fact, validates that it names the concrete value, and keeps
     * the cast at this single package-private boundary. The typed {@link VendorBinding} and {@link VendorConnector}
     * factory method references establish the Manifest/Options/Factory relation at assembly time; any dishonest generic
     * implementation is normalized by {@link #create} as a validation failure.
     * </p>
     *
     * @param <O>     concrete Vendor options type expected by the binding
     * @param options runtime options value declaring its exact implementation class
     * @return options narrowed to the binding's concrete type
     */
    @SuppressWarnings("unchecked")
    private static <O extends VendorOptions<?>> O narrow(final VendorOptions<?> options) {
        final VendorOptions<?> checked = Assert.notNull(options, "Vendor options must not be null");
        if (checked.type() != checked.getClass()) {
            throw new ValidateException("Vendor options must declare their exact implementation type");
        }
        return (O) checked;
    }

    /**
     * Returns the immutable exact-key binding index.
     *
     * @return immutable Vendor adapter bindings
     */
    Map<Key, Binding> bindings() {
        return bindings;
    }

    /**
     * Creates one Vendor adapter after validating the complete manifest, variant, options, and service relation.
     *
     * @param spaceId  runtime space identifier
     * @param sourceId configured Source identifier
     * @param manifest selected Vendor manifest
     * @param variant  selected manifest variant
     * @param options  validated options for the selected variant
     * @param services capability-limited Source services
     * @return adapter exposing exactly the selected variant capabilities
     * @throws ValidateException if routing is inconsistent, no factory exists, or factory construction fails
     */
    VendorAdapter create(
            final String spaceId,
            final String sourceId,
            final VendorManifest<?> manifest,
            final VendorManifest.Variant variant,
            final VendorOptions<?> options,
            final SourceServices services) {
        Assert.notBlank(spaceId, "Vendor adapter space id must not be blank");
        Assert.notBlank(sourceId, "Vendor adapter Source id must not be blank");
        final VendorManifest<?> checkedManifest = Assert.notNull(manifest, "Vendor adapter manifest must not be null");
        final VendorManifest.Variant checkedVariant = Assert
                .notNull(variant, "Vendor adapter variant must not be null");
        final VendorOptions<?> checkedOptions = Assert.notNull(options, "Vendor adapter options must not be null");
        final SourceServices checkedServices = Assert.notNull(services, "Vendor adapter services must not be null");
        if (!checkedOptions.vendor().equals(checkedManifest.vendor())
                || !checkedVariant.platform().equals(checkedManifest.vendor())
                || !checkedOptions.variant().equals(checkedVariant.variant())) {
            throw new ValidateException("Vendor options routing is invalid");
        }
        final Binding binding = bindings.get(new Key(checkedManifest.vendor(), checkedVariant.variant()));
        if (binding == null) {
            throw new ValidateException("Vendor adapter factory is not registered for the selected platform variant");
        }
        final VendorAdapter adapter;
        try {
            adapter = binding.invoker()
                    .create(spaceId, sourceId, checkedManifest, checkedVariant, checkedOptions, checkedServices);
        } catch (RuntimeException cause) {
            if (cause instanceof ValidateException validation) {
                throw validation;
            }
            throw new ValidateException("Vendor adapter factory failed", cause);
        }
        if (adapter == null || !adapter.manifest().equals(checkedVariant.capabilityManifest())) {
            throw new ValidateException("Vendor adapter must expose the selected variant capabilities");
        }
        return adapter;
    }

    /**
     * Erased, package-private invocation boundary retained inside one checked binding.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    interface Invoker {

        /**
         * Invokes the factory relation validated when the binding was assembled.
         *
         * @param spaceId  runtime space identifier
         * @param sourceId configured Source identifier
         * @param manifest selected Vendor manifest
         * @param variant  selected manifest variant
         * @param options  selected runtime options
         * @param services capability-limited Source services
         * @return constructed Vendor adapter
         */
        VendorAdapter create(
                String spaceId,
                String sourceId,
                VendorManifest<?> manifest,
                VendorManifest.Variant variant,
                VendorOptions<?> options,
                SourceServices services);

    }

    /**
     * Stores one checked erased factory invocation.
     *
     * @param invoker factory invocation boundary
     * @author Kimi Liu
     */
    record Binding(Invoker invoker) {

        /**
         * Validates one factory invocation binding.
         */
        Binding {
            Assert.notNull(invoker, "Vendor binding invoker must not be null");
        }

    }

    /**
     * Identifies one exact Vendor platform variant.
     *
     * @param vendor  Vendor platform identifier
     * @param variant platform-specific variant identifier
     * @author Kimi Liu
     */
    record Key(Vendor.Id vendor, Vendor.Variant variant) {

        /**
         * Validates one exact Vendor platform variant key.
         */
        Key {
            Assert.notNull(vendor, "Vendor adapter key platform must not be null");
            Assert.notNull(variant, "Vendor adapter key variant must not be null");
        }

    }

}
