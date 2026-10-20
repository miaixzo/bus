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
package org.miaixz.bus.image.builtin.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.WeakHashMap;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ResourceKit;

/**
 * Represents the ResourceManager type.
 *
 * @author Kimi Liu
 */
final class ResourceManager {

    /**
     * The app resource file name value.
     */
    private static final String APP_RESOURCE_FILE_NAME = "ldap.properties";

    /**
     * The properties cache value.
     */
    private static final WeakHashMap<ClassLoader, Properties> propertiesCache = new WeakHashMap<>(11);

    /**
     * Gets the context class loader.
     *
     * @return the context class loader.
     */
    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Gets the initial environment.
     *
     * @return the initial environment.
     * @throws InternalException if the operation cannot be completed.
     */
    public static Properties getInitialEnvironment() throws InternalException {
        ClassLoader cl = getContextClassLoader();
        synchronized (propertiesCache) {
            Properties props = propertiesCache.get(cl);
            if (props != null)
                return props;

            props = new Properties();
            URL url = ResourceKit.getResourceUrl(APP_RESOURCE_FILE_NAME, ResourceManager.class);
            if (url == null) {
                throw new InternalException("Failed to access resource: " + APP_RESOURCE_FILE_NAME);
            }
            try (InputStream is = url.openStream()) {
                props.load(is);
            } catch (IOException e) {
                throw new InternalException("Failed to parse resource: " + APP_RESOURCE_FILE_NAME);
            }
            propertiesCache.put(cl, props);
            return props;
        }
    }

}
