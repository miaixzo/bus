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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Parsing;

/**
 * Parses BSD mount tables and {@code df -i} output without losing spaces in mount points.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class MountTable {

    /**
     * JSON string pattern that keeps escape sequences for later decoding.
     */
    private static final String JSON_STRING = "\"([^\"\\\\]*+(?:\\\\.[^\"\\\\]*+)*+)\"";

    /**
     * FreeBSD libxo fstab entry pattern.
     */
    private static final Pattern LIBXO_ENTRY = Pattern.compile(
            "\\{\\s*\"device\"\\s*:\\s*" + JSON_STRING + "\\s*,\\s*\"mntpoint\"\\s*:\\s*" + JSON_STRING
                    + "\\s*,\\s*\"fstype\"\\s*:\\s*" + JSON_STRING + "\\s*,\\s*\"opts\"\\s*:\\s*" + JSON_STRING);

    /**
     * Consecutive tab separator used by {@code mount -p}.
     */
    private static final Pattern TABS = Pattern.compile("\\t+");

    /**
     * OpenBSD verbose mount row pattern.
     */
    private static final Pattern OPENBSD_MOUNT = Pattern
            .compile("(.+?)(?: \\(([0-9a-f]{16}\\.[a-p])\\))? on (.+) type (\\S+) (\\(.*\\))");

    /**
     * BSD inode row pattern whose final group preserves the complete mount point.
     */
    private static final Pattern DF_INODES = Pattern
            .compile("(.+?)\\s+\\d+\\s+\\d+\\s+-?\\d+\\s+(?:\\d+%|-)\\s+(\\d+)\\s+(-?\\d+)\\s+(?:\\d+%|-)\\s+(.+)");

    /**
     * Prevents instantiation.
     */
    private MountTable() {
    }

    /**
     * One mounted file system entry.
     *
     * @author Kimi Liu
     */
    @Immutable
    public static final class Entry {

        /**
         * Mounted device or source.
         */
        private final String volume;
        /**
         * Mount-table identifier.
         */
        private final String uuid;
        /**
         * Mount point.
         */
        private final String path;
        /**
         * File-system type.
         */
        private final String type;
        /**
         * Mount options.
         */
        private final String options;

        /**
         * Creates a mounted file-system entry.
         *
         * @param volume  the mounted source
         * @param uuid    the source identifier
         * @param path    the mount point
         * @param type    the file-system type
         * @param options the mount options
         */
        Entry(String volume, String uuid, String path, String type, String options) {
            this.volume = volume;
            this.uuid = uuid;
            this.path = path;
            this.type = type;
            this.options = options;
        }

        /**
         * Returns the mounted source.
         *
         * @return the mounted source
         */
        public String getVolume() {
            return volume;
        }

        /**
         * Returns the source identifier.
         *
         * @return the source identifier, or an empty string
         */
        public String getUuid() {
            return uuid;
        }

        /**
         * Returns the mount point.
         *
         * @return the mount point
         */
        public String getPath() {
            return path;
        }

        /**
         * Returns the file-system type.
         *
         * @return the file-system type
         */
        public String getType() {
            return type;
        }

        /**
         * Returns the mount options.
         *
         * @return the mount options
         */
        public String getOptions() {
            return options;
        }
    }

    /**
     * Parses FreeBSD's {@code mount -p --libxo json} output.
     *
     * @param lines the command output
     * @return the mounted file systems
     */
    public static List<Entry> parseFreeBsdLibxo(List<String> lines) {
        List<Entry> entries = new ArrayList<>();
        Matcher matcher = LIBXO_ENTRY.matcher(String.join(Symbol.LF, lines));
        while (matcher.find()) {
            entries.add(
                    new Entry(unescapeJson(matcher.group(1)), Normal.EMPTY, unescapeJson(matcher.group(2)),
                            unescapeJson(matcher.group(3)), unescapeJson(matcher.group(4))));
        }
        return entries;
    }

    /**
     * Parses tabular {@code mount -p} output when libxo is unavailable.
     *
     * @param lines the command output
     * @return the mounted file systems
     */
    public static List<Entry> parseFstab(List<String> lines) {
        List<Entry> entries = new ArrayList<>();
        for (String line : lines) {
            String[] tabs = TABS.split(line.trim(), -1);
            int typeSpace = tabs.length == 4 ? tabs[2].trim().indexOf(Symbol.C_SPACE) : -1;
            int pathSpace = tabs.length == 4 ? tabs[1].lastIndexOf(Symbol.C_SPACE) : -1;
            if (typeSpace > 0) {
                entries.add(
                        new Entry(tabs[0], Normal.EMPTY, tabs[1], tabs[2].substring(0, typeSpace),
                                tabs[2].substring(typeSpace + 1).trim()));
            } else if (pathSpace > 0) {
                entries.add(
                        new Entry(tabs[0], Normal.EMPTY, tabs[1].substring(0, pathSpace),
                                tabs[1].substring(pathSpace + 1), tabs[2].trim()));
            } else if (tabs.length >= 5) {
                entries.add(new Entry(tabs[0], Normal.EMPTY, tabs[1], tabs[2], tabs[3].trim()));
            } else {
                String[] fields = org.miaixz.bus.core.center.regex.Pattern.SPACES_PATTERN.split(line.trim());
                if (fields.length >= 5) {
                    entries.add(new Entry(fields[0], Normal.EMPTY, fields[1], fields[2], fields[3]));
                }
            }
        }
        return entries;
    }

    /**
     * Parses OpenBSD's {@code mount -v} output.
     *
     * @param lines the command output
     * @return the mounted file systems
     */
    public static List<Entry> parseOpenBsdVerbose(List<String> lines) {
        List<Entry> entries = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = OPENBSD_MOUNT.matcher(line.trim());
            if (matcher.matches()) {
                entries.add(
                        new Entry(matcher.group(1), matcher.group(2) == null ? Normal.EMPTY : matcher.group(2),
                                matcher.group(3), matcher.group(4), matcher.group(5)));
            }
        }
        return entries;
    }

    /**
     * Parses BSD {@code df -i} output into free and total inode counts keyed by mount point.
     *
     * @param lines the command output
     * @return a pair containing free and total inode maps
     */
    public static Pair<Map<String, Long>, Map<String, Long>> parseDfInodes(List<String> lines) {
        Map<String, Long> free = new HashMap<>();
        Map<String, Long> total = new HashMap<>();
        for (String line : lines) {
            Matcher matcher = DF_INODES.matcher(line.trim());
            if (matcher.matches()) {
                long used = Parsing.parseLongOrDefault(matcher.group(2), 0L);
                long available = Parsing.parseLongOrDefault(matcher.group(3), 0L);
                free.put(matcher.group(4), available);
                total.put(matcher.group(4), used + available);
            }
        }
        return Pair.of(free, total);
    }

    /**
     * Decodes the JSON escapes used by libxo mount output.
     *
     * @param value the escaped value
     * @return the decoded value
     */
    private static String unescapeJson(String value) {
        if (value.indexOf(Symbol.C_BACKSLASH) < 0) {
            return value;
        }
        StringBuilder result = new StringBuilder(value.length());
        int index = 0;
        while (index < value.length()) {
            char current = value.charAt(index);
            if (current != Symbol.C_BACKSLASH || index + 1 == value.length()) {
                result.append(current);
                index++;
                continue;
            }
            char escaped = value.charAt(index + 1);
            index += 2;
            switch (escaped) {
                case 'n':
                    result.append(Symbol.C_LF);
                    break;

                case 't':
                    result.append(Symbol.C_TAB);
                    break;

                case 'r':
                    result.append(Symbol.C_CR);
                    break;

                case 'b':
                    result.append('\b');
                    break;

                case 'f':
                    result.append('\f');
                    break;

                case 'u':
                    if (index + 4 <= value.length()) {
                        result.append(
                                (char) Parsing
                                        .hexStringToInt(value.substring(index, index + 4), Symbol.C_QUESTION_MARK));
                        index += 4;
                    } else {
                        result.append(escaped);
                    }
                    break;

                default:
                    result.append(escaped);
                    break;
            }
        }
        return result.toString();
    }

}
