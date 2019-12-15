package org.aoju.bus.office.verbose;

import org.aoju.bus.http.HttpClient;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.metric.RequestConfig;

/**
 * 表示用于在线转换的office环境.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface OnlineContext extends Context {

    /**
     * 获取负责向office服务器执行请求的HTTP客户端.
     *
     * @return 将发送转换请求的客户端.
     */
    HttpClient getHttpClient();

    /**
     * 获取请求配置.
     *
     * @return 请求配置.
     */
    RequestConfig getRequestConfig();

}
