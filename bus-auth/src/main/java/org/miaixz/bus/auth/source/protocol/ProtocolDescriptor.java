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
package org.miaixz.bus.auth.source.protocol;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Scheme.Conformance;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.source.SourceDescriptor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Exposes one exact standards-based protocol Source choice using facts owned by its ProtocolScheme.
 *
 * @author Kimi Liu
 */
public class ProtocolDescriptor implements SourceDescriptor {

    /**
     * Exact protocol scheme that owns every descriptor fact.
     */
    private final ProtocolScheme<?> scheme;

    /**
     * Creates a descriptor from the exact protocol scheme owned by a driver.
     *
     * @param scheme exact protocol scheme
     */
    public ProtocolDescriptor(final ProtocolScheme<?> scheme) {
        this.scheme = Assert.notNull(scheme, "Protocol descriptor scheme must not be null");
        Assert.notBlank(scheme.id(), "Protocol descriptor scheme id must not be blank");
        Assert.notNull(scheme.metadata(), "Protocol descriptor metadata must not be null");
        Assert.notNull(scheme.protocol(), "Protocol descriptor protocol must not be null");
        Assert.notNull(scheme.manifest(), "Protocol descriptor manifest must not be null");
        Assert.notNull(scheme.conformance(), "Protocol descriptor conformance must not be null");
        Assert.notNull(scheme.form(), "Protocol descriptor form must not be null");
    }

    /**
     * Returns the scheme identifier as the stable exact protocol selection identifier.
     *
     * @return scheme identifier
     */
    @Override
    public String id() {
        return scheme.id();
    }

    /**
     * Returns the scheme identifier written to Source type.
     *
     * @return Source type
     */
    @Override
    public String type() {
        return scheme.id();
    }

    /**
     * Returns the standards-based Protocol grouping.
     *
     * @return Protocol descriptor kind
     */
    @Override
    public Kind kind() {
        return Kind.PROTOCOL;
    }

    /**
     * Returns exact presentation metadata from the protocol scheme.
     *
     * @return scheme metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return scheme.metadata();
    }

    /**
     * Returns the exact protocol implemented by the scheme.
     *
     * @return exact protocol
     */
    @Override
    public Protocol protocol() {
        return scheme.protocol();
    }

    /**
     * Returns the exact scheme capabilities.
     *
     * @return capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return scheme.manifest();
    }

    /**
     * Returns the scheme's verified formal conformance declaration.
     *
     * @return conformance or empty
     */
    @Override
    public Optional<Conformance> conformance() {
        return scheme.conformance();
    }

    /**
     * Returns the scheme's exact management form.
     *
     * @return management form
     */
    @Override
    public Scheme.Form form() {
        return scheme.form();
    }

    /**
     * Matches Source type and protocol without interpreting options or credentials.
     *
     * @param source persisted Source candidate
     * @return whether routing identity matches this descriptor
     */
    @Override
    public boolean matches(final Source source) {
        return source != null && type().equals(source.getType()) && source.getProtocol() != null
                && protocol().name().equalsIgnoreCase(source.getProtocol());
    }

}
