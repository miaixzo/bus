package org.miaixz.bus.notify.metric.netease;

import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * 网易云短信消息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NeteaseSmsProvider extends NeteaseProvider<NeteaseProperty, Context> {

    public NeteaseSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(NeteaseProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("templateid", entity.getTemplate());
        bodys.put("mobiles", entity.getReceive());
        bodys.put("params", JsonKit.toJsonString(entity.getParams()));
        return post(this.getUrl(entity), bodys);
    }

}
