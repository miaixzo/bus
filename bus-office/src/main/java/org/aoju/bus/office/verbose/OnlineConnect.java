package org.aoju.bus.office.verbose;

import org.aoju.bus.http.HttpClient;
import org.aoju.bus.office.metric.RequestConfig;

/**
 * 保存与LibreOffice在线服务器通信的请求配置.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class OnlineConnect implements OnlineContext {

    private final HttpClient httpClient;
    private final RequestConfig requestConfig;

    /**
     * 使用指定的客户端和URL构造新连接.
     *
     * @param httpClient    用于与LibreOffice在线服务器通信的HTTP客户机(已初始化).
     * @param requestConfig 转换的请求配置.
     */
    public OnlineConnect(final HttpClient httpClient, final RequestConfig requestConfig) {

        this.httpClient = httpClient;
        this.requestConfig = requestConfig;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

}
