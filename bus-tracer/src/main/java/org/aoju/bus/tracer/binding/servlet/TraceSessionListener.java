package org.aoju.bus.tracer.binding.servlet;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.consts.TraceConsts;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Warning: This boot assumes that the HttpSessionListener is executed on the same thread as the request handling.
 * This might not work for every servlet container.
 * It should at least work for the following containers:
 * <ul>
 * <li>Jetty</li>
 * <li>Tomcat</li>
 * </ul>
 */
@WebListener("TraceSessionListener to create sessionIds on session creation and remove it instead from the Builder backend on session termination.")
public class TraceSessionListener implements HttpSessionListener {

    private final Backend backend;

    public TraceSessionListener() {
        this(Builder.getBackend());
    }

    protected TraceSessionListener(Backend backend) {
        this.backend = backend;
    }

    @Override
    public final void sessionCreated(HttpSessionEvent httpSessionEvent) {
        Builder.generateSessionIdIfNecessary(backend, httpSessionEvent.getSession().getId());
    }

    @Override
    public final void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        backend.remove(TraceConsts.SESSION_ID_KEY);
    }

}
