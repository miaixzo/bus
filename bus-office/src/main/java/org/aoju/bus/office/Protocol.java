package org.aoju.bus.office;

/**
 * 表示可用于与正在运行的office实例通信的协议.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public enum Protocol {

    /**
     * 表示使用共享内存的命名管道连接类型.
     * 这种类型的进程间连接比套接字连接稍微快一些，
     * 并且只有在两个进程位于同一台机器上时才能工作.
     * 默认情况下,不能在Java上工作，不支持命名管道.
     */
    PIPE,
    /**
     * 表示使用可靠的TCP/IP套接字连接的连接类型.
     */
    SOCKET

}
