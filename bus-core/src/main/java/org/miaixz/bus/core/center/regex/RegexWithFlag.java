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
package org.miaixz.bus.core.center.regex;

/**
 * Wrapper for a regular expression string and its associated flags. This class is used as a key in a cache to store
 * compiled {@link java.util.regex.Pattern} objects, ensuring that patterns with the same regex string and flags are
 * reused.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexWithFlag {

    /**
     * The regular expression string.
     */
    private final String regex;
    /**
     * The flags for the regular expression, e.g., {@link java.util.regex.Pattern#CASE_INSENSITIVE}.
     */
    private final int flag;

    /**
     * Constructs a new {@code RegexWithFlag} instance.
     *
     * @param regex The regular expression string.
     * @param flag  The flags for the regular expression.
     */
    public RegexWithFlag(final String regex, final int flag) {
        this.regex = regex;
        this.flag = flag;
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + flag;
        result = prime * result + ((regex == null) ? 0 : regex.hashCode());
        return result;
    }

    /**
     * Checks if this object equals another object.
     *
     * @param object the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final RegexWithFlag other = (RegexWithFlag) object;
        if (flag != other.flag) {
            return false;
        }
        if (regex == null) {
            return other.regex == null;
        } else {
            return regex.equals(other.regex);
        }
    }

}
