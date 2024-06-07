package org.miaixz.bus.notify.metric.jpush;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Message;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 极光短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JpushSmsProvider extends AbstractProvider<JpushProperty, Context> {

    public JpushSmsProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(JpushProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("sign_id", entity.getSignature());
        bodys.put("mobile", entity.getReceive());
        bodys.put("temp_id", entity.getTemplate());
        bodys.put("temp_para", entity.getParams());

        Map<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Header.AUTHORIZATION, "Basic " + getSign());

        String response = Httpx.post(this.getUrl(entity), bodys, headers);
        boolean succeed = Objects.equals(JsonKit.getValue(response, "success_count"), 0);
        String errcode = succeed ? ErrorCode.SUCCESS.getCode() : ErrorCode.FAILURE.getCode();
        String errmsg = succeed ? ErrorCode.SUCCESS.getMsg() : ErrorCode.FAILURE.getMsg();

        return Message.builder()
                .errcode(errcode)
                .errmsg(errmsg)
                .build();
    }

    private String getSign() {
        String origin = context.getAppKey() + Symbol.COLON + context.getAppSecret();
        return Base64.getEncoder().encodeToString(origin.getBytes(Charset.UTF_8));
    }

}
