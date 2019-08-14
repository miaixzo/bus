package org.aoju.bus.trace4j.binding.servlet;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@WebFilter(filterName = "TraceFilter", urlPatterns = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class TraceFilter implements Filter {

    public static final String PROFILE_INIT_PARAM = "profile";

    private static final String HTTP_HEADER_NAME = TraceConsts.TPIC_HEADER;
    private final TraceBackend backend;
    private final HttpHeaderTransport transportSerialization;
    private String profile = TraceConsts.DEFAULT;

    public TraceFilter() {
        this(Trace.getBackend(), new HttpHeaderTransport());
    }

    TraceFilter(TraceBackend backend, HttpHeaderTransport transportSerialization) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
    }

    @Override
    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            doFilterHttp((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    final void doFilterHttp(final HttpServletRequest request, final HttpServletResponse response,
                            final FilterChain filterChain) throws IOException, ServletException {

        final TraceFilterConfiguration configuration = backend.getConfiguration(profile);

        try {
            writeContextToResponse(response, configuration);
            filterChain.doFilter(request, response);
        } finally {
            if (!response.isCommitted()) {
                writeContextToResponse(response, configuration);
            }
        }
    }

    private void writeContextToResponse(final HttpServletResponse response, final TraceFilterConfiguration configuration) {
        if (!backend.isEmpty() && configuration.shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingResponse)) {
            final Map<String, String> filteredContext = backend.getConfiguration(profile).filterDeniedParams(backend.copyToMap(), TraceFilterConfiguration.Channel.OutgoingResponse);
            response.setHeader(HTTP_HEADER_NAME, transportSerialization.render(filteredContext));
        }
    }

    @Override
    public final void init(final FilterConfig filterConfig) {
        final String profileInitParameter = filterConfig.getInitParameter(PROFILE_INIT_PARAM);
        if (profileInitParameter != null) {
            profile = profileInitParameter;
        }
    }

    @Override
    public final void destroy() {
    }

}
