package org.aoju.bus.pay.provider.wxpay.models;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * V3 统一下单-商户门店信息
 */
@Data
@Accessors(chain = true)
public class StoreInfo {

    /**
     * 门店编号
     */
    private String id;
    /**
     * 门店名称
     */
    private String name;
    /**
     * 地区编码
     */
    private String area_code;
    /**
     * 详细地址
     */
    private String address;

}
