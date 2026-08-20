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

import org.miaixz.bus.auth.Library;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.net.Protocol;

/**
 * Binds one protocol or Vendor scheme to the only factory capable of compiling matching Source registrations.
 * <p>
 * A driver is an immutable startup input. It does not register itself, call runtime assembly, load external data, or
 * access published Registry state.
 * </p>
 *
 * @param <O> exact immutable Source options type
 * @author Kimi Liu
 */
public interface SourceDriver<O extends Options<?>> {

    /**
     * Returns the management and capability scheme owned by this driver.
     *
     * @return exact Source scheme
     */
    Scheme<O> scheme();

    /**
     * Returns the Bus protocol owned by this driver.
     *
     * @return exact protocol or Vendor classification for the Source registration
     */
    default Protocol protocol() {
        return scheme().protocol();
    }

    /**
     * Reports whether this driver supports the actual protocol declared by a Source.
     *
     * @param protocol persisted protocol identifier
     * @return {@code true} when this driver accepts the protocol
     */
    default boolean supports(final String protocol) {
        return protocol != null && protocol().name().equalsIgnoreCase(protocol);
    }

    /**
     * Requires and narrows the exact immutable options value accepted by this driver.
     *
     * @param options candidate options value
     * @return options narrowed to this driver's exact type
     * @throws org.miaixz.bus.core.lang.exception.ValidateException if the options type does not match this driver
     */
    O require(Options<?> options);

    /**
     * Compiles one validated complete Source registration with its resolved required relationships.
     *
     * @param registration validated Source registration
     * @param provider     owning complete Provider
     * @param library      Library resolved through the owning Provider
     * @param services     externally supplied execution services
     * @return immutable executable Registry entry
     * @throws IllegalArgumentException if an argument or registration type does not match this driver
     */
    SourceWorker compile(
            Registration.Record<Source> registration,
            Provider provider,
            Library library,
            ExecutionServices services);

}
