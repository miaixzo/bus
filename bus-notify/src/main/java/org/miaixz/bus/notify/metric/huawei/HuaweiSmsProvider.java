package org.miaixz.bus.notify.metric.huawei;

import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Message;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.security.MessageDigest;
import java.util.*;

/**
 * 七牛云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HuaweiSmsProvider extends AbstractProvider<HuaweiProperty, Context> {

    /**
     * 成功代码.
     */
    public static final String SUCCESS_CODE = "000000";
    /**
     * 无需修改,用于格式化鉴权头域,给"X-WSSE"参数赋值.
     */
    private static final String WSSE_HEADER_FORMAT = "UsernameToken Username=\"%s\",PasswordDigest=\"%s\",Nonce=\"%s\",Created=\"%s\"";

    /**
     * 无需修改,用于格式化鉴权头域,给"Authorization"参数赋值.
     */
    private static final String AUTH_HEADER_VALUE = "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"";

    public HuaweiSmsProvider(Context properties) {
        super(properties);
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    @Override
    public Message send(HuaweiProperty entity) {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("from", entity.getSender());
        bodys.put("to", entity.getReceive());
        bodys.put("templateId", entity.getTemplate());
        bodys.put("templateParas", entity.getParams());
        bodys.put("signature", entity.getSignature());

        Map<String, String> headers = new HashMap<>();
        headers.put(Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(Header.AUTHORIZATION, AUTH_HEADER_VALUE);
        headers.put("X-WSSE", buildWsseHeader());

        String response = Httpx.post(entity.getUrl(), bodys, headers);
        String errcode = JsonKit.getValue(response, "code");
        return Message.builder()
                .errcode(SUCCESS_CODE.equals(errcode) ? ErrorCode.SUCCESS.getCode() : errcode)
                .errmsg(JsonKit.getValue(response, "description"))
                .build();
    }

    /**
     * 构造X-WSSE参数值.
     *
     * @return X-WSSE参数值
     */
    private String buildWsseHeader() {
        try {
            String time = DateKit.format(new Date(), Fields.UTC);
            String nonce = UUID.randomUUID().toString().replace(Symbol.MINUS, "");
            String text = nonce + time + context.getAppSecret();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes(Charset.UTF_8));
            String hexDigest = byte2Hex(digest.digest());
            String passwordDigestBase64Str = Base64.getEncoder().encodeToString(hexDigest.getBytes());
            return String.format(WSSE_HEADER_FORMAT, context.getAppKey(), passwordDigestBase64Str, nonce, time);
        } catch (Exception e) {
            throw new InternalException(e.getLocalizedMessage(), e);
        }
    }

}
