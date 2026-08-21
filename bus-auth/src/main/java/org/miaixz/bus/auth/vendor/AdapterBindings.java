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
package org.miaixz.bus.auth.vendor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds exact platform, variant, manifest, and adapter-factory bindings.
 * <p>
 * Concrete options identify their own runtime type through {@link VendorOptions#type()}; this directory deliberately
 * stores no second options class token.
 * </p>
 */
final class AdapterBindings {

    private final Map<Key, Binding> bindings;

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
     */
    static <D extends VariantManifest<O>, O extends VendorOptions<?>> Binding binding(
            final Class<D> manifestType,
            final VendorAdapter.Factory<O> factory) {
        final Class<D> checkedManifestType = Assert.notNull(manifestType, "Vendor manifest type must not be null");
        final VendorAdapter.Factory<O> checkedFactory = Assert
                .notNull(factory, "Vendor adapter factory must not be null");
        return new Binding((namespaceId, sourceId, manifest, variant, options, services) -> checkedFactory
                .create(namespaceId, sourceId, checkedManifestType.cast(manifest), variant, narrow(options), services));
    }

    /**
     * Creates a checked binding for one externally contributed manifest instance.
     */
    static <O extends VendorOptions<?>> Binding binding(
            final VariantManifest<O> manifest,
            final VendorAdapter.Factory<O> factory) {
        final VariantManifest<O> checkedManifest = Assert.notNull(manifest, "Vendor manifest must not be null");
        final VendorAdapter.Factory<O> checkedFactory = Assert
                .notNull(factory, "Vendor adapter factory must not be null");
        return new Binding((namespaceId, sourceId, selected, variant, options, services) -> {
            if (selected != checkedManifest) {
                throw new ValidateException("Vendor binding received a different manifest instance");
            }
            return checkedFactory.create(namespaceId, sourceId, checkedManifest, variant, narrow(options), services);
        });
    }

    /**
     * Narrows through the type fact declared by the options value itself.
     * <p>
     * Java erases the generic relationship retained by the binding map, so one audited cast is unavoidable unless a
     * second options-class token is added to Factory or Manifest. The framework deliberately keeps
     * {@link VendorOptions#type()} as the only runtime type fact, validates that it names the concrete value, and keeps
     * the cast at this single package-private boundary. The typed {@link VendorDriver} and built-in factory method
     * references establish the Manifest/Options/Factory relation at assembly time; any dishonest external generic
     * implementation is normalized by {@link #create} as a validation failure.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private static <O extends VendorOptions<?>> O narrow(final VendorOptions<?> options) {
        final VendorOptions<?> checked = Assert.notNull(options, "Vendor options must not be null");
        if (checked.type() != checked.getClass()) {
            throw new ValidateException("Vendor options must declare their exact implementation type");
        }
        return (O) checked;
    }

    Map<Key, Binding> bindings() {
        return bindings;
    }

    VendorAdapter create(
            final String namespaceId,
            final String sourceId,
            final VariantManifest<?> manifest,
            final VariantManifest.Variant variant,
            final VendorOptions<?> options,
            final DriverServices services) {
        Assert.notBlank(namespaceId, "Vendor adapter namespace id must not be blank");
        Assert.notBlank(sourceId, "Vendor adapter Source id must not be blank");
        final VariantManifest<?> checkedManifest = Assert.notNull(manifest, "Vendor adapter manifest must not be null");
        final VariantManifest.Variant checkedVariant = Assert
                .notNull(variant, "Vendor adapter variant must not be null");
        final VendorOptions<?> checkedOptions = Assert.notNull(options, "Vendor adapter options must not be null");
        final DriverServices checkedServices = Assert.notNull(services, "Vendor adapter services must not be null");
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
                    .create(namespaceId, sourceId, checkedManifest, checkedVariant, checkedOptions, checkedServices);
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

    @FunctionalInterface
    interface Invoker {

        VendorAdapter create(
                String namespaceId,
                String sourceId,
                VariantManifest<?> manifest,
                VariantManifest.Variant variant,
                VendorOptions<?> options,
                DriverServices services);
    }

    record Binding(Invoker invoker) {

        Binding {
            Assert.notNull(invoker, "Vendor binding invoker must not be null");
        }
    }

    record Key(Vendor.Id vendor, Vendor.Variant variant) {

        Key {
            Assert.notNull(vendor, "Vendor adapter key platform must not be null");
            Assert.notNull(variant, "Vendor adapter key variant must not be null");
        }
    }

}
