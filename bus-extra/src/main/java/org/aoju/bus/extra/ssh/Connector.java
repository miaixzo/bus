/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.extra.ssh;

/**
 * 连接者对象,提供一些连接的基本信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Connector {
    private String host;
    private int port;
    private String user;
    private String password;
    private String group;

    public Connector() {
    }

    /**
     * 构造
     *
     * @param user     用户名
     * @param password 密码
     * @param group    组
     */
    public Connector(String user, String password, String group) {
        this.user = user;
        this.password = password;
        this.group = group;
    }

    /**
     * 构造
     *
     * @param host     主机名
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     */
    public Connector(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    /**
     * 获得主机名
     *
     * @return 主机名
     */
    public String getHost() {
        return host;
    }

    /**
     * 设定主机名
     *
     * @param host 主机名
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获得端口号
     *
     * @return 端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 设定端口号
     *
     * @param port 端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获得用户名
     *
     * @return 用户名
     */
    public String getUser() {
        return user;
    }

    /**
     * 设定用户名
     *
     * @param name 用户名
     */
    public void setUser(String name) {
        this.user = name;
    }

    /**
     * 获得密码
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设定密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获得用户组名
     *
     * @return 用户组
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设定用户组名
     *
     * @param group 用户组
     */
    public void setGroup(String group) {
        this.group = group;
    }

}
