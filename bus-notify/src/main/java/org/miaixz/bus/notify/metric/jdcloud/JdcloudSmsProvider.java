package org.miaixz.bus.notify.metric.jdcloud;

import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.MediaType;
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
 * 京东云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdcloudSmsProvider extends AbstractProvider<JdcloudProperty, Context> {

    public JdcloudSmsProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(JdcloudProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("regionId", this.getUrl(entity));
        bodys.put("templateId", entity.getTemplate());
        bodys.put("params", StringKit.split(entity.getParams(), Symbol.COMMA));
        bodys.put("phoneList", entity.getReceive());
        bodys.put("signId", entity.getSignature());

        Map<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(this.getUrl(entity), bodys, headers);
        int status = JsonKit.getValue(response, "statusCode");

        String errcode = status == Http.HTTP_OK ? ErrorCode.SUCCESS.getCode() : ErrorCode.FAILURE.getCode();
        String errmsg = status == Http.HTTP_OK ? ErrorCode.SUCCESS.getMsg() : ErrorCode.FAILURE.getMsg();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

}
