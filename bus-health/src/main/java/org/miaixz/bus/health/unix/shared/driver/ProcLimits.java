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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;

/**
 * Reads process resource limits from {@code /proc/<pid>/limits}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class ProcLimits {

    /**
     * Creates a new ProcLimits instance.
     */
    private ProcLimits() {
        // No initialization required.
    }

    /**
     * Queries {@code /proc/<pid>/limits} for a process open-file limit.
     *
     * @param processId The process ID.
     * @param index     {@code 1} for the soft limit, {@code 2} for the hard limit.
     * @return The limit, or {@code -1} if unavailable or unlimited.
     */
    public static long queryOpenFileLimit(long processId, int index) {
        final String limitsPath = String.format(Locale.ROOT, "/proc/%d/limits", processId);
        if (!Files.exists(Paths.get(limitsPath))) {
            return -1;
        }
        return parseOpenFileLimit(Builder.readFile(limitsPath), index);
    }

    /**
     * Parses the open-file limit from {@code /proc/<pid>/limits} content.
     *
     * @param lines The lines of {@code /proc/<pid>/limits}.
     * @param index {@code 1} for the soft limit, {@code 2} for the hard limit.
     * @return The limit, or {@code -1} if the row or requested field is unavailable.
     */
    public static long parseOpenFileLimit(List<String> lines, int index) {
        final Optional<String> maxOpenFilesLine = lines.stream().filter(line -> line.startsWith("Max open files"))
                .findFirst();
        if (!maxOpenFilesLine.isPresent()) {
            return -1;
        }
        final String[] split = maxOpenFilesLine.get().split("\\D+");
        if (index < 1 || split.length <= index) {
            return -1;
        }
        return Parsing.parseLongOrDefault(split[index], -1);
    }

}
