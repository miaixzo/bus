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
package org.miaixz.bus.core.lang.intern;

/**
 * An implementation of the {@link Intern} interface specifically for {@link String} objects. This class leverages the
 * default string interning mechanism provided by the Java Development Kit (JDK) through the {@link String#intern()}
 * method.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class StringIntern implements Intern<String> {

    /**
     * Returns the canonical representation for the given string. This method delegates to {@link String#intern()} to
     * return a canonical string. If the {@code sample} string is {@code null}, this method returns {@code null}.
     *
     * @param sample The string for which to retrieve the canonical representation.
     * @return The canonical string instance, or {@code null} if the sample is {@code null}.
     */
    @Override
    public String intern(final String sample) {
        if (null == sample) {
            return null;
        }
        return sample.intern();
    }

}
