package org.miaixz.bus.notify.provider.jdcloud;

import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.MediaType;
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
 * 京东云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdcloudSmsProvider extends AbstractProvider<JdcloudProperty, Context> {

    public JdcloudSmsProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(JdcloudProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("regionId", entity.getEndpoint());
        bodys.put("templateId", entity.getTemplate());
        bodys.put("params", StringKit.split(entity.getParams(), Symbol.COMMA));
        bodys.put("phoneList", entity.getReceive());
        bodys.put("signId", entity.getSignature());

        Map<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(entity.getUrl(), bodys, headers);
        int status = JsonKit.getValue(response, "statusCode");

        String errcode = status == Http.HTTP_OK ? Builder.ErrorCode.SUCCESS.getCode() : Builder.ErrorCode.FAILURE.getCode();
        String errmsg = status == Http.HTTP_OK ? Builder.ErrorCode.SUCCESS.getMsg() : Builder.ErrorCode.FAILURE.getMsg();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

}
