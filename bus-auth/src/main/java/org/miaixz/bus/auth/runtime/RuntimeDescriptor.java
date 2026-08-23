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
package org.miaixz.bus.auth.runtime;

import java.util.List;

import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.source.SourceDescriptor;
import org.miaixz.bus.auth.source.SourceLookup;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Exposes the exact Source choices assembled into one runtime through a single implementation-neutral discovery
 * surface.
 * <p>
 * This projection delegates identity and reverse routing to the runtime's shared frozen {@link SourceLookup}. It does
 * not read Roster state, create Options, expose implementation factories, compile workers, or execute capabilities.
 * </p>
 *
 * @author Kimi Liu
 */
public class RuntimeDescriptor {

    /**
     * Shared immutable Source lookup used by validation and compilation.
     */
    private final SourceLookup sourceLookup;

    /**
     * Creates a read-only Source discovery projection over one frozen lookup.
     *
     * @param sourceLookup runtime's shared Source lookup
     */
    public RuntimeDescriptor(final SourceLookup sourceLookup) {
        this.sourceLookup = Assert.notNull(sourceLookup, "Runtime descriptor Source lookup must not be null");
    }

    /**
     * Returns all exact Source choices in deterministic module order.
     *
     * @return immutable Source descriptor list
     */
    public List<SourceDescriptor> sources() {
        return sourceLookup.descriptors();
    }

    /**
     * Finds one exact Source choice by its stable descriptor identifier.
     *
     * @param id stable descriptor identifier
     * @return matching descriptor or empty
     */
    public Optional<SourceDescriptor> source(final String id) {
        return sourceLookup.descriptor(id);
    }

    /**
     * Resolves the unique descriptor represented by a persisted Source route.
     *
     * @param source persisted Source
     * @return matching descriptor or empty
     */
    public Optional<SourceDescriptor> source(final Source source) {
        return sourceLookup.descriptor(Assert.notNull(source, "Runtime descriptor Source must not be null"));
    }

    /**
     * Returns exact Source choices backed by one Source driver type.
     *
     * @param type stable Source type
     * @return immutable matching descriptor list
     */
    public List<SourceDescriptor> sources(final String type) {
        Assert.notBlank(type, "Runtime descriptor Source type must not be blank");
        return sourceLookup.descriptors().stream().filter(descriptor -> type.equals(descriptor.type())).toList();
    }

    /**
     * Returns exact Source choices in one explicit management group.
     *
     * @param kind Source descriptor kind
     * @return immutable matching descriptor list
     */
    public List<SourceDescriptor> sources(final SourceDescriptor.Kind kind) {
        Assert.notNull(kind, "Runtime descriptor Source kind must not be null");
        return sourceLookup.descriptors().stream().filter(descriptor -> kind == descriptor.kind()).toList();
    }

}
