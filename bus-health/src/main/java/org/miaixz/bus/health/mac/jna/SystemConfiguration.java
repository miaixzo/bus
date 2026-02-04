/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.CoreFoundation.CFTypeRef;

/**
 * Allows applications to access a device’s network configuration settings. Determine the reachability of the device,
 * such as whether Wi-Fi or cell connectivity are active.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SystemConfiguration extends Library {

    /**
     * Singleton instance of the SystemConfiguration library.
     */
    SystemConfiguration INSTANCE = Native.load("SystemConfiguration", SystemConfiguration.class);

    /**
     * Returns an array of all network interfaces known to the System Configuration framework.
     *
     * @return A CFArrayRef containing SCNetworkInterfaceRef objects, each representing a network interface. This
     *         reference must be released with {@link com.sun.jna.platform.mac.CoreFoundation#CFRelease} to avoid
     *         leaking references.
     */
    CFArrayRef SCNetworkInterfaceCopyAll();

    /**
     * Returns the BSD name of a network interface.
     *
     * @param netint The network interface.
     * @return A CFStringRef containing the BSD name of the network interface. This reference must be released with
     *         {@link com.sun.jna.platform.mac.CoreFoundation#CFRelease} to avoid leaking references.
     */
    CFStringRef SCNetworkInterfaceGetBSDName(SCNetworkInterfaceRef netint);

    /**
     * Returns the localized display name of a network interface.
     *
     * @param netint The network interface.
     * @return A CFStringRef containing the localized display name of the network interface. This reference must be
     *         released with {@link com.sun.jna.platform.mac.CoreFoundation#CFRelease} to avoid leaking references.
     */
    CFStringRef SCNetworkInterfaceGetLocalizedDisplayName(SCNetworkInterfaceRef netint);

    /**
     * A reference to an SCNetworkInterface object.
     */
    class SCNetworkInterfaceRef extends CFTypeRef {

        /**
         * Constructs an SCNetworkInterfaceRef with a null pointer.
         */
        public SCNetworkInterfaceRef() {
            super();
        }

        /**
         * Constructs an SCNetworkInterfaceRef with the given pointer.
         *
         * @param p The pointer to the SCNetworkInterface object.
         */
        public SCNetworkInterfaceRef(Pointer p) {
            super(p);
        }
    }

}
