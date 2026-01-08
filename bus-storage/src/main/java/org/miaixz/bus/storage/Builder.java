/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.storage;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Builder class for storage service operations, primarily for building object keys.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * Constructs a new AbstractProvider with default settings.
     */
    private Builder() {

    }

    /**
     * Constructs an object key by concatenating a prefix (if present), path, and file name. The prefix and path are
     * normalized to ensure correct formatting.
     *
     * @param prefix   The prefix path, can be null or empty.
     * @param path     The path to the file, can be null or empty.
     * @param fileName The name of the file.
     * @return The normalized and concatenated object key.
     */
    public static String buildObjectKey(String prefix, String path, String fileName) {
        String normalizedPrefix = buildNormalizedPrefix(prefix);
        String normalizedPath = StringKit.isBlank(path) ? Normal.EMPTY
                : path.replaceAll("/{2,}", Symbol.SLASH).replaceAll("^/|/$", Normal.EMPTY);

        if (StringKit.isBlank(normalizedPrefix) && StringKit.isBlank(normalizedPath)) {
            return fileName;
        } else if (StringKit.isBlank(normalizedPrefix)) {
            return normalizedPath + Symbol.SLASH + fileName;
        } else if (StringKit.isBlank(normalizedPath)) {
            return normalizedPrefix + Symbol.SLASH + fileName;
        } else {
            return normalizedPrefix + Symbol.SLASH + normalizedPath + Symbol.SLASH + fileName;
        }
    }

    /**
     * Builds a normalized prefix path. This method removes redundant slashes and leading/trailing slashes from the
     * prefix.
     *
     * @param prefix The original prefix path.
     * @return The normalized prefix path.
     */
    public static String buildNormalizedPrefix(String prefix) {
        return StringKit.isBlank(prefix) ? Normal.EMPTY
                : prefix.replaceAll("/{2,}", Symbol.SLASH).replaceAll("^/|/$", Normal.EMPTY);
    }

}
