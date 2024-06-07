package org.miaixz.bus.notify.metric.qiniu;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
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
public class QiniuSmsProvider extends AbstractProvider<QiniuProperty, Context> {

    public QiniuSmsProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(QiniuProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("template_id", entity.getTemplate());
        bodys.put("parameters", StringKit.split(entity.getParams(), Symbol.COMMA));
        bodys.put("mobiles", entity.getReceive());
        String response = Httpx.post(this.getUrl(entity), bodys);
        int status = JsonKit.getValue(response, "status");

        String errcode = status == 200 ? ErrorCode.SUCCESS.getCode() : ErrorCode.FAILURE.getCode();
        String errmsg = status == 200 ? ErrorCode.SUCCESS.getMsg() : ErrorCode.FAILURE.getMsg();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

}
