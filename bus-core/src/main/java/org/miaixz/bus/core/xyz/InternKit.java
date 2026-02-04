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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.lang.intern.Intern;
import org.miaixz.bus.core.lang.intern.StringIntern;
import org.miaixz.bus.core.lang.intern.WeakIntern;

/**
 * Utility class for creating canonical object generators (Interners).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InternKit {

    /**
     * Constructs a new InternKit. Utility class constructor for static access.
     */
    private InternKit() {
    }

    /**
     * Creates an interner implemented with `WeakHashMap`.
     *
     * @param <T> The type of the object to intern.
     * @return A new {@link Intern} instance.
     */
    public static <T> Intern<T> ofWeak() {
        return new WeakIntern<>();
    }

    /**
     * Creates an interner that uses the default JDK `String.intern()` method.
     *
     * @return A new {@link Intern} instance for strings.
     * @see String#intern()
     */
    public static Intern<String> ofString() {
        return new StringIntern();
    }

    /**
     * Creates a string interner.
     *
     * @param isWeak If `true`, creates an interner implemented with `WeakHashMap`.
     * @return A new {@link Intern} instance.
     */
    public static Intern<String> of(final boolean isWeak) {
        return isWeak ? ofWeak() : ofString();
    }

}
