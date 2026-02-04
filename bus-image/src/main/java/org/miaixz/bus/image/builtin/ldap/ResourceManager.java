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
package org.miaixz.bus.image.builtin.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.WeakHashMap;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
final class ResourceManager {

    private static final String APP_RESOURCE_FILE_NAME = "ldap.properties";

    private static final WeakHashMap<ClassLoader, Properties> propertiesCache = new WeakHashMap<>(11);

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static InputStream getResourceAsStream(final ClassLoader cl, final String name) {
        return cl.getResourceAsStream(name);
    }

    public static Properties getInitialEnvironment() throws InternalException {
        ClassLoader cl = getContextClassLoader();
        synchronized (propertiesCache) {
            Properties props = propertiesCache.get(cl);
            if (props != null)
                return props;

            props = new Properties();
            InputStream is = getResourceAsStream(cl, APP_RESOURCE_FILE_NAME);
            if (is == null) {
                throw new InternalException("Failed to access resource: " + APP_RESOURCE_FILE_NAME);
            }
            try {
                props.load(is);
            } catch (IOException e) {
                throw new InternalException("Failed to parse resource: " + APP_RESOURCE_FILE_NAME);
            } finally {
                IoKit.close(is);
            }
            propertiesCache.put(cl, props);
            return props;
        }
    }

}
