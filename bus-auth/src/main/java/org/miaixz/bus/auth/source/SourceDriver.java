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
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Binds one protocol or Vendor profile to the only factory capable of compiling matching Source registrations.
 * <p>
 * A driver is an immutable startup input. It does not register itself, call runtime assembly, load external data, or
 * access published Registry state.
 * </p>
 *
 * @param <S> exact immutable Source settings type
 * @author Kimi Liu
 */
public interface SourceDriver<S extends SourceSettings> {

    /**
     * Returns the management and capability profile owned by this driver.
     *
     * @return exact Source profile
     */
    SourceProfile<S> profile();

    /**
     * Returns the Bus protocol type owned by this driver.
     *
     * @return exact protocol or Vendor classification for the Source registration
     */
    default Protocol type() {
        return profile().type();
    }

    /**
     * Reports whether this driver supports the actual protocol declared by a Source.
     *
     * @param protocol persisted protocol identifier
     * @return {@code true} when this driver accepts the protocol
     */
    default boolean supports(final String protocol) {
        return protocol != null && type().name().equalsIgnoreCase(protocol);
    }

    /**
     * Decodes the raw JSON settings stored by one Source entity.
     *
     * @param source complete Source entity
     * @return exact immutable settings value owned by this driver
     * @throws ValidateException        if the JSON cannot be decoded as the profile settings type
     * @throws IllegalArgumentException if the Source or its settings are {@code null} or blank
     */
    default S decode(final Source source) {
        final Source checked = Assert.notNull(source, "Source must not be null");
        final String settings = Assert.notBlank(checked.getSettings(), "Source settings must not be blank");
        final SourceProfile<S> profile = Assert.notNull(profile(), "Source profile must not be null");
        final Class<S> settingsType = Assert
                .notNull(profile.settingsType(), "Source profile settings type must not be null");
        try {
            return Assert.notNull(JsonKit.toPojo(settings, settingsType), "Decoded Source settings must not be null");
        } catch (RuntimeException cause) {
            throw new ValidateException("Source settings cannot be decoded for profile " + profile.id(), cause);
        }
    }

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
    RuntimeProvider compile(
            Registration.Record<Source> registration,
            Provider provider,
            Library library,
            ExecutionServices services);

}
