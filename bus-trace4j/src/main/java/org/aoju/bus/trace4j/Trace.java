package org.aoju.bus.trace4j;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.trace4j.backend.TraceBackendProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class Trace {

    public static TraceBackend getBackend() {
        return getBackend(new BackendProviderResolver());
    }

    protected static TraceBackend getBackend(final BackendProviderResolver resolver) {
        final Set<TraceBackendProvider> backendProviders;
        try {
            backendProviders = resolver.getBackendProviders();
        } catch (RuntimeException e) {
            throw new InstrumentException("Unable to load available backend providers", e);
        }
        if (backendProviders.isEmpty()) {
            final Set<TraceBackendProvider> defaultProvider = resolver.getDefaultTraceBackendProvider();
            if (defaultProvider.isEmpty()) {
                throw new InstrumentException("Unable to find a Trace backend provider. Make sure that you have " +
                        "Trace-core (for slf4j) or any other backend implementation on the classpath.");
            }
            return defaultProvider.iterator().next().provideBackend();
        }
        if (backendProviders.size() > 1) {
            final List<Class<?>> providerClasses = new ArrayList<>(backendProviders.size());
            for (TraceBackendProvider backendProvider : backendProviders) {
                providerClasses.add(backendProvider.getClass());
            }
            final String providerClassNames = Arrays.toString(providerClasses.toArray());
            throw new InstrumentException("Multiple Trace backend providers found. Don't know which one of the following to use: "
                    + providerClassNames);
        }
        return backendProviders.iterator().next().provideBackend();
    }

}
