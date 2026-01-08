/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
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

import org.miaixz.bus.health.Builder;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;

/**
 * The Core Graphics framework is based on the Quartz advanced drawing provider. It provides low-level, lightweight 2D
 * rendering with unmatched output fidelity. You use this framework to handle path-based drawing, transformations, color
 * management, offscreen rendering, patterns, gradients and shadings, image data management, image creation, and image
 * masking, as well as PDF document creation, display, and parsing. In macOS, Core Graphics also includes services for
 * working with display hardware, low-level user input events, and the windowing system.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CoreGraphics extends Library {

    /**
     * Singleton instance of the CoreGraphics library.
     */
    CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);

    /**
     * A constant representing a null window ID.
     */
    int kCGNullWindowID = 0;

    /**
     * Option to include all windows in the window list.
     */
    int kCGWindowListOptionAll = 0;
    /**
     * Option to include only on-screen windows in the window list.
     */
    int kCGWindowListOptionOnScreenOnly = 1 << 0;
    /**
     * Option to include on-screen windows above a specified window in the window list.
     */
    int kCGWindowListOptionOnScreenAboveWindow = 1 << 1;
    /**
     * Option to include on-screen windows below a specified window in the window list.
     */
    int kCGWindowListOptionOnScreenBelowWindow = 1 << 2;
    /**
     * Option to include a specific window in the window list.
     */
    int kCGWindowListOptionIncludingWindow = 1 << 3;
    /**
     * Option to exclude desktop elements from the window list.
     */
    int kCGWindowListExcludeDesktopElements = 1 << 4;

    /**
     * Returns an array of dictionaries, where each dictionary describes a window in the current window list.
     *
     * @param option           A bit mask of options for specifying which windows to include in the list.
     * @param relativeToWindow The ID of a window to which the options relate.
     * @return A CFArrayRef containing CFDictionaryRef objects, each describing a window.
     */
    CFArrayRef CGWindowListCopyWindowInfo(int option, int relativeToWindow);

    /**
     * Creates a CGRect structure from a dictionary representation.
     *
     * @param dict The dictionary representation of the CGRect.
     * @param rect The CGRect structure to populate.
     * @return True if the CGRect was successfully created, false otherwise.
     */
    boolean CGRectMakeWithDictionaryRepresentation(CFDictionaryRef dict, CGRect rect);

    /**
     * JNA wrapper for the CGPoint structure.
     * <p>
     * This class maps to the native macOS structure: {@code struct CGPoint { CGFloat x; CGFloat y; }; }
     * </p>
     */
    @FieldOrder({ "x", "y" })
    class CGPoint extends Structure {

        /**
         * The x-coordinate of the point.
         */
        public double x;
        /**
         * The y-coordinate of the point.
         */
        public double y;

    }

    /**
     * JNA wrapper for the CGSize structure.
     * <p>
     * This class maps to the native macOS structure: {@code struct CGSize { CGFloat width; CGFloat height; }; }
     * </p>
     */
    @FieldOrder({ "width", "height" })
    class CGSize extends Structure {

        /**
         * The width component of the size.
         */
        public double width;
        /**
         * The height component of the size.
         */
        public double height;
    }

    /**
     * JNA wrapper for the CGRect structure.
     * <p>
     * This class maps to the native macOS structure: {@code struct CGRect { CGPoint origin; CGSize size; }; }
     * </p>
     */
    @FieldOrder({ "origin", "size" })
    class CGRect extends Structure implements AutoCloseable {

        /**
         * The origin point (x, y) of the rectangle.
         */
        public CGPoint origin;
        /**
         * The size (width, height) of the rectangle.
         */
        public CGSize size;

        /**
         * Closes the memory associated with this structure.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

}
