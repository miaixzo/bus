package org.aoju.bus.extra.ssh;

/**
 * Jsch支持的Channel类型
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
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
    private String value;

    /**
     * 构造
     *
     * @param value 类型值
     */
    ChannelType(String value) {
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
