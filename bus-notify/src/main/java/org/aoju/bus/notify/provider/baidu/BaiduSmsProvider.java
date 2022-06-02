package org.aoju.bus.notify.provider.baidu;

import org.aoju.bus.extra.json.JsonKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.notify.Builder;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.provider.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 七牛云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaiduSmsProvider extends AbstractProvider<BaiduProperty, Context> {

    public BaiduSmsProvider(Context properties) {
        super(properties);
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
                .errcode("200".equals(errcode) ? Builder.ErrorCode.SUCCESS.getCode() : errcode)
                .errmsg(JsonKit.getValue(response, "errmsg"))
                .build();
    }

}
