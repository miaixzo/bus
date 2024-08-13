/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.mac.driver;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.builtin.software.OSDesktopWindow;
import org.miaixz.bus.health.mac.CFKit;
import org.miaixz.bus.health.mac.jna.CoreGraphics;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.*;

/**
 * Utility to query desktop windows
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class WindowInfo {

    /**
     * Gets windows on the operating system's GUI desktop.
     *
     * @param visibleOnly Whether to restrict the list to only windows visible to the user.
     * @return A list of {@link OSDesktopWindow} objects representing the desktop windows.
     */
    public static List<OSDesktopWindow> queryDesktopWindows(boolean visibleOnly) {
        CFArrayRef windowInfo = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(visibleOnly
                ? CoreGraphics.kCGWindowListOptionOnScreenOnly | CoreGraphics.kCGWindowListExcludeDesktopElements
                : CoreGraphics.kCGWindowListOptionAll, CoreGraphics.kCGNullWindowID);
        int numWindows = windowInfo.getCount();
        // Prepare a list to return
        List<OSDesktopWindow> windowList = new ArrayList<>();
        // Set up keys for dictionary lookup
        CFStringRef kCGWindowIsOnscreen = CFStringRef.createCFString("kCGWindowIsOnscreen");
        CFStringRef kCGWindowNumber = CFStringRef.createCFString("kCGWindowNumber");
        CFStringRef kCGWindowOwnerPID = CFStringRef.createCFString("kCGWindowOwnerPID");
        CFStringRef kCGWindowLayer = CFStringRef.createCFString("kCGWindowLayer");
        CFStringRef kCGWindowBounds = CFStringRef.createCFString("kCGWindowBounds");
        CFStringRef kCGWindowName = CFStringRef.createCFString("kCGWindowName");
        CFStringRef kCGWindowOwnerName = CFStringRef.createCFString("kCGWindowOwnerName");
        try {
            // Populate the list
            for (int i = 0; i < numWindows; i++) {
                // For each array element, get the dictionary
                Pointer result = windowInfo.getValueAtIndex(i);
                CFDictionaryRef windowRef = new CFDictionaryRef(result);
                // Now get information from the dictionary.
                result = windowRef.getValue(kCGWindowIsOnscreen); // Optional key, check for null
                boolean visible = result == null || new CFBooleanRef(result).booleanValue();
                if (!visibleOnly || visible) {
                    result = windowRef.getValue(kCGWindowNumber); // kCFNumberSInt64Type
                    long windowNumber = new CFNumberRef(result).longValue();

                    result = windowRef.getValue(kCGWindowOwnerPID); // kCFNumberSInt64Type
                    long windowOwnerPID = new CFNumberRef(result).longValue();

                    result = windowRef.getValue(kCGWindowLayer); // kCFNumberIntType
                    int windowLayer = new CFNumberRef(result).intValue();

                    result = windowRef.getValue(kCGWindowBounds);
                    try (CoreGraphics.CGRect rect = new CoreGraphics.CGRect()) {
                        CoreGraphics.INSTANCE.CGRectMakeWithDictionaryRepresentation(new CFDictionaryRef(result), rect);
                        Rectangle windowBounds = new Rectangle(Formats.roundToInt(rect.origin.x),
                                Formats.roundToInt(rect.origin.y), Formats.roundToInt(rect.size.width),
                                Formats.roundToInt(rect.size.height));
                        // Note: the Quartz name returned by this field is rarely used
                        result = windowRef.getValue(kCGWindowName); // Optional key, check for null
                        String windowName = CFKit.cfPointerToString(result, false);
                        // This is the program running the window, use as name if name blank or add in
                        // parenthesis
                        result = windowRef.getValue(kCGWindowOwnerName); // Optional key, check for null
                        String windowOwnerName = CFKit.cfPointerToString(result, false);
                        if (windowName.isEmpty()) {
                            windowName = windowOwnerName;
                        } else {
                            windowName = windowName + Symbol.PARENTHESE_LEFT + windowOwnerName
                                    + Symbol.PARENTHESE_RIGHT;
                        }

                        windowList.add(new OSDesktopWindow(windowNumber, windowName, windowOwnerName, windowBounds,
                                windowOwnerPID, windowLayer, visible));
                    }
                }
            }
        } finally {
            // CF references from "Copy" or "Create" must be released
            kCGWindowIsOnscreen.release();
            kCGWindowNumber.release();
            kCGWindowOwnerPID.release();
            kCGWindowLayer.release();
            kCGWindowBounds.release();
            kCGWindowName.release();
            kCGWindowOwnerName.release();
            windowInfo.release();
        }

        return windowList;
    }
}
