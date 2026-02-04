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
