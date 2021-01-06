package org.aoju.bus.notify.provider.huawei;

import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.netease.NeteaseProvider;

/**
 * 七牛云短信
 *
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK1.8+
 */
public class HuaweiSmsProvider extends NeteaseProvider<HuaweiSmsProperty, Context> {

    public HuaweiSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(HuaweiSmsProperty entity) {
        return null;
    }

}
