package org.aoju.bus.trace4j.binding.spring.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class TraceResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private TraceInterceptor interceptor;

    public TraceResponseBodyAdvice() {
    }

    TraceResponseBodyAdvice(TraceInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest springHttpRequest, ServerHttpResponse springHttpResponse) {
        if (springHttpRequest instanceof ServletServerHttpRequest && springHttpResponse instanceof ServletServerHttpResponse) {
            final HttpServletRequest request = ((ServletServerHttpRequest) springHttpRequest).getServletRequest();
            final HttpServletResponse response = ((ServletServerHttpResponse) springHttpResponse).getServletResponse();

            interceptor.afterCompletion(request, response, null, null);
        }
        return body;
    }

}
