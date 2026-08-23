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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Provides the stable identifiers and immutable presentation metadata used by registered third-party platforms.
 * <p>
 * A Vendor identifies a client-side integration source. It is neither a server-side Provider nor an industry protocol,
 * and it contains no endpoint routing or protocol operation switch.
 * </p>
 *
 * @author Kimi Liu
 */
public class Vendor {

    /**
     * Creates a Vendor constant holder with no retained state.
     */
    public Vendor() {
        // No initialization required.
    }

    /**
     * Creates a complete immutable Vendor integration set.
     *
     * @return newly constructed Vendor module
     */
    public static VendorModule module() {
        return builder().builtins().build();
    }

    /**
     * Creates an empty builder for discovered, explicit, or combined platform registrations.
     *
     * @return mutable build-scoped Vendor module builder
     */
    public static VendorModule.Builder builder() {
        return VendorModule.builder();
    }

    /**
     * Creates the complete immutable locator used by external management interfaces.
     *
     * @return newly constructed Vendor locator
     */
    public static VendorLocator locator() {
        return module().locator();
    }

    /**
     * Carries one short-lived client-side Vendor configuration command.
     * <p>
     * The credential lease contains caller-supplied plaintext only until {@link VendorConfigurer#configure} hands the
     * material to the project {@code VendorCredentialWriter}. This command is never a persistent Options value and must
     * not be logged or serialized.
     * </p>
     *
     * @param vendor     exact Vendor platform identifier
     * @param variant    exact platform variant identifier
     * @param clientId   public client identifier issued by the external platform
     * @param credential short-lived plaintext client secret or private key lease
     * @param callback   exact registered callback, empty only for direct variants
     * @param scopes     requested scopes, or empty to use manifest defaults
     * @param parameters immutable manifest-form parameter values
     * @author Kimi Liu
     */
    public record Configuration(Id vendor, Variant variant, String clientId, SecretLease credential,
            Optional<String> callback, List<String> scopes, JsonValue.ObjectValue parameters) {

        /**
         * Validates and freezes one short-lived Vendor configuration command.
         */
        public Configuration {
            vendor = Assert.notNull(vendor, "Vendor configuration platform must not be null");
            variant = Assert.notNull(variant, "Vendor configuration variant must not be null");
            clientId = Assert.notBlank(clientId, "Vendor configuration client id must not be blank");
            credential = Assert.notNull(credential, "Vendor configuration credential lease must not be null");
            callback = Assert.notNull(callback, "Vendor configuration callback container must not be null");
            callback = Optional.ofNullable(callback.getOrNull());
            if (callback.isPresent()) {
                Assert.notBlank(callback.getOrNull(), "Vendor configuration callback must not be blank");
            }
            Assert.notNull(scopes, "Vendor configuration scopes must not be null");
            scopes = List.copyOf(scopes);
            parameters = new JsonValue.ObjectValue(
                    Assert.notNull(parameters, "Vendor configuration parameters must not be null").values());
        }

        /**
         * Creates the ordinary client configuration shape used by fixed Vendor platforms.
         *
         * @param vendor     exact Vendor platform identifier
         * @param variant    exact platform variant identifier
         * @param clientId   public client identifier
         * @param credential short-lived plaintext credential lease
         * @param callback   exact registered callback
         * @return configuration using manifest default scopes and no platform parameters
         */
        public static Configuration client(
                final Id vendor,
                final Variant variant,
                final String clientId,
                final SecretLease credential,
                final String callback) {
            return new Configuration(vendor, variant, clientId, credential, Optional.ofNullable(callback), List.of(),
                    new JsonValue.ObjectValue(Map.of()));
        }

    }

    /**
     * Identifies one third-party platform independently of its products or protocol variants.
     *
     * @param value stable non-blank platform identifier
     * @author Kimi Liu
     */
    public record Id(String value) implements Serializable {

        /**
         * Creates a stable platform identifier.
         *
         * @param value stable non-blank platform identifier
         * @throws IllegalArgumentException if the identifier is blank
         */
        public Id {
            Assert.notBlank(value, "Vendor id must not be blank");
        }

    }

    /**
     * Identifies one independently configured product or authentication flow of a platform.
     *
     * @param value stable non-blank variant identifier
     * @author Kimi Liu
     */
    public record Variant(String value) implements Serializable {

        /**
         * Creates a stable platform variant identifier.
         *
         * @param value stable non-blank variant identifier
         * @throws IllegalArgumentException if the identifier is blank
         */
        public Variant {
            Assert.notBlank(value, "Vendor variant must not be blank");
        }

    }

}
