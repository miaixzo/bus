package org.aoju.bus.notify.provider.qiniu;

import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.netease.NeteaseProvider;

/**
 * 七牛云短信
 *
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK1.8+
 */
public class QiniuSmsProvider extends NeteaseProvider<QiniuSmsProperty, Context> {

    public QiniuSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(QiniuSmsProperty entity) {
        return null;
    }

}
