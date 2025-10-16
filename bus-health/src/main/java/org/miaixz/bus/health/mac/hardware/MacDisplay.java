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
package org.miaixz.bus.health.mac.hardware;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.common.AbstractDisplay;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFDataRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.CoreFoundation.CFTypeRef;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

/**
 * <p>
 * MacDisplay class.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacDisplay extends AbstractDisplay {

    /**
     * Constructor for MacDisplay.
     *
     * @param edid A byte array representing a display EDID (Extended Display Identification Data).
     */
    MacDisplay(byte[] edid) {
        super(edid);
        Logger.debug("Initialized MacDisplay");
    }

    /**
     * Retrieves a list of {@link Display} objects representing the monitors connected to the system. This method
     * queries both Intel-based and Apple Silicon-based display services.
     *
     * @return A list of {@link Display} objects, each encapsulating information about a connected monitor.
     */
    public static List<Display> getDisplays() {
        List<Display> displays = new ArrayList<>();
        // Intel-based Macs
        displays.addAll(getDisplaysFromService("IODisplayConnect", "IODisplayEDID", "IOService"));
        // Apple Silicon-based Macs
        displays.addAll(getDisplaysFromService("IOPortTransportStateDisplayPort", "EDID", null));

        return displays;
    }

    /**
     * Helper method to get displays from a specific IOKit service.
     *
     * @param serviceName    The IOKit service name to search for (e.g., "IODisplayConnect").
     * @param edidKeyName    The key name for the EDID property within the service (e.g., "IODisplayEDID").
     * @param childEntryName The name of the child entry to search in, or {@code null} to search directly in the
     *                       service.
     * @return A list of {@link Display} objects found using this service.
     */
    private static List<Display> getDisplaysFromService(String serviceName, String edidKeyName, String childEntryName) {
        List<Display> displays = new ArrayList<>();

        IOIterator serviceIterator = IOKitUtil.getMatchingServices(serviceName);
        if (serviceIterator != null) {
            CFStringRef cfEdid = CFStringRef.createCFString(edidKeyName);
            IORegistryEntry sdService = serviceIterator.next();

            while (sdService != null) {
                IORegistryEntry propertySource;

                try {
                    propertySource = childEntryName == null ? sdService : sdService.getChildEntry(childEntryName);
                    if (propertySource != null) {
                        CFTypeRef edidRaw = propertySource.createCFProperty(cfEdid);
                        if (edidRaw != null) {
                            CFDataRef edid = new CFDataRef(edidRaw.getPointer());
                            try {
                                // EDID is a byte array of 128 bytes (or more)
                                int length = edid.getLength();
                                Pointer p = edid.getBytePtr();
                                displays.add(new MacDisplay(p.getByteArray(0, length)));
                            } finally {
                                edid.release();
                            }
                        }
                        if (childEntryName != null && propertySource != null) {
                            propertySource.release();
                        }
                    }
                } finally {
                    sdService.release();
                    sdService = serviceIterator.next();
                }
            }
            serviceIterator.release();
            cfEdid.release();
        }
        return displays;
    }

}
