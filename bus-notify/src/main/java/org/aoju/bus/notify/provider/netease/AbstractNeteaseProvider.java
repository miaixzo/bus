package org.aoju.bus.notify.provider.netease;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.notify.AbstractProvider;
import org.aoju.bus.notify.magic.Message;

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 云信抽象类
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
public abstract class AbstractNeteaseProvider extends AbstractProvider<NeteaseMsgTemplate, NeteaseProperties> {

    /**
     * 发送成功后返回code
     */
    private static final String SUCCESS_RESULT = "200";

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public AbstractNeteaseProvider(NeteaseProperties properties) {
        super(properties);
    }

    private static String encode(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest messageDigest
                    = MessageDigest.getInstance("sha1");
            messageDigest.update(value.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (byte aByte : bytes) {
            buf.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[aByte & 0x0f]);
        }
        return buf.toString();
    }

    protected HashMap<String, String> getPostHeader() {
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        HashMap<String, String> map = new HashMap<>();
        map.put("AppKey", properties.getAppKey());
        map.put("Nonce", properties.getAppNonce());
        map.put("CurTime", curTime);
        map.put("CheckSum", getCheckSum(properties.getAppNonce(), curTime));
        return map;
    }

    public Message post(String routerUrl, Map<String, Object> map) {

        Map<String, String> header = getPostHeader();
        Logger.debug("netease send：{}", map);
        String response = Httpx.post(routerUrl, map, header);
        Logger.debug("netease result：{}", response);
        JSONObject object = JSON.parseObject(response);
        return Message.builder()
                .result(SUCCESS_RESULT.equals(object.getString("code")))
                .desc(object.getString("desc")).build();
    }

    /**
     * CheckSum
     *
     * @param nonce   APP_NONCE
     * @param curTime 时间
     * @return 结果
     */
    private String getCheckSum(String nonce, String curTime) {
        return encode(properties.getAppNonce() + nonce + curTime);
    }
}
