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
 * Enumeration of channel types supported by JSch (Java Secure Channel). Each enum constant corresponds to a specific
 * type of SSH channel that can be opened.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ChannelType {

    /**
     * Session channel type, a general-purpose channel.
     */
    SESSION("session"),
    /**
     * Shell channel type, used for interactive shell sessions.
     */
    SHELL("shell"),
    /**
     * Exec channel type, used for executing a single remote command.
     */
    EXEC("exec"),
    /**
     * X11 forwarding channel type, used for forwarding X11 graphical user interface sessions.
     */
    X11("x11"),
    /**
     * Agent forwarding channel type, used for forwarding authentication agent connections.
     */
    AGENT_FORWARDING("auth-agent@openssh.com"),
    /**
     * Direct TCP/IP forwarding channel type, used for local port forwarding.
     */
    DIRECT_TCPIP("direct-tcpip"),
    /**
     * Forwarded TCP/IP channel type, used for remote port forwarding.
     */
    FORWARDED_TCPIP("forwarded-tcpip"),
    /**
     * SFTP channel type, used for Secure File Transfer Protocol sessions.
     */
    SFTP("sftp"),
    /**
     * Subsystem channel type, used for accessing predefined subsystems on the server (e.g., sftp).
     */
    SUBSYSTEM("subsystem");

    /**
     * The string value representing the channel type, as used by the JSch library.
     */
    private final String value;

    /**
     * Constructs a {@code ChannelType} with the specified string value.
     *
     * @param value The string representation of the channel type.
     */
    ChannelType(final String value) {
        this.value = value;
    }

    /**
     * Retrieves the string value of the channel type. This value is used when opening a channel with JSch's
     * {@code openChannel} method.
     *
     * @return The string value of the channel type.
     */
    public String getValue() {
        return this.value;
    }

}
