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
package org.miaixz.bus.auth.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the exact formal standard version, profile, and normative citations implemented by one protocol profile.
 * <p>
 * This catalog metadata is not protocol Discovery or wire content and does not repeat Capability declarations.
 * Vendor-specific behavior without a formal standard must use VendorDeviation rather than fabricate Conformance.
 * </p>
 *
 * @param protocol  existing Bus protocol identifier
 * @param version   formal protocol version
 * @param citations non-empty set of normative standard locations
 * @param profile   exact formal standard profile name
 * @author Kimi Liu
 */
public record Conformance(Protocol protocol, Version version, Set<Citation> citations, String profile) {

    /**
     * Validates and freezes formal conformance metadata.
     *
     * @throws IllegalArgumentException if a component or citation is {@code null} or text is blank
     * @throws ValidateException        if no normative citation is supplied
     */
    public Conformance {
        Assert.notNull(protocol, "Conformance protocol must not be null");
        Assert.notNull(version, "Conformance version must not be null");
        Assert.notNull(citations, "Conformance citations must not be null");
        citations = Set.copyOf(citations);
        if (citations.isEmpty()) {
            throw new ValidateException("Conformance must cite at least one formal standard section");
        }
        for (Citation citation : citations) {
            Assert.notNull(citation, "Conformance citation must not be null");
        }
        Assert.notBlank(profile, "Conformance standard profile must not be blank");
    }

    /**
     * Identifies one normative section of a formal specification without copying specification content into runtime.
     *
     * @param standardUrl absolute stable HTTP(S) specification URL without query or fragment
     * @param section     exact normative section identifier
     * @author Kimi Liu
     */
    public record Citation(String standardUrl, String section) {

        /**
         * Validates the stable formal-standard location and section identifier.
         *
         * @throws IllegalArgumentException if text is blank
         * @throws ValidateException        if the URL is not an absolute credential-free HTTP(S) specification URL
         */
        public Citation {
            Assert.notBlank(standardUrl, "Conformance standard URL must not be blank");
            Assert.notBlank(section, "Conformance standard section must not be blank");
            try {
                final URI uri = new URI(standardUrl);
                if (!uri.isAbsolute() || uri.getHost() == null
                        || !(Protocol.HTTP.name.equalsIgnoreCase(uri.getScheme())
                                || Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()))
                        || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                    throw new ValidateException(
                            "Conformance standard URL must be an absolute HTTP(S) URL without credentials, query, or fragment");
                }
            } catch (URISyntaxException cause) {
                throw new ValidateException("Conformance standard URL is malformed", cause);
            }
        }

    }

}
