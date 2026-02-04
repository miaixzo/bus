/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
