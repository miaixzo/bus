/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.safety.archive;

import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.complex.RegexComplex;

/**
 * A {@link Complex} implementation that filters {@link ZipArchiveEntry} entries based on regular expression matching.
 * This class extends {@link RegexComplex} and provides a way to apply regex patterns to the names of ZIP archive
 * entries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipRegexComplex extends RegexComplex<ZipArchiveEntry> implements Complex<ZipArchiveEntry> {

    /**
     * Constructs a new {@code ZipRegexComplex} with the specified regular expression string.
     *
     * @param regex The regular expression string to use for filtering.
     */
    public ZipRegexComplex(String regex) {
        super(regex);
    }

    /**
     * Constructs a new {@code ZipRegexComplex} with the specified compiled {@link Pattern}.
     *
     * @param pattern The compiled {@link Pattern} to use for filtering.
     */
    public ZipRegexComplex(Pattern pattern) {
        super(pattern);
    }

    /**
     * Converts a {@link ZipArchiveEntry} into a string representation for pattern matching. This implementation returns
     * the name of the ZIP archive entry.
     *
     * @param entry The {@link ZipArchiveEntry} to convert.
     * @return The name of the ZIP archive entry.
     */
    @Override
    protected String toText(ZipArchiveEntry entry) {
        return entry.getName();
    }

}
