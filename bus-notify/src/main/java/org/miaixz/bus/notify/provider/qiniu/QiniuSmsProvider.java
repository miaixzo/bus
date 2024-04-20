package org.miaixz.bus.notify.provider.qiniu;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Builder;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.Message;
import org.miaixz.bus.notify.provider.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 七牛云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QiniuSmsProvider extends AbstractProvider<QiniuProperty, Context> {

    public QiniuSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(QiniuProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("template_id", entity.getTemplate());
        bodys.put("parameters", StringKit.split(entity.getParams(), Symbol.COMMA));
        bodys.put("mobiles", entity.getReceive());
        String response = Httpx.post(entity.getUrl(), bodys);
        int status = JsonKit.getValue(response, "status");

        String errcode = status == 200 ? Builder.ErrorCode.SUCCESS.getCode() : Builder.ErrorCode.FAILURE.getCode();
        String errmsg = status == 200 ? Builder.ErrorCode.SUCCESS.getMsg() : Builder.ErrorCode.FAILURE.getMsg();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

}
