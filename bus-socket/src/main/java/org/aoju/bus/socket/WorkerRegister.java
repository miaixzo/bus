package org.aoju.bus.socket;

import java.nio.channels.Selector;

/**
 * selector register callback
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
interface WorkerRegister {

    /**
     * selector回调
     *
     * @param selector 用于注册事件的selector
     */
    void callback(Selector selector);

}
