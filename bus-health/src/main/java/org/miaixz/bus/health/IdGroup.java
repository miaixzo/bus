/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health;

import com.sun.jna.Platform;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class to temporarily cache the userID and group maps in *nix, for
 * parsing process ownership. Cache expires after one minute.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class IdGroup {

    // Temporarily cache users and groups in concurrent maps, completely refresh
    // every 5 minutes
    private static final Supplier<Map<String, String>> USERS_ID_MAP = Memoizer.memoize(IdGroup::getUserMap,
            TimeUnit.MINUTES.toNanos(5));
    private static final Supplier<Map<String, String>> GROUPS_ID_MAP = Memoizer.memoize(IdGroup::getGroupMap,
            TimeUnit.MINUTES.toNanos(5));

    private static final boolean ELEVATED = 0 == Parsing.parseIntOrDefault(Executor.getFirstAnswer("id -u"),
            -1);

    /**
     * Determine whether the current process has elevated permissions such as sudo /
     * Administrator
     *
     * @return True if this process has elevated permissions
     */
    public static boolean isElevated() {
        return ELEVATED;
    }

    /**
     * Gets a user from their ID
     *
     * @param userId a user ID
     * @return a pair containing that user id as the first element and the user name
     * as the second
     */
    public static String getUser(String userId) {
        // If value is in cached /etc/passwd return, else do getent passwd uid
        return USERS_ID_MAP.get().getOrDefault(userId, getentPasswd(userId));
    }

    /**
     * Gets the group name for a given ID
     *
     * @param groupId a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getGroupName(String groupId) {
        // If value is in cached /etc/passwd return, else do getent group gid
        return GROUPS_ID_MAP.get().getOrDefault(groupId, getentGroup(groupId));
    }

    private static Map<String, String> getUserMap() {
        return parsePasswd(Builder.readFile("/etc/passwd"));
    }

    private static String getentPasswd(String userId) {
        if (Platform.isAIX()) {
            return Normal.UNKNOWN;
        }
        Map<String, String> newUsers = parsePasswd(Executor.runNative("getent passwd " + userId));
        // add to user map for future queries
        USERS_ID_MAP.get().putAll(newUsers);
        return newUsers.getOrDefault(userId, Normal.UNKNOWN);
    }

    private static Map<String, String> parsePasswd(List<String> passwd) {
        Map<String, String> userMap = new ConcurrentHashMap<>();
        // see man 5 passwd for the fields
        for (String entry : passwd) {
            String[] split = entry.split(":");
            if (split.length > 2) {
                String userName = split[0];
                String uid = split[2];
                // it is allowed to have multiple entries for the same userId,
                // we use the first one
                userMap.putIfAbsent(uid, userName);
            }
        }
        return userMap;
    }

    private static Map<String, String> getGroupMap() {
        return parseGroup(Builder.readFile("/etc/group"));
    }

    private static String getentGroup(String groupId) {
        if (Platform.isAIX()) {
            return Normal.UNKNOWN;
        }
        Map<String, String> newGroups = parseGroup(Executor.runNative("getent group " + groupId));
        // add to group map for future queries
        GROUPS_ID_MAP.get().putAll(newGroups);
        return newGroups.getOrDefault(groupId, Normal.UNKNOWN);
    }

    private static Map<String, String> parseGroup(List<String> group) {
        Map<String, String> groupMap = new ConcurrentHashMap<>();
        // see man 5 group for the fields
        for (String entry : group) {
            String[] split = entry.split(":");
            if (split.length > 2) {
                String groupName = split[0];
                String gid = split[2];
                groupMap.putIfAbsent(gid, groupName);
            }
        }
        return groupMap;
    }

}
