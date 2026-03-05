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
package org.miaixz.bus.core.text.bloom;

import java.io.Serializable;

/**
 * Bloom filter is a binary vector data structure proposed by Howard Bloom in 1970. It offers excellent space and time
 * efficiency and is used to check if an element is a member of a set. If the check result is positive, the element may
 * or may not be in the set; however, if the check result is negative, the element is definitely not in the set.
 * Therefore, a Bloom filter has a 100% recall rate. Each check request can return two outcomes: "in the set (possibly
 * false positive)" and "not in the set (definitely not in the set)".
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BloomFilter extends Serializable {

    /**
     * Checks if the given text is contained in the Bloom filter. If the text is definitely not in the filter, it
     * returns {@code false}. If the text might be in the filter (with a possibility of false positive), it returns
     * {@code true}.
     *
     * @param text The string to check.
     * @return {@code true} if the text might be in the filter, {@code false} if it is definitely not in the filter.
     */
    boolean contains(String text);

    /**
     * Adds a string to the Bloom filter. If the string already exists in the filter, it returns {@code false}. If the
     * string does not exist, it adds the string and returns {@code true}.
     *
     * @param text The string to add.
     * @return {@code true} if the string was added successfully, {@code false} if it already existed.
     */
    boolean add(String text);

}
