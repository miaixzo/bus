package org.aoju.bus.office.metric;

import org.aoju.bus.office.verbose.LocalConnect;

import java.util.EventObject;

/**
 * office连接打开或关闭时引发的事件.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class OfficeConnectEvent extends EventObject {

    /**
     * 为指定的连接构造新事件.
     *
     * @param source 事件最初发生时所在的连接.
     */
    public OfficeConnectEvent(final LocalConnect source) {
        super(source);
    }

}
