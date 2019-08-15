package org.aoju.bus.trace4j.backend;

import org.aoju.bus.trace4j.Backend;

public interface TraceBackendProvider {

    Backend provideBackend();

}
