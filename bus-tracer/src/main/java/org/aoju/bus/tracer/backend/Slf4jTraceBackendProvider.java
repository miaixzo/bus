package org.aoju.bus.tracer.backend;

import org.aoju.bus.tracer.Backend;

public class Slf4jTraceBackendProvider implements TraceBackendProvider {

    private static final ThreadLocalHashSet<String> Trace_KEYS = new ThreadLocalHashSet<>();

    private final Slf4JAbstractBackend slf4jTraceContext = new Slf4JAbstractBackend(Trace_KEYS);

    @Override
    public final Backend provideBackend() {
        return slf4jTraceContext;
    }
}
