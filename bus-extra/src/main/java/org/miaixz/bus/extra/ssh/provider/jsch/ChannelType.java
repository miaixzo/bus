/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.ssh.provider.jsch;

/**
 * Jsch支持的Channel类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ChannelType {
    /**
     * Session
     */
    SESSION("session"),
    /**
     * shell
     */
    SHELL("shell"),
    /**
     * exec
     */
    EXEC("exec"),
    /**
     * x11
     */
    X11("x11"),
    /**
     * agent forwarding
     */
    AGENT_FORWARDING("auth-agent@openssh.com"),
    /**
     * direct tcpip
     */
    DIRECT_TCPIP("direct-tcpip"),
    /**
     * forwarded tcpip
     */
    FORWARDED_TCPIP("forwarded-tcpip"),
    /**
     * sftp
     */
    SFTP("sftp"),
    /**
     * subsystem
     */
    SUBSYSTEM("subsystem");

    /**
     * channel值
     */
    private final String value;

    /**
     * 构造
     *
     * @param value 类型值
     */
    ChannelType(final String value) {
        this.value = value;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public String getValue() {
        return this.value;
    }
}
