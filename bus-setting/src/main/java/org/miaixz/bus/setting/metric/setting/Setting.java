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
package org.miaixz.bus.setting.metric.setting;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class that provides static methods for accessing {@link org.miaixz.bus.setting.Setting} configuration
 * files, with caching support.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Setting {

    /**
     * A cache for {@code Setting} instances, keyed by file path.
     */
    private static final Map<String, org.miaixz.bus.setting.Setting> CACHE_SETTING = new ConcurrentHashMap<>();

    /**
     * Gets a cached {@code Setting} instance for the given resource name. If the name has no extension,
     * {@code .setting} is assumed. The file is loaded from the classpath.
     *
     * @param name The name of the settings file.
     * @return The cached or newly loaded {@code Setting} instance.
     */
    public static org.miaixz.bus.setting.Setting get(final String name) {
        return CACHE_SETTING.computeIfAbsent(name, (filePath) -> {
            final String extName = FileName.extName(filePath);
            if (StringKit.isEmpty(extName)) {
                filePath = filePath + "." + org.miaixz.bus.setting.Setting.EXT_NAME;
            }
            return new org.miaixz.bus.setting.Setting(filePath, true);
        });
    }

    /**
     * Gets the first {@code Setting} instance that can be successfully loaded from a list of resource names. It tries
     * each name in order until one is found.
     *
     * @param names The resource names to try. If a name has no extension, {@code .setting} is assumed.
     * @return The first found {@code Setting} instance, or null if none are found.
     */
    public static org.miaixz.bus.setting.Setting getFirstFound(final String... names) {
        for (final String name : names) {
            try {
                return get(name);
            } catch (final InternalException e) {
                // Ignore and try the next name
            }
        }
        return null;
    }

}
