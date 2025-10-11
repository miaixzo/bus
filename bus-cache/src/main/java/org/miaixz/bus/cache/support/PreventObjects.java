/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cache.support;

import java.io.Serial;
import java.io.Serializable;

/**
 * A utility class for handling cache penetration prevention.
 * <p>
 * Cache penetration occurs when a non-existent key is frequently requested, causing each request to bypass the cache
 * and hit the underlying data source (e.g., a database). This class provides a special singleton object that can be
 * cached to represent a "null" or non-existent value, thereby preventing subsequent requests for the same key from
 * hitting the data source.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PreventObjects {

    /**
     * Returns the singleton object used for cache penetration prevention.
     * <p>
     * This object serves as a placeholder in the cache for keys that correspond to non-existent data.
     * </p>
     *
     * @return The singleton penetration prevention object.
     */
    public static Object getPreventObject() {
        return PreventObject.INSTANCE;
    }

    /**
     * Checks if a given object is the special penetration prevention object.
     *
     * @param object The object to check.
     * @return {@code true} if the object is the penetration prevention instance, otherwise {@code false}.
     */
    public static boolean isPrevent(Object object) {
        return object == PreventObject.INSTANCE || object instanceof PreventObject;
    }

    /**
     * The internal singleton class for the penetration prevention object.
     * <p>
     * It is implemented as a serializable singleton to ensure it can be stored in various distributed cache
     * implementations and that only one instance exists globally.
     * </p>
     */
    private static final class PreventObject implements Serializable {

        /**
         * The serialization version UID.
         */
        @Serial
        private static final long serialVersionUID = 2852290208329L;

        /**
         * The singleton instance.
         */
        private static final PreventObject INSTANCE = new PreventObject();
    }

}
