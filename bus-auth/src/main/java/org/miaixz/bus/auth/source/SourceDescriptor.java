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
package org.miaixz.bus.auth.source;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Scheme.Conformance;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Describes one exact Source choice exposed to management and configuration callers.
 * <p>
 * A descriptor contains discovery facts only. It does not resolve credentials, create options, compile workers, read
 * Roster state, or perform network operations.
 * </p>
 *
 * @author Kimi Liu
 */
public interface SourceDescriptor {

    /**
     * Returns the globally unique and stable management selection identifier.
     *
     * @return stable descriptor identifier
     */
    String id();

    /**
     * Returns the Source type written to persistence and used for driver lookup.
     *
     * @return stable Source type
     */
    String type();

    /**
     * Returns the management grouping of this exact selection.
     *
     * @return descriptor kind
     */
    Kind kind();

    /**
     * Returns presentation metadata for this exact selection.
     *
     * @return immutable presentation metadata
     */
    Scheme.Metadata metadata();

    /**
     * Returns the actual protocol written to the Source configuration.
     *
     * @return exact protocol
     */
    Protocol protocol();

    /**
     * Returns only the capabilities implemented by this exact selection.
     *
     * @return immutable capability manifest
     */
    Capability.Manifest manifest();

    /**
     * Returns the verified formal standard basis of this selection, when one exists.
     *
     * @return conformance declaration or empty
     */
    Optional<Conformance> conformance();

    /**
     * Returns the exact external management form for this selection.
     *
     * @return immutable management form
     */
    Scheme.Form form();

    /**
     * Tests whether a persisted Source is represented by this descriptor without reading secret material.
     *
     * @param source persisted Source candidate
     * @return whether the Source follows this descriptor's routing identity
     */
    boolean matches(Source source);

    /**
     * Classifies an exact Source selection without relying on package names or identifier prefixes.
     *
     * @author Kimi Liu
     */
    enum Kind {

        /**
         * Standards-based protocol implementation supplied by the Protocol branch.
         */
        PROTOCOL,

        /**
         * Explicit third-party platform variant supplied by the Vendor branch.
         */
        VENDOR

    }

}
