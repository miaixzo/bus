/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.windows.drivers;

import com.sun.jna.Pointer;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.ByRef;
import org.miaixz.bus.health.builtin.software.OSDesktopWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to query Desktop windows
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class EnumWindows {

    private static final DWORD GW_HWNDNEXT = new DWORD(2);

    /**
     * Gets windows on the operating system's GUI desktop.
     *
     * @param visibleOnly Whether to restrict the list to only windows visible to the user.
     * @return A list of {@link OSDesktopWindow} objects
     * representing the desktop windows.
     */
    public static List<OSDesktopWindow> queryDesktopWindows(boolean visibleOnly) {
        // Get the windows using JNA's implementation
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        // Prepare a list to return
        List<OSDesktopWindow> windowList = new ArrayList<>();
        // Populate the list
        Map<HWND, Integer> zOrderMap = new HashMap<>();
        for (DesktopWindow window : windows) {
            HWND hWnd = window.getHWND();
            if (hWnd != null) {
                boolean visible = User32.INSTANCE.IsWindowVisible(hWnd);
                if (!visibleOnly || visible) {
                    if (!zOrderMap.containsKey(hWnd)) {
                        updateWindowZOrderMap(hWnd, zOrderMap);
                    }
                    try (ByRef.CloseableIntByReference pProcessId = new ByRef.CloseableIntByReference()) {
                        User32.INSTANCE.GetWindowThreadProcessId(hWnd, pProcessId);
                        windowList.add(new OSDesktopWindow(Pointer.nativeValue(hWnd.getPointer()), window.getTitle(),
                                window.getFilePath(), window.getLocAndSize(), pProcessId.getValue(),
                                zOrderMap.get(hWnd), visible));
                    }
                }
            }
        }
        return windowList;
    }

    private static void updateWindowZOrderMap(HWND hWnd, Map<HWND, Integer> zOrderMap) {
        if (hWnd != null) {
            int zOrder = 1;
            HWND h = new HWND(hWnd.getPointer());
            // First is highest, so decrement
            do {
                zOrderMap.put(h, --zOrder);
            } while ((h = User32.INSTANCE.GetWindow(h, GW_HWNDNEXT)) != null);
            // now add lowest value to all
            final int offset = zOrder * -1;
            zOrderMap.replaceAll((k, v) -> v + offset);
        }
    }

}
