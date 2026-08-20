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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares one strongly typed authentication capability actually implemented by a runtime provider.
 * <p>
 * Request and response classes are part of the declaration, allowing Registry invocation to retain compile-time type
 * relationships instead of reducing protocol operations to generic requests. Capability declarations are internal
 * routing metadata and must never be serialized as protocol wire messages or Discovery documents.
 * </p>
 *
 * @param key          stable protocol and operation key
 * @param requestType  exact standard request type accepted by the capability
 * @param responseType exact standard response type returned through {@link Outcome}
 * @param direction    runtime provider direction
 * @param interactions interaction patterns supported by the operation
 * @param security     minimum caller or subject authentication boundary
 * @param <Q>          standard request type
 * @param <S>          standard success value type
 * @author Kimi Liu
 */
public record Capability<Q, S>(Key key, Class<Q> requestType, Class<S> responseType, Direction direction,
        Set<Interaction> interactions, Security security) {

    /**
     * Creates a complete capability declaration.
     *
     * @param key          stable protocol and operation key
     * @param requestType  exact standard request type
     * @param responseType exact standard success value type
     * @param direction    runtime provider direction
     * @param interactions supported interaction patterns
     * @param security     minimum authentication boundary
     * @throws IllegalArgumentException if any component is {@code null}
     */
    public Capability {
        Assert.notNull(key, "Capability key must not be null");
        Assert.notNull(requestType, "Capability request type must not be null");
        Assert.notNull(responseType, "Capability response type must not be null");
        Assert.notNull(direction, "Capability direction must not be null");
        Assert.notNull(interactions, "Capability interactions must not be null");
        Assert.notEmpty(interactions, "Capability interactions must not be empty");
        interactions = Set.copyOf(interactions);
        Assert.notNull(security, "Capability security must not be null");
    }

    /**
     * Identifies the protocol role implemented by a compiled Source runtime.
     * <p>
     * The enum names are historical role names and do not identify the persistence {@link Provider} or {@link Source}
     * entity category. Both directions are selected and configured by a Source registration.
     * </p>
     *
     * @author Kimi Liu
     */
    public enum Direction {

        /**
         * Server-role capability implemented by a Source runtime.
         */
        PROVIDER,

        /**
         * Client-role capability implemented by a Source runtime.
         */
        SOURCE

    }

    /**
     * Describes the user or device interaction required to complete an operation.
     *
     * @author Kimi Liu
     */
    public enum Interaction {

        /**
         * Completes without redirecting a user agent or polling a device flow.
         */
        DIRECT,

        /**
         * Requires a user-agent redirect and correlated callback.
         */
        REDIRECT,

        /**
         * Requires a device authorization interaction and polling.
         */
        DEVICE

    }

    /**
     * Describes the authentication boundary required before invoking a capability.
     *
     * @author Kimi Liu
     */
    public enum Security {

        /**
         * Operation is available without client or subject authentication.
         */
        PUBLIC,

        /**
         * Operation requires an authenticated protocol client.
         */
        CLIENT_AUTHENTICATED,

        /**
         * Operation requires an authenticated subject context.
         */
        SUBJECT_AUTHENTICATED

    }

    /**
     * Identifies either an industry-standard protocol operation or an application-level framework operation.
     * <p>
     * A present protocol means that the operation is defined by that protocol. An empty protocol is reserved for
     * application-level framework capabilities such as Source authentication orchestration and must never be exposed as
     * protocol metadata or serialized on a protocol wire boundary.
     * </p>
     *
     * @param protocol  protocol that formally defines the operation, or empty for an application-level capability
     * @param operation stable operation name used only for internal routing
     * @author Kimi Liu
     */
    public record Key(Optional<Protocol> protocol, String operation) {

        /**
         * Creates an internal capability key.
         *
         * @param protocol  protocol that defines the operation, or empty for an application-level capability
         * @param operation non-blank internal operation name
         * @throws IllegalArgumentException if the protocol is {@code null} or the operation is blank
         */
        public Key {
            Assert.notNull(protocol, "Capability protocol must not be null");
            Assert.notBlank(operation, "Capability operation must not be blank");
        }

        /**
         * Creates a key for an operation formally defined by an industry protocol.
         *
         * @param protocol  protocol that defines the operation
         * @param operation stable operation name within the protocol
         * @return protocol-scoped capability key
         * @throws IllegalArgumentException if the protocol is {@code null} or the operation is blank
         */
        public static Key standard(final Protocol protocol, final String operation) {
            return new Key(Optional.of(Assert.notNull(protocol, "Capability protocol must not be null")), operation);
        }

        /**
         * Creates a key for an application-level framework operation that is not defined by one protocol.
         *
         * @param operation globally stable framework operation name
         * @return application-scoped capability key
         * @throws IllegalArgumentException if the operation is blank
         */
        public static Key application(final String operation) {
            return new Key(Optional.empty(), operation);
        }

    }

    /**
     * Holds the immutable capabilities implemented by one profile or runtime provider.
     * <p>
     * Entries preserve declaration order and must have unique {@link Key} values. A manifest is internal compilation
     * metadata, not a protocol response or user-visible conformance claim by itself.
     * </p>
     *
     * @param capabilities implemented capabilities in stable declaration order
     * @author Kimi Liu
     */
    public record Manifest(List<Capability<?, ?>> capabilities) {

        /**
         * Creates an immutable manifest after checking key uniqueness.
         *
         * @param capabilities implemented capabilities in stable declaration order
         * @throws IllegalArgumentException if the list, an entry, or an entry key is duplicated
         */
        public Manifest {
            Assert.notNull(capabilities, "Capability manifest entries must not be null");
            final List<Capability<?, ?>> copy = new ArrayList<>(capabilities.size());
            final Set<Key> keys = new HashSet<>(capabilities.size());
            for (Capability<?, ?> capability : capabilities) {
                final Capability<?, ?> entry = Assert.notNull(capability, "Capability manifest entry must not be null");
                Assert.isTrue(keys.add(entry.key()), "Capability manifest contains duplicate key: {}", entry.key());
                copy.add(entry);
            }
            capabilities = List.copyOf(copy);
        }

    }

}
