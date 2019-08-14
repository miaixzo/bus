package org.aoju.bus.trace4j.binding.servlet;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.Utilities;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static org.aoju.bus.trace4j.config.TraceFilterConfiguration.Channel.IncomingRequest;

/**
 * Manages the Trace lifecycle.
 */
@WebListener("TraceServletRequestListener to read incoming TPICs into Trace backend")
public final class TraceServletRequestListener implements ServletRequestListener {

    private static final String HTTP_HEADER_NAME = TraceConsts.TPIC_HEADER;

    private final TraceBackend backend;

    private final HttpHeaderTransport transportSerialization;

    protected TraceServletRequestListener(TraceBackend backend, HttpHeaderTransport transportSerialization) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
    }

    public TraceServletRequestListener() {
        this(Trace.getBackend(), new HttpHeaderTransport());
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent sre) {
        backend.clear();
    }

    @Override
    public void requestInitialized(final ServletRequestEvent sre) {
        final ServletRequest servletRequest = sre.getServletRequest();
        if (servletRequest instanceof HttpServletRequest) {
            httpRequestInitialized((HttpServletRequest) servletRequest);
        }
    }

    private void httpRequestInitialized(final HttpServletRequest request) {
        final TraceFilterConfiguration configuration = backend.getConfiguration();

        if (configuration.shouldProcessContext(IncomingRequest)) {
            final Enumeration<String> headers = request.getHeaders(HTTP_HEADER_NAME);

            if (headers != null && headers.hasMoreElements()) {
                final Map<String, String> contextMap = transportSerialization.parse(Collections.list(headers));
                backend.putAll(backend.getConfiguration().filterDeniedParams(contextMap, IncomingRequest));
            }
        }

        Utilities.generateInvocationIdIfNecessary(backend);

        final HttpSession session = request.getSession(false);
        if (session != null) {
            Utilities.generateSessionIdIfNecessary(backend, session.getId());
        }
    }
}
