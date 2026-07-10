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
package org.miaixz.bus.health.mac.hardware;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFBooleanRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDataRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFIndex;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.CoreFoundation.CFTypeRef;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.DisplayInfo;
import org.miaixz.bus.health.builtin.hardware.common.AbstractDisplay;
import org.miaixz.bus.health.mac.jna.CoreGraphics;
import org.miaixz.bus.health.mac.jna.ObjCRuntime;
import org.miaixz.bus.logger.Logger;

/**
 * <p>
 * MacDisplay class.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class MacDisplay extends AbstractDisplay {

    /**
     * The CoreFoundation instance.
     */
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;

    /**
     * The kCFNumberSInt64Type value used by CFNumberGetValue.
     */
    private static final CFIndex K_CF_NUMBER_SINT64 = new CFIndex(4);

    /**
     * Constructor for MacDisplay.
     *
     * @param edid A byte array representing a display EDID (Extended Display Identification Data).
     */
    MacDisplay(byte[] edid) {
        super(edid);
        Logger.debug(false, "Health", "Initialized MacDisplay");
    }

    /**
     * Constructor for MacDisplay from synthetic display information.
     *
     * @param displayInfo The synthesized display information.
     */
    MacDisplay(DisplayInfo displayInfo) {
        super(displayInfo);
        Logger.debug(false, "Health", "Initialized MacDisplay (synthetic)");
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
        // Apple Silicon built-in panel without a physical EDID.
        displays.addAll(getAppleSiliconBuiltInDisplay());

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
                IORegistryEntry propertySource = null;

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
                        if (childEntryName != null) {
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

    /**
     * Discovers the Apple Silicon built-in display by matching the stable {@code IOMobileFramebuffer} base class.
     * External monitors are skipped because they are enumerated through {@code IOPortTransportStateDisplayPort}; only
     * the built-in panel without a physical EDID is synthesized from {@code DisplayAttributes}.
     *
     * @return A list containing the built-in display, or an empty list if it is not found.
     */
    private static List<Display> getAppleSiliconBuiltInDisplay() {
        List<Display> displays = new ArrayList<>();
        IOIterator iter = IOKitUtil.getMatchingServices("IOMobileFramebuffer");
        if (iter == null) {
            return displays;
        }
        CFStringRef cfExternal = CFStringRef.createCFString("external");
        CFStringRef cfAttrs = CFStringRef.createCFString("DisplayAttributes");
        try {
            IORegistryEntry fb = iter.next();
            while (fb != null) {
                try {
                    addBuiltInDisplay(fb, cfExternal, cfAttrs, displays);
                } finally {
                    fb.release();
                }
                fb = iter.next();
            }
        } finally {
            iter.release();
            cfExternal.release();
            cfAttrs.release();
        }
        return displays;
    }

    /**
     * Adds a synthesized display for an Apple Silicon built-in panel.
     *
     * @param fb         The framebuffer registry entry.
     * @param cfExternal The CoreFoundation key for the external display flag.
     * @param cfAttrs    The CoreFoundation key for display attributes.
     * @param displays   The target display list.
     */
    private static void addBuiltInDisplay(
            IORegistryEntry fb,
            CFStringRef cfExternal,
            CFStringRef cfAttrs,
            List<Display> displays) {
        CFTypeRef externalRef = fb.createCFProperty(cfExternal);
        if (externalRef != null) {
            try {
                if (new CFBooleanRef(externalRef.getPointer()).booleanValue()) {
                    return;
                }
            } finally {
                externalRef.release();
            }
        }
        CFTypeRef attrsRaw = fb.createCFProperty(cfAttrs);
        if (attrsRaw == null) {
            IORegistryEntry parent = fb.getParentEntry("IODeviceTree");
            if (parent != null) {
                try {
                    attrsRaw = parent.createCFProperty(cfAttrs);
                } finally {
                    parent.release();
                }
            }
        }
        if (attrsRaw == null) {
            return;
        }
        try {
            DisplayInfo info = synthesize(fb, new CFDictionaryRef(attrsRaw.getPointer()));
            if (info != null) {
                displays.add(new MacDisplay(info));
            }
        } finally {
            attrsRaw.release();
        }
    }

    /**
     * Synthesizes display information from an Apple Silicon display attributes dictionary.
     *
     * @param fb    The framebuffer registry entry.
     * @param attrs The display attributes dictionary.
     * @return Synthesized display information, or {@code null} if required attributes are unavailable.
     */
    private static DisplayInfo synthesize(IORegistryEntry fb, CFDictionaryRef attrs) {
        CFDictionaryRef product = cfDictGetDictionary(attrs, "ProductAttributes");
        if (product == null) {
            return null;
        }
        Long legacyMfg = cfDictGetLong(product, "LegacyManufacturerID");
        Long week = cfDictGetLong(product, "WeekOfManufacture");
        Long year = cfDictGetLong(product, "YearOfManufacture");
        String model = cfDictGetString(product, "ProductName");
        String serial = cfDictGetString(product, "AlphanumericSerialNumber");

        Long displayWidth = cfRegistryEntryGetLong(fb, "DisplayWidth");
        Long displayHeight = cfRegistryEntryGetLong(fb, "DisplayHeight");

        String fallbackName = null;
        String ioNameMatched = cfRegistryEntryGetString(fb, "IONameMatched");
        if (ioNameMatched != null) {
            String shortName = ioNameMatched.contains(",") ? ioNameMatched.substring(0, ioNameMatched.indexOf(','))
                    : ioNameMatched;
            fallbackName = shortName + " (Built-in Display)";
        }

        Integer cgModel = null;
        Integer cgSerial = null;
        Double widthMm = null;
        Double heightMm = null;
        String displayName = null;
        int builtInId = findBuiltInDisplayId();
        if (builtInId >= 0) {
            try {
                CoreGraphics cg = CoreGraphics.INSTANCE;
                cgModel = cg.CGDisplayModelNumber(builtInId);
                cgSerial = cg.CGDisplaySerialNumber(builtInId);
                CoreGraphics.CGSizeByValue size = cg.CGDisplayScreenSize(builtInId);
                widthMm = size.width;
                heightMm = size.height;
            } catch (Exception e) {
                Logger.debug(
                        false,
                        "Health",
                        "Failed to get built-in display CoreGraphics properties: {}",
                        e.getMessage());
            }
            displayName = getLocalizedDisplayName(builtInId);
        }
        return Builder.synthesizeDisplayInfo(
                legacyMfg,
                cgModel,
                cgSerial,
                week == null ? null : week.intValue(),
                year == null ? null : year.intValue(),
                model,
                serial,
                displayWidth,
                displayHeight,
                fallbackName,
                widthMm,
                heightMm,
                displayName);
    }

    /**
     * Finds the CoreGraphics display identifier for the built-in display.
     *
     * @return The display identifier, or {@code -1} if no built-in display is active.
     */
    private static int findBuiltInDisplayId() {
        CoreGraphics cg = CoreGraphics.INSTANCE;
        IntByReference count = new IntByReference();
        if (cg.CGGetActiveDisplayList(0, null, count) != 0 || count.getValue() == 0) {
            return -1;
        }
        int[] displayIds = new int[count.getValue()];
        if (cg.CGGetActiveDisplayList(displayIds.length, displayIds, count) != 0) {
            return -1;
        }
        for (int id : displayIds) {
            if (cg.CGDisplayIsBuiltin(id) != 0) {
                return id;
            }
        }
        return -1;
    }

    /**
     * Gets the localized AppKit display name for a CoreGraphics display identifier.
     *
     * @param targetDisplayId The target display identifier.
     * @return The localized display name, or {@code null} if unavailable.
     */
    private static String getLocalizedDisplayName(int targetDisplayId) {
        try {
            ObjCRuntime objc = ObjCRuntime.INSTANCE;
            Pointer pool = objc.objc_autoreleasePoolPush();
            try {
                return queryLocalizedDisplayName(objc, targetDisplayId);
            } finally {
                objc.objc_autoreleasePoolPop(pool);
            }
        } catch (Exception e) {
            Logger.debug(false, "Health", "Failed to get localized display name: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Queries NSScreen for the localized display name.
     *
     * @param objc            The Objective-C runtime binding.
     * @param targetDisplayId The target display identifier.
     * @return The localized display name, or {@code null} if unavailable.
     */
    private static String queryLocalizedDisplayName(ObjCRuntime objc, int targetDisplayId) {
        Pointer nsScreenClass = objc.objc_getClass("NSScreen");
        if (nsScreenClass == null) {
            return null;
        }
        Pointer selScreens = objc.sel_registerName("screens");
        Pointer selCount = objc.sel_registerName("count");
        Pointer selObjectAt = objc.sel_registerName("objectAtIndex:");
        Pointer selDeviceDesc = objc.sel_registerName("deviceDescription");
        Pointer selLocalizedName = objc.sel_registerName("localizedName");

        Pointer screensArray = objc.objc_msgSend(nsScreenClass, selScreens);
        if (screensArray == null) {
            return null;
        }
        long count = Pointer.nativeValue(objc.objc_msgSend(screensArray, selCount));
        CFStringRef cfKey = CFStringRef.createCFString("NSScreenNumber");
        try {
            for (long i = 0; i < count; i++) {
                Pointer screen = objc.objc_msgSend(screensArray, selObjectAt, i);
                if (screen == null) {
                    continue;
                }
                Pointer deviceDesc = objc.objc_msgSend(screen, selDeviceDesc);
                if (deviceDesc == null) {
                    continue;
                }
                Pointer cfNum = CF.CFDictionaryGetValue(new CFDictionaryRef(deviceDesc), cfKey);
                if (cfNum == null) {
                    continue;
                }
                LongByReference outId = new LongByReference();
                if (CF.CFNumberGetValue(new CFNumberRef(cfNum), K_CF_NUMBER_SINT64, outId) != 0
                        && (int) outId.getValue() == targetDisplayId) {
                    Pointer nsName = objc.objc_msgSend(screen, selLocalizedName);
                    if (nsName != null) {
                        return new CFStringRef(nsName).stringValue();
                    }
                }
            }
        } finally {
            cfKey.release();
        }
        return null;
    }

    /**
     * Reads a long value from an IORegistryEntry property.
     *
     * @param entry The registry entry.
     * @param key   The property key.
     * @return The long value, or {@code null} if unavailable.
     */
    private static Long cfRegistryEntryGetLong(IORegistryEntry entry, String key) {
        CFStringRef k = CFStringRef.createCFString(key);
        try {
            CFTypeRef ref = entry.createCFProperty(k);
            if (ref == null) {
                return null;
            }
            try {
                CFNumberRef num = new CFNumberRef(ref.getPointer());
                LongByReference out = new LongByReference();
                CF.CFNumberGetValue(num, K_CF_NUMBER_SINT64, out);
                return out.getValue();
            } finally {
                ref.release();
            }
        } finally {
            k.release();
        }
    }

    /**
     * Reads a string value from an IORegistryEntry property.
     *
     * @param entry The registry entry.
     * @param key   The property key.
     * @return The string value, or {@code null} if unavailable.
     */
    private static String cfRegistryEntryGetString(IORegistryEntry entry, String key) {
        CFStringRef k = CFStringRef.createCFString(key);
        try {
            CFTypeRef ref = entry.createCFProperty(k);
            if (ref == null) {
                return null;
            }
            try {
                return new CFStringRef(ref.getPointer()).stringValue();
            } finally {
                ref.release();
            }
        } finally {
            k.release();
        }
    }

    /**
     * Reads a dictionary value from a CoreFoundation dictionary. The returned value is borrowed and must not be
     * released.
     *
     * @param dict The source dictionary.
     * @param key  The dictionary key.
     * @return The dictionary value, or {@code null} if unavailable.
     */
    private static CFDictionaryRef cfDictGetDictionary(CFDictionaryRef dict, String key) {
        CFStringRef k = CFStringRef.createCFString(key);
        try {
            Pointer value = CF.CFDictionaryGetValue(dict, k);
            return value == null ? null : new CFDictionaryRef(value);
        } finally {
            k.release();
        }
    }

    /**
     * Reads a string value from a CoreFoundation dictionary. The returned value is borrowed and must not be released.
     *
     * @param dict The source dictionary.
     * @param key  The dictionary key.
     * @return The string value, or {@code null} if unavailable.
     */
    private static String cfDictGetString(CFDictionaryRef dict, String key) {
        CFStringRef k = CFStringRef.createCFString(key);
        try {
            Pointer value = CF.CFDictionaryGetValue(dict, k);
            return value == null ? null : new CFStringRef(value).stringValue();
        } finally {
            k.release();
        }
    }

    /**
     * Reads a long value from a CoreFoundation dictionary. The returned value is borrowed and must not be released.
     *
     * @param dict The source dictionary.
     * @param key  The dictionary key.
     * @return The long value, or {@code null} if unavailable.
     */
    private static Long cfDictGetLong(CFDictionaryRef dict, String key) {
        CFStringRef k = CFStringRef.createCFString(key);
        try {
            Pointer value = CF.CFDictionaryGetValue(dict, k);
            if (value == null) {
                return null;
            }
            CFNumberRef num = new CFNumberRef(value);
            LongByReference out = new LongByReference();
            CF.CFNumberGetValue(num, K_CF_NUMBER_SINT64, out);
            return out.getValue();
        } finally {
            k.release();
        }
    }

}
