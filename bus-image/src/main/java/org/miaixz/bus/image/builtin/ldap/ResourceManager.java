/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.builtin.ldap;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
final class ResourceManager {

    private static final String APP_RESOURCE_FILE_NAME = "ldap.properties";

    private static final WeakHashMap<ClassLoader, Properties> propertiesCache =
            new WeakHashMap<>(11);

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static InputStream getResourceAsStream(
            final ClassLoader cl, final String name) {
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
                throw new InternalException(
                        "Failed to access resource: " + APP_RESOURCE_FILE_NAME);
            }
            try {
                props.load(is);
            } catch (IOException e) {
                throw new InternalException(
                        "Failed to parse resource: " + APP_RESOURCE_FILE_NAME);
            } finally {
                IoKit.close(is);
            }
            propertiesCache.put(cl, props);
            return props;
        }
    }

}
