package org.aoju.bus.tracer.web.interceptor;

import org.aoju.bus.tracer.TraceUtils;
import org.aoju.bus.tracer.context.TraceContext;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class WebTraceInterceptor extends HandlerInterceptorAdapter implements Ordered {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TraceUtils.trace(request, response);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceContext.clear();
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
