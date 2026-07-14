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
package org.miaixz.bus.fabric.protocol;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;

/**
 * Immutable protocol and address itinerary.
 *
 * @param protocol bus-core protocol
 * @param address  itinerary address
 * @author Kimi Liu
 * @since Java 21+
 */
public record Itinerary(Protocol protocol, Address address) {

    /**
     * Creates an itinerary.
     *
     * @param protocol bus-core protocol
     * @param address  itinerary address
     */
    public Itinerary {
        Assert.isTrue(
                (protocol == null) == (address == null),
                () -> new ValidateException("Protocol and address must both be present or both be empty"));
        if (protocol != null) {
            Assert.isTrue(
                    protocol == address.protocol(),
                    () -> new ProtocolException("Protocol does not match address scheme"));
        }
    }

    /**
     * Creates an itinerary.
     *
     * @param protocol bus-core protocol
     * @param address  itinerary address
     * @return itinerary
     */
    public static Itinerary of(final Protocol protocol, final Address address) {
        return new Itinerary(protocol, address);
    }

    /**
     * Returns the shared empty itinerary.
     *
     * @return empty itinerary
     */
    public static Itinerary empty() {
        return Instances.get(Itinerary.class.getName() + ".empty", () -> new Itinerary(null, null));
    }

    /**
     * Returns whether the itinerary is secure.
     *
     * @return true when secure
     */
    public boolean secure() {
        return address != null && address.secure();
    }

}
