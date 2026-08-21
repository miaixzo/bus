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

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Headers;

/**
 * Defines the single protocol-neutral callback transport and correlation contract.
 * <p>
 * These values preserve the raw inbound request URI, headers, method, and ordered duplicate parameters. They do not
 * interpret OAuth authorization codes, OpenID Connect ID Tokens, SAML responses, Vendor fields, or any other protocol
 * artifact. The appropriate protocol or Vendor codec performs that work after correlation succeeds.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Callback {

    /**
     * Prevents instantiation of the callback contract namespace.
     */
    private Callback() {
        // No initialization required.
    }

    /**
     * Identifies the source and registered redirect URI used to initiate a browser interaction.
     *
     * @param sourceId    registered Source identifier
     * @param redirectUri registered redirect URI lexical value
     * @author Kimi Liu
     */
    public record Target(String sourceId, String redirectUri) {

        /**
         * Creates a callback target without resolving or normalizing its URI.
         *
         * @param sourceId    registered Source identifier
         * @param redirectUri registered redirect URI lexical value
         * @throws IllegalArgumentException if either value is blank
         */
        public Target {
            Assert.notBlank(sourceId, "Callback target source id must not be blank");
            Assert.notBlank(redirectUri, "Callback target redirect URI must not be blank");
        }

    }

    /**
     * Stores one-time browser interaction correlation data before a callback arrives.
     *
     * @param sourceId  registered Source identifier bound to the interaction
     * @param state     opaque one-time state value
     * @param nonce     optional protocol nonce bound to the same interaction
     * @param expiresAt absolute correlation expiration time
     * @author Kimi Liu
     */
    public record Correlation(String sourceId, String state, Optional<String> nonce, Instant expiresAt)
            implements Serializable {

        /**
         * Creates an immutable callback correlation value.
         *
         * @param sourceId  registered Source identifier
         * @param state     opaque one-time state value
         * @param nonce     optional protocol nonce
         * @param expiresAt absolute expiration time
         * @throws IllegalArgumentException if required values are blank or containers are {@code null}
         */
        public Correlation {
            Assert.notBlank(sourceId, "Callback correlation source id must not be blank");
            Assert.notBlank(state, "Callback correlation state must not be blank");
            Assert.notNull(nonce, "Callback correlation nonce container must not be null");
            Assert.notNull(expiresAt, "Callback correlation expiration must not be null");
            nonce = Optional.ofNullable(nonce.getOrNull());
        }

    }

    /**
     * Preserves one decoded callback parameter while allowing ordered duplicate names.
     *
     * @param name  original parameter name
     * @param value original decoded parameter value, which may be empty
     * @author Kimi Liu
     */
    public record Parameter(String name, String value) {

        /**
         * Creates one raw callback parameter.
         *
         * @param name  non-blank parameter name
         * @param value non-null parameter value
         * @throws IllegalArgumentException if the name is blank or the value is {@code null}
         */
        public Parameter {
            Assert.notBlank(name, "Callback parameter name must not be blank");
            Assert.notNull(value, "Callback parameter value must not be null");
        }

    }

    /**
     * Carries the immutable raw inbound HTTP callback captured by an external endpoint.
     *
     * @param sourceId   Source identifier selected by the external route
     * @param requestUri original request URI lexical value
     * @param method     actual HTTP method
     * @param headers    immutable Fabric headers
     * @param parameters decoded parameters in original order with duplicates retained
     * @author Kimi Liu
     */
    public record Inbound(String sourceId, String requestUri, Http.Method method, Headers headers,
            List<Parameter> parameters) {

        /**
         * Creates an immutable inbound callback transport snapshot without interpreting protocol fields.
         *
         * @param sourceId   Source identifier selected by the external route
         * @param requestUri original request URI lexical value
         * @param method     actual HTTP method
         * @param headers    immutable Fabric headers
         * @param parameters ordered raw decoded parameters
         * @throws IllegalArgumentException if a required value is blank, a container is {@code null}, or a parameter
         *                                  entry is {@code null}
         */
        public Inbound {
            Assert.notBlank(sourceId, "Inbound callback source id must not be blank");
            Assert.notBlank(requestUri, "Inbound callback request URI must not be blank");
            Assert.notNull(method, "Inbound callback HTTP method must not be null");
            Assert.notNull(headers, "Inbound callback headers must not be null");
            Assert.notNull(parameters, "Inbound callback parameters must not be null");
            final List<Parameter> copy = new ArrayList<>(parameters.size());
            for (Parameter parameter : parameters) {
                copy.add(Assert.notNull(parameter, "Inbound callback parameter must not be null"));
            }
            parameters = List.copyOf(copy);
        }

    }

    /**
     * Wraps an opaque callback state value generated and validated by the state codec and guard.
     *
     * @param value opaque state value
     * @author Kimi Liu
     */
    public record State(String value) {

        /**
         * Creates an opaque callback state value without decoding it.
         *
         * @param value non-blank opaque state value
         * @throws IllegalArgumentException if the value is blank
         */
        public State {
            Assert.notBlank(value, "Callback state value must not be blank");
        }

    }

}
