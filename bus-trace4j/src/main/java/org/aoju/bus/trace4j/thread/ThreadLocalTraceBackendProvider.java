package org.aoju.bus.trace4j.thread;

import org.aoju.bus.trace4j.Backend;
import org.aoju.bus.trace4j.backend.TraceBackendProvider;

public class ThreadLocalTraceBackendProvider implements TraceBackendProvider {

    private final ThreadLocalBackend backend = new ThreadLocalBackend();

    @Override
    public final Backend provideBackend() {
        return backend;
    }

}
