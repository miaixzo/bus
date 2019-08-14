package org.aoju.bus.trace4j.binding.servlet;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.Utilities;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Warning: This filter assumes that the HttpSessionListener is executed on the same thread as the request handling.
 * This might not work for every servlet container.
 * It should at least work for the following containers:
 * <ul>
 * <li>Jetty</li>
 * <li>Tomcat</li>
 * </ul>
 */
@WebListener("TraceSessionListener to create sessionIds on session creation and remove it instead from the Trace backend on session termination.")
public class TraceSessionListener implements HttpSessionListener {

    private final TraceBackend backend;

    public TraceSessionListener() {
        this(Trace.getBackend());
    }

    protected TraceSessionListener(TraceBackend backend) {
        this.backend = backend;
    }

    @Override
    public final void sessionCreated(HttpSessionEvent httpSessionEvent) {
        Utilities.generateSessionIdIfNecessary(backend, httpSessionEvent.getSession().getId());
    }

    @Override
    public final void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        backend.remove(TraceConsts.SESSION_ID_KEY);
    }

}
