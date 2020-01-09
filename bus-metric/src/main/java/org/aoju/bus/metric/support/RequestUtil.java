/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.metric.support;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.IoUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public class RequestUtil {

    private static final String UNKOWN = "unknown";
    private static final String LOCAL_IP = "127.0.0.1";
    private static final int IP_LEN = 15;

    public static String getText(HttpServletRequest request) throws Exception {
        return IoUtils.toString(request.getInputStream(), Charset.DEFAULT_UTF_8);
    }


    /**
     * request中的参数转换成map
     *
     * @param request request对象
     * @return 返回参数键值对
     */
    public static Map<String, Object> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap == null || paramMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> retMap = new HashMap<>(paramMap.size());

        Set<Entry<String, String[]>> entrySet = paramMap.entrySet();

        for (Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values.length == 1) {
                retMap.put(name, values[0]);
            } else if (values.length > 1) {
                retMap.put(name, values);
            } else {
                retMap.put(name, "");
            }
        }

        return retMap;
    }

    /**
     * 获取客户端真实IP
     *
     * @param request request对象
     * @return 返回ip
     */
    public static String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || UNKOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || UNKOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || UNKOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (LOCAL_IP.equals(ipAddress)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    // ignore
                }
            }

        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > IP_LEN) {
            if (ipAddress.indexOf(Symbol.COMMA) > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(Symbol.COMMA));
            }
        }
        return ipAddress;

    }

    /**
     * 是否是get请求
     *
     * @param request request对象
     * @return true，是
     */
    public static boolean isGetRequest(HttpServletRequest request) {
        return Http.GET.equalsIgnoreCase(request.getMethod());
    }

}
