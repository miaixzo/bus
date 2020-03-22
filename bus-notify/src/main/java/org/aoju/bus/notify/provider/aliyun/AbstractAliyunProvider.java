package org.aoju.bus.notify.provider.aliyun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.notify.AbstractProvider;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.metric.Properties;
import org.aoju.bus.notify.metric.Template;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 阿里云抽象类提供者
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
public class AbstractAliyunProvider<T extends Template, K extends Properties> extends AbstractProvider<T, K> {

    /**
     * 发送成功后返回code
     */
    private static final String SUCCESS_RESULT = "OK";

    public AbstractAliyunProvider(K properties) {
        super(properties);
    }

    /**
     * pop编码
     *
     * @param value 原值
     * @return 编码值
     */
    protected String specialUrlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException("aliyun specialUrlEncode error");
        }
    }

    /**
     * 构造签名
     *
     * @param params    参数
     * @param appSecret 密钥
     * @return 签名值
     */
    protected String getSign(Map<String, String> params, String appSecret) {
        // 4. 参数KEY排序
        TreeMap<String, String> sortParas = new TreeMap<>(params);
        // 5. 构造待签名的字符串
        Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp
                    .append(Symbol.AND)
                    .append(specialUrlEncode(key))
                    .append(Symbol.EQUAL)
                    .append(specialUrlEncode(params.get(key)));
        }
        // 去除第一个多余的&符号
        String sortedQueryString = sortQueryStringTmp.substring(1);
        String stringToSign = "GET" + Symbol.AND +
                specialUrlEncode(Symbol.SLASH) + Symbol.AND +
                specialUrlEncode(sortedQueryString);
        return sign(stringToSign);
    }

    /**
     * 密钥签名
     *
     * @param stringToSign 代签名字符串
     * @return 签名后字符串
     */
    protected String sign(String stringToSign) {

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec((properties.getAppSecret() + Symbol.AND).getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return new sun.misc.BASE64Encoder().encode(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new InstrumentException("aliyun specialUrlEncode error");
        }
    }

    protected Message checkResponse(String response) {
        JSONObject object = JSON.parseObject(response);
        return Message.builder()
                .result(SUCCESS_RESULT.equals(object.getString("code")))
                .desc(object.getString("code")).build();
    }
}
