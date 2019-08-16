package org.aoju.bus.tracer.backend;

import org.aoju.bus.tracer.Backend;

public interface TraceBackendProvider {

    Backend provideBackend();

}
