/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
