package org.aoju.bus.notify.provider.aliyun;

import org.aoju.bus.http.Httpx;
import org.aoju.bus.notify.magic.Message;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云短信
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
public class AliyunSmsProvider extends AbstractAliyunProvider<AliyunSmsTemplate, AliyunSmsProperties> {

    /**
     * 阿里云短信产品域名
     */
    private static final String ALIYUN_PRODUCT_DOMAIN = "dysmsapi.aliyuncs.com";

    public AliyunSmsProvider(AliyunSmsProperties properties) {
        super(properties);
    }

    @Override
    public Message send(AliyunSmsTemplate template, Map<String, String> context) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        // 这里一定要设置GMT时区
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        Map<String, String> paras = new HashMap<>();
        // 1. 系统参数
        paras.put("SignatureMethod", "HMAC-SHA1");
        paras.put("SignatureNonce", UUID.randomUUID().toString());
        paras.put("AccessKeyId", properties.getAppKey());
        paras.put("SignatureVersion", "1.0");
        paras.put("Timestamp", df.format(new Date()));
        paras.put("Format", "JSON");
        // 2. 业务API参数
        paras.put("Action", "SendSms");
        paras.put("Version", "2017-05-25");
        paras.put("RegionId", "cn-hangzhou");
        paras.put("PhoneNumbers", template.getReceive());
        paras.put("SignName", properties.getSignName());
        paras.put("TemplateParam", template.getTemplateParam());
        paras.put("TemplateCode", template.getTempCode());

        paras.put("Signature", getSign(paras, properties.getAppSecret()));

        Map<String, Object> map = new HashMap<>();
        for (String str : paras.keySet()) {
            map.put(specialUrlEncode(str), specialUrlEncode(paras.get(str)));
        }
        return checkResponse(Httpx.get(ALIYUN_PRODUCT_DOMAIN, map));
    }

}
