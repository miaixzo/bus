package org.aoju.bus.office.magic;

import java.util.Map;

/**
 * UnoUrl用于处理UNO进程间连接类型和参数.
 * OpenOffice.org 支持两种连接类型:TCP套接字和命名管道.
 * 命名管道稍微快一些，并且不占用TCP端口，但是它们需要本地库，
 * 这意味着设置java.library.path启动Java时路径
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class UnoUrl {

    private final com.sun.star.lib.uno.helper.UnoUrl unoUrl;

    /**
     * 为指定的管道创建UnoUrl.
     *
     * @param pipeName 管道的名称.
     */
    public UnoUrl(final String pipeName) {
        this.unoUrl = pipe(pipeName);
    }

    /**
     * 为指定的端口创建一个UnoUrl.
     *
     * @param port 端口.
     */
    public UnoUrl(final int port) {
        this.unoUrl = socket(port);
    }

    /**
     * 为指定的管道创建一个UnoUrl.
     *
     * @param pipeName 管道的名称.
     * @return 创建的UnoUrl.
     */
    static com.sun.star.lib.uno.helper.UnoUrl pipe(final String pipeName) {
        try {
            return com.sun.star.lib.uno.helper.UnoUrl.parseUnoUrl("pipe,name=" + pipeName + ";urp;StarOffice.ServiceManager");
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 为指定的端口创建一个UnoUrl.
     *
     * @param port 端口.
     * @return 创建的UnoUrl.
     */
    static com.sun.star.lib.uno.helper.UnoUrl socket(final int port) {
        try {
            return com.sun.star.lib.uno.helper.UnoUrl.parseUnoUrl(
                    "socket,host=127.0.0.1,port="
                            + port
                            + ",tcpNoDelay=1;urp;StarOffice.ServiceManager");
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 返回此Uno Url连接的名称。不允许使用编码字符.
     *
     * @return 连接名称为字符串.
     */
    public String getConnection() {
        return unoUrl.getConnection();
    }

    /**
     * 返回此Uno Url的协议名称。不允许使用编码字符.
     *
     * @return 协议名称为字符串.
     */
    public String getProtocol() {
        return unoUrl.getProtocol();
    }

    /**
     * 返回对象名。不允许使用编码字符.
     *
     * @return 对象名称为字符串.
     */
    public String getRootOid() {
        return unoUrl.getRootOid();
    }

    /**
     * 以键/值对映射的形式返回协议参数.
     *
     * @return 具有协议参数的键/值对的映射.
     */
    public Map<String, String> getProtocolParameters() {
        return unoUrl.getProtocolParameters();
    }

    /**
     * 以键/值对映射的形式返回连接参数.
     *
     * @return 具有连接参数的键/值对的映射.
     */
    public Map<String, String> getConnectionParameters() {
        return unoUrl.getConnectionParameters();
    }

    /**
     * 返回协议参数的原始规范.
     *
     * @return 未解释的协议参数为字符串.
     */
    public String getProtocolParametersAsString() {
        return unoUrl.getProtocolParametersAsString();
    }

    /**
     * 返回连接参数的原始规范.
     *
     * @return 未解释的连接参数为字符串.
     */
    public String getConnectionParametersAsString() {
        return unoUrl.getConnectionParametersAsString();
    }

    /**
     * 返回协议名称和参数的原始规范.
     *
     * @return 未解释的协议名称和参数为字符串.
     */
    public String getProtocolAndParametersAsString() {
        return unoUrl.getProtocolAndParametersAsString();
    }

    /**
     * 返回连接名称和参数的原始规范.
     *
     * @return 未解释的连接名和参数为字符串.
     */
    public String getConnectionAndParametersAsString() {
        return unoUrl.getConnectionAndParametersAsString();
    }

}
