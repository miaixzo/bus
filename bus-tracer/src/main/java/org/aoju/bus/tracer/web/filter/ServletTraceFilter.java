package org.aoju.bus.tracer.web.filter;


import org.aoju.bus.tracer.TraceUtils;
import org.aoju.bus.tracer.context.TraceContext;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Component
public class ServletTraceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        TraceUtils.trace(request, response);
        try {
            chain.doFilter(request, response);
        } finally {
            TraceContext.clear();
        }
    }

    @Override
    public void destroy() {

    }

}
