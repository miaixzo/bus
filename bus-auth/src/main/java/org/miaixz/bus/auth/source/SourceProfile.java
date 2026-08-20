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
import org.miaixz.bus.auth.Form;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the immutable management and compilation contract for one generic protocol Source type.
 * <p>
 * A source profile identifies the exact settings class, industry protocol, implemented capability manifest, formal
 * conformance basis, management form, and optional non-sensitive defaults. Vendor profiles remain in the Vendor layer
 * and are neither wrapped nor expanded by this contract.
 * </p>
 *
 * @param <S> immutable protocol-specific Source settings type
 * @author Kimi Liu
 */
public interface SourceProfile<S extends SourceSettings> extends org.miaixz.bus.core.Provider<Protocol> {

    /**
     * Returns the stable profile identifier used by registration type lookup.
     *
     * @return non-blank stable profile identifier
     */
    String id();

    /**
     * Returns the closed protocol or Vendor category implemented by this Source profile.
     *
     * @return Source category
     */
    @Override
    Protocol type();

    /**
     * Returns the exact immutable settings class accepted during registration decoding.
     *
     * @return protocol-specific settings class
     */
    Class<S> settingsType();

    /**
     * Returns only the Source capabilities actually implemented by the runtime.
     *
     * @return immutable implemented-capability manifest
     */
    Capability.Manifest manifest();

    /**
     * Returns the formal standard basis implemented by this generic protocol Source profile.
     *
     * @return formal protocol conformance declaration, or empty only when no formal standard applies
     */
    Optional<Conformance> conformance();

    /**
     * Returns the immutable external management form for this profile's settings.
     *
     * @return management form whose keys retain formal protocol names
     */
    Form form();

    /**
     * Returns optional immutable non-sensitive default settings.
     *
     * @return profile defaults, or empty when deployment input is required
     */
    Optional<S> defaults();

}
