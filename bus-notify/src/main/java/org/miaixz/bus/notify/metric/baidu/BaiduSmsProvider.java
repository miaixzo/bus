package org.miaixz.bus.notify.metric.baidu;

import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Message;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 七牛云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaiduSmsProvider extends AbstractProvider<BaiduProperty, Context> {

    public BaiduSmsProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(BaiduProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("mobile", entity.getReceive());
        bodys.put("template", entity.getTemplate());
        bodys.put("signatureId", entity.getSignature());
        bodys.put("contentVar", entity.getParams());
        String response = Httpx.post(entity.getUrl(), bodys);
        String errcode = JsonKit.getValue(response, "errcode");
        return Message.builder()
                .errcode("200".equals(errcode) ? ErrorCode.SUCCESS.getCode() : errcode)
                .errmsg(JsonKit.getValue(response, "errmsg"))
                .build();
    }

}
