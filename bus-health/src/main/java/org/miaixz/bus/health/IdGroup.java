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
package org.miaixz.bus.health;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;

import com.sun.jna.Platform;

/**
 * Utility class for temporarily caching user ID and group mappings in *nix systems to resolve process ownership. The
 * cache expires after one minute.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class IdGroup {

    /**
     * Supplier for a map of user IDs to usernames, with the cache fully refreshed after 5 minutes.
     */
    private static final Supplier<Map<String, String>> USERS_ID_MAP = Memoizer
            .memoize(IdGroup::getUserMap, TimeUnit.MINUTES.toNanos(5));

    /**
     * Supplier for a map of group IDs to group names, with the cache fully refreshed after 5 minutes.
     */
    private static final Supplier<Map<String, String>> GROUPS_ID_MAP = Memoizer
            .memoize(IdGroup::getGroupMap, TimeUnit.MINUTES.toNanos(5));

    /**
     * Flag indicating whether the current process has elevated privileges (e.g., sudo / Administrator), determined by
     * checking if the output of "id -u" is 0.
     */
    private static final boolean ELEVATED = 0 == Parsing.parseIntOrDefault(Executor.getFirstAnswer("id -u"), -1);

    /**
     * Checks if the current process has elevated privileges, such as sudo or Administrator.
     *
     * @return {@code true} if the current process has elevated privileges, {@code false} otherwise.
     */
    public static boolean isElevated() {
        return ELEVATED;
    }

    /**
     * Retrieves the username associated with the given user ID.
     *
     * @param userId The user ID.
     * @return The username corresponding to the user ID, or {@link Normal#UNKNOWN} if not found.
     */
    public static String getUser(String userId) {
        // If the value is in the cached /etc/passwd, return it; otherwise, execute getent passwd uid.
        return USERS_ID_MAP.get().getOrDefault(userId, getentPasswd(userId));
    }

    /**
     * Retrieves the group name associated with the given group ID.
     *
     * @param groupId The group ID.
     * @return The group name corresponding to the group ID, or {@link Normal#UNKNOWN} if not found.
     */
    public static String getGroupName(String groupId) {
        // If the value is in the cached /etc/group, return it; otherwise, execute getent group gid.
        return GROUPS_ID_MAP.get().getOrDefault(groupId, getentGroup(groupId));
    }

    /**
     * Retrieves a map of user IDs to usernames.
     *
     * @return A map of user IDs to usernames parsed from the {@code /etc/passwd} file.
     */
    private static Map<String, String> getUserMap() {
        return parsePasswd(Builder.readFile("/etc/passwd"));
    }

    /**
     * Retrieves the username for a given user ID using the {@code getent passwd} command.
     *
     * @param userId The user ID.
     * @return The username, or {@link Normal#UNKNOWN} if it cannot be retrieved.
     */
    private static String getentPasswd(String userId) {
        if (Platform.isAIX()) {
            return Normal.UNKNOWN;
        }
        Map<String, String> newUsers = parsePasswd(Executor.runNative("getent passwd " + userId));
        // Add new users to the user map for subsequent queries
        USERS_ID_MAP.get().putAll(newUsers);
        return newUsers.getOrDefault(userId, Normal.UNKNOWN);
    }

    /**
     * Parses the content of a passwd file to generate a map of user IDs to usernames.
     *
     * @param passwd A list of lines from the passwd file content.
     * @return A map of user IDs to usernames.
     */
    private static Map<String, String> parsePasswd(List<String> passwd) {
        Map<String, String> userMap = new ConcurrentHashMap<>();
        // See man 5 passwd for field information
        for (String entry : passwd) {
            String[] split = entry.split(Symbol.COLON);
            if (split.length > 2) {
                String userName = split[0];
                String uid = split[2];
                // Allow multiple entries for the same user ID, use the first one
                userMap.putIfAbsent(uid, userName);
            }
        }
        return userMap;
    }

    /**
     * Retrieves a map of group IDs to group names.
     *
     * @return A map of group IDs to group names parsed from the {@code /etc/group} file.
     */
    private static Map<String, String> getGroupMap() {
        return parseGroup(Builder.readFile("/etc/group"));
    }

    /**
     * Retrieves the group name for a given group ID using the {@code getent group} command.
     *
     * @param groupId The group ID.
     * @return The group name, or {@link Normal#UNKNOWN} if it cannot be retrieved.
     */
    private static String getentGroup(String groupId) {
        if (Platform.isAIX()) {
            return Normal.UNKNOWN;
        }
        Map<String, String> newGroups = parseGroup(Executor.runNative("getent group " + groupId));
        // Add new groups to the group map for subsequent queries
        GROUPS_ID_MAP.get().putAll(newGroups);
        return newGroups.getOrDefault(groupId, Normal.UNKNOWN);
    }

    /**
     * Parses the content of a group file to generate a map of group IDs to group names.
     *
     * @param group A list of lines from the group file content.
     * @return A map of group IDs to group names.
     */
    private static Map<String, String> parseGroup(List<String> group) {
        Map<String, String> groupMap = new ConcurrentHashMap<>();
        // See man 5 group for field information
        for (String entry : group) {
            String[] split = entry.split(Symbol.COLON);
            if (split.length > 2) {
                String groupName = split[0];
                String gid = split[2];
                groupMap.putIfAbsent(gid, groupName);
            }
        }
        return groupMap;
    }

}
