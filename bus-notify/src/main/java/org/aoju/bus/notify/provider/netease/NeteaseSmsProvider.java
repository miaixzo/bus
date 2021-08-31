package org.aoju.bus.notify.provider.netease;

import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * 网易云短信消息
 *
 * @author Kimi Liu
 * @version 6.2.8
 * @since JDK1.8+
 */
public class NeteaseSmsProvider extends NeteaseProvider<NeteaseSmsProperty, Context> {

    /**
     * 请求路径URL
     */
    private static final String NETEASE_SMS_API = "https://api.netease.im/sms/sendtemplate.action";

    public NeteaseSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(NeteaseSmsProperty entity) {
        Map<String, Object> params = new HashMap<>();
        params.put("templateid", entity.getTemplateId());
        params.put("mobiles", entity.getReceive());
        params.put("params", JsonKit.toJsonString(entity.getParams()));
        return post(NETEASE_SMS_API, params);
    }

}
