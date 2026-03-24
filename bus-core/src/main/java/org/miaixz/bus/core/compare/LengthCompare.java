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
package org.miaixz.bus.core.compare;

import java.util.Comparator;

import org.miaixz.bus.core.xyz.CompareKit;

/**
 * Comparator for {@link CharSequence} length, sorting shorter sequences first. If lengths are equal, it performs a
 * lexicographical comparison.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LengthCompare implements Comparator<CharSequence> {

    /**
     * Singleton instance of {@code LengthCompare}, which sorts shorter sequences first.
     */
    public static final LengthCompare INSTANCE = new LengthCompare();

    /**
     * Compares two {@link CharSequence} objects based on their length. If the lengths are equal, a lexicographical
     * comparison is performed on their string representations.
     *
     * @param o1 the first {@link CharSequence} to be compared.
     * @param o2 the second {@link CharSequence} to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     */
    @Override
    public int compare(final CharSequence o1, final CharSequence o2) {
        int result = Integer.compare(o1.length(), o2.length());
        if (0 == result) {
            result = CompareKit.compare(o1.toString(), o2.toString());
        }
        return result;
    }

}
