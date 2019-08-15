package org.aoju.bus.trace4j.binding.servlet;

import org.aoju.bus.trace4j.Builder;
import org.aoju.bus.trace4j.Backend;
import org.aoju.bus.trace4j.consts.TraceConsts;
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
 * Manages the Builder lifecycle.
 */
@WebListener("TraceServletRequestListener to read incoming TPICs into Builder backend")
public final class TraceServletRequestListener implements ServletRequestListener {

    private static final String HTTP_HEADER_NAME = TraceConsts.TPIC_HEADER;

    private final Backend backend;

    private final HttpHeaderTransport transportSerialization;

    protected TraceServletRequestListener(Backend backend, HttpHeaderTransport transportSerialization) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
    }

    public TraceServletRequestListener() {
        this(Builder.getBackend(), new HttpHeaderTransport());
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

        Builder.generateInvocationIdIfNecessary(backend);

        final HttpSession session = request.getSession(false);
        if (session != null) {
            Builder.generateSessionIdIfNecessary(backend, session.getId());
        }
    }
}
