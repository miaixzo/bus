package org.aoju.bus.trace4j.backend;

import org.aoju.bus.trace4j.ThreadLocalHashSet;
import org.aoju.bus.trace4j.TraceBackend;

public class Slf4jTraceBackendProvider implements TraceBackendProvider {

    private static final ThreadLocalHashSet<String> Trace_KEYS = new ThreadLocalHashSet<>();

    private final Slf4JTraceAbstractBackend slf4jTraceContext = new Slf4JTraceAbstractBackend(Trace_KEYS);

    @Override
    public final TraceBackend provideBackend() {
        return slf4jTraceContext;
    }
}
