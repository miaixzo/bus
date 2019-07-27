package org.aoju.bus.spring.servlet;

import org.aoju.bus.logger.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author aoju.org
 * @version 3.0.1
 * @Group 839128
 * @since JDK 1.8
 */
@Component
public class RequestLoggingHandler implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String method = request.getMethod();
        if (HttpMethod.POST.matches(method) || HttpMethod.PATCH.matches(method) || HttpMethod.PUT.matches(method)) {
            if (request instanceof BodyCacheHttpServletRequestWrapper) {
                BodyCacheHttpServletRequestWrapper requestWrapper = ((BodyCacheHttpServletRequestWrapper) request);
                Logger.info("[请求] - [{}]", new String(requestWrapper.getBody()));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        final String method = request.getMethod();
        if (HttpMethod.POST.matches(method) || HttpMethod.PATCH.matches(method) || HttpMethod.PUT.matches(method)) {
            if (response instanceof BodyCacheHttpServletResponseWrapper) {
                BodyCacheHttpServletResponseWrapper responseWrapper = ((BodyCacheHttpServletResponseWrapper) response);
                Logger.info("[请求] - [{}]", new String(responseWrapper.getBody()));
            }
        }
    }

}
