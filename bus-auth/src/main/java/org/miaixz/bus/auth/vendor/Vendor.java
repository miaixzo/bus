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

import java.io.Serializable;

import org.miaixz.bus.core.lang.Assert;

/**
 * Provides the stable identifiers and immutable presentation metadata used by registered third-party platforms.
 * <p>
 * A Vendor identifies a client-side integration source. It is neither a server-side Provider nor an industry protocol,
 * and it contains no endpoint routing or protocol operation switch.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Vendor {

    /**
     * Prevents construction of the Vendor namespace.
     */
    private Vendor() {
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
     * Creates an empty builder for an explicit built-in, custom, or combined platform module.
     *
     * @return mutable build-scoped Vendor module builder
     */
    public static VendorModule.Builder builder() {
        return VendorModule.builder();
    }

    /**
     * Creates the complete immutable directory used by external management interfaces.
     *
     * @return newly constructed Vendor directory
     */
    public static VendorDirectory directory() {
        return module().directory();
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

    /**
     * Carries management presentation data that never becomes protocol Metadata or a wire response.
     *
     * @param name        required platform display name
     * @param description optional platform description
     * @param icon        optional platform icon reference
     * @author Kimi Liu
     */
    public record Metadata(String name, String description, String icon) implements Serializable {

        /**
         * Creates platform presentation metadata with a required display name.
         *
         * @param name        platform display name
         * @param description optional platform description
         * @param icon        optional platform icon reference
         * @throws IllegalArgumentException if the display name is blank
         */
        public Metadata {
            Assert.notBlank(name, "Vendor metadata name must not be blank");
        }

    }

}
