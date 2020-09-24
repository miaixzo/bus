package org.aoju.bus.notify.provider.baidu;

import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.netease.NeteaseProvider;

/**
 * 七牛云短信
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK1.8+
 */
public class BaiduSmsProvider extends NeteaseProvider<BaiduSmsProperty, Context> {

    public BaiduSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(BaiduSmsProperty entity) {
        return null;
    }

}
