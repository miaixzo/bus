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
package org.miaixz.bus.health.unix.shared.driver;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSDesktopWindow;

/**
 * Utility to query X11 windows.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Xwininfo {

    /**
     * The NET_CLIENT_LIST_STACKING constant.
     */
    private static final String[] NET_CLIENT_LIST_STACKING = Pattern.SPACES_PATTERN
            .split("xprop -root _NET_CLIENT_LIST_STACKING");

    /**
     * The XWININFO_ROOT_TREE constant.
     */
    private static final String[] XWININFO_ROOT_TREE = Pattern.SPACES_PATTERN.split("xwininfo -root -tree");

    /**
     * The XPROP_NET_WM_PID_ID constant.
     */
    private static final String[] XPROP_NET_WM_PID_ID = Pattern.SPACES_PATTERN.split("xprop _NET_WM_PID -id");

    /**
     * The WINDOW_PATTERN constant.
     */
    private static final java.util.regex.Pattern WINDOW_PATTERN = java.util.regex.Pattern.compile(
            "(0x\\S+) (?:\"(.+)\")?.*: \\((?:\"(.+)\" \".+\")?\\)  (\\d+)x(\\d+)\\+.+  \\+(-?\\d+)\\+(-?\\d+)");

    /**
     * Gets windows on the operating system's GUI desktop.
     *
     * @param visibleOnly Whether to restrict the list to only windows visible to the user.
     * @return A list of {@link OSDesktopWindow} objects representing the desktop windows.
     */
    public static List<OSDesktopWindow> queryXWindows(boolean visibleOnly) {
        // Attempted to implement using native X11 code. However, this produced native X
        // errors (e.g., BadValue) which cannot be caught on the Java side and
        // terminated the thread. Using x command lines which execute in a separate
        // process. Errors are caught by the terminal process and safely ignored.

        // X commands don't work with LC_ALL
        List<String> stacking = Executor.runNative(NET_CLIENT_LIST_STACKING, null);
        List<String> tree = Executor.runNative(XWININFO_ROOT_TREE, null);

        Map<String, Integer> zOrderMap = parseZOrder(stacking);
        Map<String, String> windowNameMap = new HashMap<>();
        Map<String, String> windowPathMap = new HashMap<>();
        Map<String, Rectangle> windowMap = parseWindowTree(tree, visibleOnly, zOrderMap, windowNameMap, windowPathMap);

        // Get info for each window
        // Prepare a list to return
        List<OSDesktopWindow> windowList = new ArrayList<>();
        for (Entry<String, Rectangle> e : windowMap.entrySet()) {
            String id = e.getKey();
            long pid = queryPidFromId(id);
            boolean visible = zOrderMap.containsKey(id);
            windowList.add(
                    new OSDesktopWindow(Parsing.hexStringToLong(id, 0L), windowNameMap.getOrDefault(id, Normal.EMPTY),
                            windowPathMap.getOrDefault(id, Normal.EMPTY), e.getValue(), pid,
                            zOrderMap.getOrDefault(id, 0), visible));
        }
        return windowList;
    }

    /**
     * Parses the z-order stacking from xprop output.
     *
     * @param stacking the output lines from xprop
     * @return a map of window IDs to z-order values
     */
    static Map<String, Integer> parseZOrder(List<String> stacking) {
        // Get visible windows in their Z order. Assign 1 to bottom and increment.
        // All other non visible windows will be assigned 0.
        Map<String, Integer> zOrderMap = new HashMap<>();
        int z = 0;
        if (!stacking.isEmpty()) {
            String stack = stacking.get(0);
            int bottom = stack.indexOf("0x");
            if (bottom >= 0) {
                for (String id : stack.substring(bottom).split(", ")) {
                    zOrderMap.put(id, ++z);
                }
            }
        }
        return zOrderMap;
    }

    /**
     * Parses the window tree from xwininfo output.
     *
     * @param tree          the output lines from xwininfo
     * @param visibleOnly   whether to restrict the list to only windows visible to the user
     * @param zOrderMap     the z-order map used for visibility filtering
     * @param windowNameMap the output map populated with window names
     * @param windowPathMap the output map populated with window paths
     * @return a map of window IDs to window bounds
     */
    static Map<String, Rectangle> parseWindowTree(
            List<String> tree,
            boolean visibleOnly,
            Map<String, Integer> zOrderMap,
            Map<String, String> windowNameMap,
            Map<String, String> windowPathMap) {
        // Get all windows along with title and path info
        // This map will include all the windows, preserve the insertion order
        Map<String, Rectangle> windowMap = new LinkedHashMap<>();
        for (String line : tree) {
            Matcher m = WINDOW_PATTERN.matcher(line.trim());
            if (m.matches()) {
                String id = m.group(1);
                if (!visibleOnly || zOrderMap.containsKey(id)) {
                    String windowName = m.group(2);
                    if (!StringKit.isBlank(windowName)) {
                        windowNameMap.put(id, windowName);
                    }
                    String windowPath = m.group(3);
                    if (!StringKit.isBlank(windowPath)) {
                        windowPathMap.put(id, windowPath);
                    }
                    windowMap.put(
                            id,
                            new Rectangle(Parsing.parseIntOrDefault(m.group(6), 0),
                                    Parsing.parseIntOrDefault(m.group(7), 0), Parsing.parseIntOrDefault(m.group(4), 0),
                                    Parsing.parseIntOrDefault(m.group(5), 0)));
                }
            }
        }
        return windowMap;
    }

    /**
     * Queries the process ID (PID) for a given window ID.
     *
     * @param id The window ID.
     * @return The PID of the process that owns the window, or 0 if not found.
     */
    private static long queryPidFromId(String id) {
        // X commands don't work with LC_ALL
        String[] cmd = new String[XPROP_NET_WM_PID_ID.length + 1];
        System.arraycopy(XPROP_NET_WM_PID_ID, 0, cmd, 0, XPROP_NET_WM_PID_ID.length);
        cmd[XPROP_NET_WM_PID_ID.length] = id;
        List<String> pidStr = Executor.runNative(cmd, null);
        if (pidStr.isEmpty()) {
            return 0;
        }
        return Parsing.getFirstIntValue(pidStr.get(0));
    }

}
