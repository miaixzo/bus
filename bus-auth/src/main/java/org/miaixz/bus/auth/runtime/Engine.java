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

import org.miaixz.bus.auth.Policy;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.protocol.Handler;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.core.Lifecycle;

/**
 * Common access contract for an assembled authentication runtime.
 *
 * @author Kimi Liu
 */
public interface Engine extends Lifecycle, AutoCloseable {

    /**
     * @return immutable-access vendor definition registry
     */
    Registry<VendorDefinition> vendors();

    /**
     * @return protocol-neutral provider registry
     */
    Registry<Provider> providers();

    /**
     * @return server protocol handler registry
     */
    Registry<Handler<?, ?>> handlers();

    /**
     * @return shared authentication policy registry
     */
    Registry<Policy> policies();

    /**
     * Closes this runtime and its owned closeable components.
     */
    @Override
    void close();

}
