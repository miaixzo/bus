package org.aoju.bus.office.metric;

import java.util.EventListener;

/**
 * office连接事件侦听器
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface OfficeConnectEventListener extends EventListener {

    /**
     * 侦听office是否已经连接.
     *
     * @param event 连接事件信息.
     */
    void connected(OfficeConnectEvent event);

    /**
     * 侦听office是否已经断开连接
     *
     * @param event 连接事件信息.
     */
    void disconnected(OfficeConnectEvent event);

}
