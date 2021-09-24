package org.aoju.bus.notify.provider.tencent;

import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.netease.NeteaseProvider;

/**
 * 腾讯云短信
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK1.8+
 */
public class QCloudSmsProvider extends NeteaseProvider<QCloudSmsProperty, Context> {

    public QCloudSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(QCloudSmsProperty entity) {
        return null;
    }

}
