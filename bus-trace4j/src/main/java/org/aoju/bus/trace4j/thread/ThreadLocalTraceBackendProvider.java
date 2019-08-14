package org.aoju.bus.trace4j.thread;

import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.backend.TraceBackendProvider;

public class ThreadLocalTraceBackendProvider implements TraceBackendProvider {

    private final ThreadLocalTraceBackend backend = new ThreadLocalTraceBackend();

    @Override
    public final TraceBackend provideBackend() {
        return backend;
    }

}
