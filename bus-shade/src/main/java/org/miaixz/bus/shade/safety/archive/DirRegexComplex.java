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
package org.miaixz.bus.shade.safety.archive;

import java.io.File;
import java.util.regex.Pattern;

import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.complex.RegexComplex;

/**
 * A {@link Complex} implementation that filters {@link File} entries based on regular expression matching. This class
 * extends {@link RegexComplex} and provides a way to apply regex patterns to file names.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DirRegexComplex extends RegexComplex<File> implements Complex<File> {

    /**
     * Constructs a new {@code DirRegexComplex} with the specified regular expression string.
     *
     * @param regex The regular expression string to use for filtering.
     */
    public DirRegexComplex(String regex) {
        super(regex);
    }

    /**
     * Constructs a new {@code DirRegexComplex} with the specified compiled {@link Pattern}.
     *
     * @param pattern The compiled {@link Pattern} to use for filtering.
     */
    public DirRegexComplex(Pattern pattern) {
        super(pattern);
    }

    /**
     * Converts a {@link File} entry into a string representation for pattern matching. This implementation returns the
     * name of the file.
     *
     * @param entry The {@link File} entry to convert.
     * @return The name of the file.
     */
    @Override
    protected String toText(File entry) {
        return entry.getName();
    }

}
