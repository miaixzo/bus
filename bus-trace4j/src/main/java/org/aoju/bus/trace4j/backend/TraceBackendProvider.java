package org.aoju.bus.trace4j.backend;

import org.aoju.bus.trace4j.TraceBackend;

public interface TraceBackendProvider {

    TraceBackend provideBackend();

}
