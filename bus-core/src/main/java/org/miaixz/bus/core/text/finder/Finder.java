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
package org.miaixz.bus.core.text.finder;

/**
 * Interface for string searching. It finds the start position by calling {@link #start(int)} and then the end position
 * by calling {@link #end(int)}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Finder {

    /**
     * Returns the starting position (inclusive). Returns -1 if not found.
     *
     * @param from The starting position for the search (inclusive).
     * @return The starting character position, or -1 if not found.
     */
    int start(int from);

    /**
     * Returns the ending position (exclusive), which is the position after the last character.
     *
     * @param start The found starting position.
     * @return The ending position, or -1 if not found.
     */
    int end(int start);

    /**
     * Resets the finder for object reuse.
     *
     * @return This Finder instance.
     */
    default Finder reset() {
        return this;
    }

}
