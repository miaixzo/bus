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
import java.util.Optional;

import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.fabric.registry.Binding;
import org.miaixz.bus.fabric.registry.Ledger;

/**
 * Authentication component registry backed by the shared Fabric ledger contract.
 *
 * @param <T> registered component type
 * @author Kimi Liu
 */
public interface Registry<T> extends Ledger<T> {

    /**
     * Creates an empty registry backed by a Fabric ledger.
     *
     * @param <T> registered component type
     * @return empty registry
     */
    static <T> Registry<T> create() {
        return adapt(Ledger.create());
    }

    /**
     * Adapts a Fabric ledger without changing its storage semantics.
     *
     * @param ledger Fabric ledger
     * @param <T>    registered component type
     * @return registry view
     * @throws StatefulException if {@code ledger} is null
     */
    static <T> Registry<T> adapt(final Ledger<T> ledger) {
        if (ledger == null) {
            throw new StatefulException("Fabric ledger must not be null");
        }
        if (ledger instanceof Registry<?>) {
            return cast(ledger);
        }
        return new Registry<>() {

            /** {@inheritDoc} This view delegates insertion to the wrapped Fabric ledger. */
            @Override
            public void put(final Binding<T> binding) {
                ledger.put(binding);
            }

            /** {@inheritDoc} This view delegates lookup to the wrapped Fabric ledger. */
            @Override
            public T get(final String key) {
                return ledger.get(key);
            }

            /** {@inheritDoc} This view delegates binding lookup to the wrapped Fabric ledger. */
            @Override
            public Binding<T> binding(final String key) {
                return ledger.binding(key);
            }

            /** {@inheritDoc} This view delegates removal to the wrapped Fabric ledger. */
            @Override
            public T remove(final String key) {
                return ledger.remove(key);
            }

            /** {@inheritDoc} This view returns the wrapped Fabric ledger snapshot. */
            @Override
            public Map<String, Binding<T>> snapshot() {
                return ledger.snapshot();
            }

            /** {@inheritDoc} This view returns the wrapped Fabric ledger size. */
            @Override
            public int size() {
                return ledger.size();
            }
        };
    }

    /**
     * Contains the single safe generic cast used when a ledger already implements this registry contract.
     *
     * @param ledger ledger proven to implement {@link Registry}
     * @param <T>    registered component type
     * @return the same registry instance with its existing generic contract
     */
    @SuppressWarnings("unchecked")
    private static <T> Registry<T> cast(final Ledger<T> ledger) {
        return (Registry<T>) ledger;
    }

    /**
     * Looks up an optional component.
     *
     * @param key stable registry key
     * @return optional registered component
     */
    default Optional<T> find(final String key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Returns a required component.
     *
     * @param key stable registry key
     * @return registered component
     * @throws StatefulException if the key is not registered
     */
    default T require(final String key) {
        return find(key).orElseThrow(() -> new StatefulException("Authentication component is not registered: " + key));
    }

}
