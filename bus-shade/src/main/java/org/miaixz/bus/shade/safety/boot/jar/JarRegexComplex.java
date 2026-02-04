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
package org.miaixz.bus.shade.safety.boot.jar;

import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.complex.RegexComplex;

/**
 * A {@link Complex} implementation that filters {@link JarArchiveEntry} entries based on regular expression matching.
 * This class extends {@link RegexComplex} and provides a way to apply regex patterns to the names of JAR archive
 * entries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarRegexComplex extends RegexComplex<JarArchiveEntry> implements Complex<JarArchiveEntry> {

    /**
     * Constructs a new {@code JarRegexComplex} with the specified regular expression string.
     *
     * @param regex The regular expression string to use for filtering.
     */
    public JarRegexComplex(String regex) {
        super(regex);
    }

    /**
     * Constructs a new {@code JarRegexComplex} with the specified compiled {@link Pattern}.
     *
     * @param pattern The compiled {@link Pattern} to use for filtering.
     */
    public JarRegexComplex(Pattern pattern) {
        super(pattern);
    }

    /**
     * Converts a {@link JarArchiveEntry} into a string representation for pattern matching. This implementation returns
     * the name of the JAR archive entry.
     *
     * @param entry The {@link JarArchiveEntry} to convert.
     * @return The name of the JAR archive entry.
     */
    @Override
    protected String toText(JarArchiveEntry entry) {
        return entry.getName();
    }

}
