package org.aoju.bus.tracer.handler;


import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.health.HealthUtils;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.context.TraceContext;
import org.aoju.bus.tracer.thread.TraceThread;
import org.slf4j.MDC;
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
        TraceContext traceContext = TraceThread.getTraceThreadData(Thread.currentThread());
        if (ObjectUtils.isEmpty(traceContext)) {
            traceContext.setX_trace_id("" + DateUtils.timestamp());
            traceContext.setX_span_id(String.valueOf(Thread.currentThread().getId()));
            traceContext.setX_child_Id("" + DateUtils.timestamp());
            traceContext.setX_local_ip(HealthUtils.getHostInfo().getAddress());
            traceContext.setX_remote_ip(getIpAddress(request));
            TraceThread.set(Thread.currentThread(), traceContext);
        }

        MDC.put(TraceConsts.X_TRACE_ID, traceContext.getX_trace_id());
        MDC.put(TraceConsts.X_SPAN_ID, traceContext.getX_span_id());
        MDC.put(TraceConsts.X_CHILD_ID, traceContext.getX_child_Id());
        MDC.put(TraceConsts.X_LOCAL_IP, traceContext.getX_local_ip());
        MDC.put(TraceConsts.X_REMOTE_IP, traceContext.getX_remote_ip());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    /**
     * 获取用户真实IP地址，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，
     * 取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     * <p>
     * 用户真实IP为： 192.168.1.110
     *
     * @param request
     * @return ip
     */
    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } else if (ip.length() > 15) {
            String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = ips[index];
                if (!("unknown".equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
        return ip;
    }

}
