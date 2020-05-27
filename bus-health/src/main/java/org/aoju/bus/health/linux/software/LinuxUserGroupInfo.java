/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.linux.software;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LinuxUserGroupInfo class.
 *
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
public class LinuxUserGroupInfo {

    // Temporarily cache users and groups, populated by constructor
    private final Map<String, Pair<String, String>> usersIdMap;
    private final Map<String, String> groupsIdMap;

    public LinuxUserGroupInfo() {
        usersIdMap = getUserMap();
        groupsIdMap = getGroupMap();
    }

    private static Map<String, Pair<String, String>> getUserMap() {
        HashMap<String, Pair<String, String>> userMap = new HashMap<>();
        List<String> passwd = Executor.runNative("getent passwd");
        // see man 5 passwd for the fields
        for (String entry : passwd) {
            String[] split = entry.split(Symbol.COLON);
            if (split.length < 3) {
                continue;
            }
            String userName = split[0];
            String uid = split[2];
            // it is allowed to have multiple entries for the same userId,
            // we use the first one
            userMap.putIfAbsent(uid, Pair.of(uid, userName));
        }
        return userMap;
    }

    private static Map<String, String> getGroupMap() {
        Map<String, String> groupMap = new HashMap<>();
        List<String> group = Executor.runNative("getent group");
        // see man 5 group for the fields
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

    /**
     * Gets a user from their ID
     *
     * @param userId a user ID
     * @return a pair containing that user id as the first element and the user name
     * as the second
     */
    public Pair<String, String> getUser(String userId) {
        return this.usersIdMap.getOrDefault(userId, Pair.of(userId, Normal.UNKNOWN));
    }

    /**
     * <p>
     * getGroupName.
     * </p>
     *
     * @param groupId a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getGroupName(String groupId) {
        return this.groupsIdMap.getOrDefault(groupId, Normal.UNKNOWN);
    }

}
