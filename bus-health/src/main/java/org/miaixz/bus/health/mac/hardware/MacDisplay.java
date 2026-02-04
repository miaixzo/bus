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
