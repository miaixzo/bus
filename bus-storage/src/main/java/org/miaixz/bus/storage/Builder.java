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
package org.miaixz.bus.storage;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Builder class for storage service operations, primarily for building object keys.
 *
 * @author Kimi Liu
 * @since Java 21+
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
