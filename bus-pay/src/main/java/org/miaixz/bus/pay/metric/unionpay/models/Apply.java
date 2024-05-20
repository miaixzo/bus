package org.miaixz.bus.pay.metric.unionpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.pay.magic.Property;

/**
 * 云闪付-商户进件
 */
@Data
@Builder
@AllArgsConstructor
public class Apply extends Property {

    /**
     * 合作伙伴 ID 即机构号
     */
    private String partner;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 支持 MD5 和RSA，默认为MD5
     */
    private String signType;
    /**
     * 字符集，默认为UTF-8
     */
    private String charset;
    /**
     * 请求数据
     */
    private String data;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 数据签名
     */
    private String dataSign;

}
