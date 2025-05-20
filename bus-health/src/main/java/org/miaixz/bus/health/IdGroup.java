/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
 * 工具类，用于在 *nix 系统中临时缓存用户 ID 和组映射，以便解析进程所有权。缓存在一分钟后过期。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class IdGroup {

    /**
     * 用户 ID 到用户名的映射供应商，缓存 5 分钟后完全刷新
     */
    private static final Supplier<Map<String, String>> USERS_ID_MAP = Memoizer.memoize(IdGroup::getUserMap,
            TimeUnit.MINUTES.toNanos(5));

    /**
     * 组 ID 到组名的映射供应商，缓存 5 分钟后完全刷新
     */
    private static final Supplier<Map<String, String>> GROUPS_ID_MAP = Memoizer.memoize(IdGroup::getGroupMap,
            TimeUnit.MINUTES.toNanos(5));

    /**
     * 标志当前进程是否具有提升的权限（如 sudo / Administrator），通过检查 "id -u" 输出是否为 0
     */
    private static final boolean ELEVATED = 0 == Parsing.parseIntOrDefault(Executor.getFirstAnswer("id -u"), -1);

    /**
     * 判断当前进程是否具有提升的权限，如 sudo / Administrator。
     *
     * @return 如果当前进程具有提升的权限，返回 true
     */
    public static boolean isElevated() {
        return ELEVATED;
    }

    /**
     * 根据用户 ID 获取用户名。
     *
     * @param userId 用户 ID
     * @return 包含用户 ID 作为第一个元素和用户名作为第二个元素的字符串
     */
    public static String getUser(String userId) {
        // 如果值在缓存的 /etc/passwd 中，则返回；否则执行 getent passwd uid
        return USERS_ID_MAP.get().getOrDefault(userId, getentPasswd(userId));
    }

    /**
     * 根据组 ID 获取组名。
     *
     * @param groupId 组 ID
     * @return 对应的组名
     */
    public static String getGroupName(String groupId) {
        // 如果值在缓存的 /etc/group 中，则返回；否则执行 getent group gid
        return GROUPS_ID_MAP.get().getOrDefault(groupId, getentGroup(groupId));
    }

    /**
     * 获取用户 ID 到用户名的映射。
     *
     * @return 从 /etc/passwd 文件解析出的用户 ID 到用户名的映射
     */
    private static Map<String, String> getUserMap() {
        return parsePasswd(Builder.readFile("/etc/passwd"));
    }

    /**
     * 通过 getent passwd 命令获取用户 ID 对应的用户名。
     *
     * @param userId 用户 ID
     * @return 用户名，如果无法获取则返回 Normal.UNKNOWN
     */
    private static String getentPasswd(String userId) {
        if (Platform.isAIX()) {
            return Normal.UNKNOWN;
        }
        Map<String, String> newUsers = parsePasswd(Executor.runNative("getent passwd " + userId));
        // 将新用户添加到用户映射以供后续查询
        USERS_ID_MAP.get().putAll(newUsers);
        return newUsers.getOrDefault(userId, Normal.UNKNOWN);
    }

    /**
     * 解析 passwd 文件内容，生成用户 ID 到用户名的映射。
     *
     * @param passwd passwd 文件内容的行列表
     * @return 用户 ID 到用户名的映射
     */
    private static Map<String, String> parsePasswd(List<String> passwd) {
        Map<String, String> userMap = new ConcurrentHashMap<>();
        // 参见 man 5 passwd 获取字段信息
        for (String entry : passwd) {
            String[] split = entry.split(Symbol.COLON);
            if (split.length > 2) {
                String userName = split[0];
                String uid = split[2];
                // 允许同一用户 ID 有多个条目，使用第一个
                userMap.putIfAbsent(uid, userName);
            }
        }
        return userMap;
    }

    /**
     * 获取组 ID 到组名的映射。
     *
     * @return 从 /etc/group 文件解析出的组 ID 到组名的映射
     */
    private static Map<String, String> getGroupMap() {
        return parseGroup(Builder.readFile("/etc/group"));
    }

    /**
     * 通过 getent group 命令获取组 ID 对应的组名。
     *
     * @param groupId 组 ID
     * @return 组名，如果无法获取则返回 Normal.UNKNOWN
     */
    private static String getentGroup(String groupId) {
        if (Platform.isAIX()) {
            return Normal.UNKNOWN;
        }
        Map<String, String> newGroups = parseGroup(Executor.runNative("getent group " + groupId));
        // 将新组添加到组映射以供后续查询
        GROUPS_ID_MAP.get().putAll(newGroups);
        return newGroups.getOrDefault(groupId, Normal.UNKNOWN);
    }

    /**
     * 解析 group 文件内容，生成组 ID 到组名的映射。
     *
     * @param group group 文件内容的行列表
     * @return 组 ID 到组名的映射
     */
    private static Map<String, String> parseGroup(List<String> group) {
        Map<String, String> groupMap = new ConcurrentHashMap<>();
        // 参见 man 5 group 获取字段信息
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