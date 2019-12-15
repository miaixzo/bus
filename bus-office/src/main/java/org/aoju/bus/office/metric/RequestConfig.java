package org.aoju.bus.office.metric;

/**
 * LibreOffice联机通信的请求配置.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class RequestConfig {

    private final String url;
    private final int connectTimeout;
    private final int socketTimeout;

    /**
     * 使用指定的参数构造新配置.
     *
     * @param url            转换的URL.
     * @param connectTimeout 超时时间(毫秒)，直到建立连接为止。0的超时值被解释为无限超时。负值被解释为未定义(系统默认值).
     * @param socketTimeout  套接字超时({@code SO_TIMEOUT})，以毫秒为单位，是等待数据的超时，换句话说，
     *                       是两个连续数据包之间的最大不活动周期)。0的超时值被解释为无限超时。
     *                       负值被解释为未定义(系统默认值).
     */
    public RequestConfig(final String url, final int connectTimeout, final int socketTimeout) {
        this.url = url;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    /**
     * 获取可发送转换请求的URL.
     *
     * @return 发送转换请求的URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取超时时间(以毫秒为单位)，直到建立连接为止。0的超时值被解释为无限超时.
     * 默认值: {@code -1}
     *
     * @return 连接超时时间.
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 获取套接字超时({@code SO_TIMEOUT})，以毫秒为单位，
     * 这是等待数据的超时，换句话说，是两个连续数据包之间的最大不活动周期).
     * 默认值: {@code -1}
     *
     * @return socket 超时时间.
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

}
