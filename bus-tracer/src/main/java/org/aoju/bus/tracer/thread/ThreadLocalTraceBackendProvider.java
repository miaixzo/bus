package org.aoju.bus.tracer.thread;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.backend.TraceBackendProvider;

public class ThreadLocalTraceBackendProvider implements TraceBackendProvider {

    private final ThreadLocalBackend backend = new ThreadLocalBackend();

    @Override
    public final Backend provideBackend() {
        return backend;
    }

}
