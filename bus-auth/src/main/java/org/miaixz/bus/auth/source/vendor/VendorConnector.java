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

import org.miaixz.bus.auth.source.SourceConnector;
import org.miaixz.bus.core.lang.Assert;

/**
 * Connects one complete third-party platform manifest and all of its variant factories to a build-scoped registry.
 * <p>
 * Implementations are discovered through the unified {@link SourceConnector} SPI and must expose a public no-argument
 * constructor. Each implementation belongs in its Vendor package as a registration-only peer of the manifest, options,
 * and adapters. The connect callback performs no platform API call, credential loading, adapter construction, or
 * runtime Roster mutation.
 * </p>
 *
 * @author Kimi Liu
 */
public non-sealed interface VendorConnector extends SourceConnector<Vendor.Id, VendorRegistry> {

    /**
     * Dispatches this connector to the Vendor registration branch.
     *
     * @param visitor unified Source connector visitor
     */
    @Override
    default void accept(final Visitor visitor) {
        Assert.notNull(visitor, "Source connector visitor must not be null").visit(this);
    }

}
