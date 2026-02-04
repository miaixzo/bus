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
package org.miaixz.bus.health.windows.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSDesktopWindow;

import com.sun.jna.Pointer;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;

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
     * @return A list of {@link OSDesktopWindow} objects representing the desktop windows.
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
                        windowList.add(
                                new OSDesktopWindow(Pointer.nativeValue(hWnd.getPointer()), window.getTitle(),
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
