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

import org.miaixz.bus.auth.source.SourceConnector;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Protocol;

/**
 * Connects every client-role or server-role Source driver owned by one protocol to a build-scoped registry.
 * <p>
 * Implementations are discovered through the unified {@link SourceConnector} SPI and must expose a public no-argument
 * constructor. One connector may bind multiple role-specific drivers, but every driver must use the protocol returned
 * by {@link #key()} as its primary classification. The connect callback performs declarative assembly only and must not
 * open a protocol connection, load external data, execute a Source, or access the runtime Roster.
 * </p>
 *
 * @author Kimi Liu
 */
public non-sealed interface ProtocolConnector extends SourceConnector<Protocol, ProtocolRegistry> {

    /**
     * Dispatches this connector to the protocol registration branch.
     *
     * @param visitor unified Source connector visitor
     */
    @Override
    default void accept(final Visitor visitor) {
        Assert.notNull(visitor, "Source connector visitor must not be null").visit(this);
    }

}
