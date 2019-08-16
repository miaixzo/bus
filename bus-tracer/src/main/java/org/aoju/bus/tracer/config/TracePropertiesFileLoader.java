package org.aoju.bus.tracer.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public final class TracePropertiesFileLoader {

    public static final String Trace_PROPERTIES_FILE = "META-INF/tracer.properties";
    public static final String Trace_DEFAULT_PROPERTIES_FILE = "META-INF/tracer.default.properties";

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
