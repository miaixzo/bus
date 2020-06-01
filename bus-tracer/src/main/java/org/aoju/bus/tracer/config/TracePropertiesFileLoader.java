/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.tracer.config;

import org.aoju.bus.core.lang.Normal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Kimi Liu
 * @version 5.9.6
 * @since JDK 1.8+
 */
public final class TracePropertiesFileLoader {

    public static final String Trace_PROPERTIES_FILE = Normal.META_DATA_INF + "/tracer/tracer.properties";
    public static final String Trace_DEFAULT_PROPERTIES_FILE = Normal.META_DATA_INF + "/tracer/tracer.default.properties";

    public Properties loadTraceProperties(String TracePropertiesFile) throws IOException {
        final Properties propertiesFromFile = new Properties();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Enumeration<URL> TracePropertyFiles = loader.getResources(TracePropertiesFile);

        while (TracePropertyFiles.hasMoreElements()) {
            final URL url = TracePropertyFiles.nextElement();
            try (InputStream stream = url.openStream()) {
                propertiesFromFile.load(stream);
            }
        }
        return propertiesFromFile;
    }

}
