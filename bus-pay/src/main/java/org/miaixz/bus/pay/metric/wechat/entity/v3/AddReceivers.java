/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.pay.metric.wechat.entity.v3;

import lombok.*;
import org.miaixz.bus.pay.magic.Material;

/**
 * V3 添加分账接收方
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddReceivers extends Material {

    /**
     * 微信支付分配的子商户号，即分账的出资商户号
     * （直连商户不需要，服务商需要）
     */
    private String sub_mchid;
    /**
     * 子商户的公众账号ID，分账接收方类型包含PERSONAL_SUB_OPENID时必填
     * （直连商户不需要，服务商需要）
     */
    private String sub_appid;
    /**
     * 分账接收方类型
     * MERCHANT_ID：商户ID
     * PERSONAL_OPENID：个人openid（由父商户APPID转换得到）
     * PERSONAL_SUB_OPENID：个人sub_openid（由子商户APPID转换得到）
     */
    private String type;
    /**
     * 分账接收方帐号
     * 类型是MERCHANT_ID时，是商户号（mch_id或者sub_mch_id）
     * 类型是PERSONAL_OPENID时，是个人openid
     * 类型是PERSONAL_SUB_OPENID时，是个人sub_openid
     */
    private String account;
    /**
     * 分账接收方全称
     * 分账接收方类型是MERCHANT_ID时，是商户全称（必传），当商户是小微商户或个体户时，是开户人姓名
     * 分账接收方类型是PERSONAL_OPENID时，是个人姓名（选传，传则校验）
     * 分账接收方类型是PERSONAL_SUB_OPENID时，是个人姓名（选传，传则校验）
     * 1、此字段需要加密，的加密方法详见：敏感信息加密说明
     * 2、使用微信支付平台证书中的公钥
     * 3、使用RSAES-OAEP算法进行加密
     * 4、将请求中HTTP头部的Wechatpay-Serial设置为证书序列号
     */
    private String name;
    /**
     * 与分账方的关系类型，子商户与接收方的关系。
     * 本字段值为枚举：
     * SERVICE_PROVIDER：服务商
     * STORE：门店
     * STAFF：员工
     * STORE_OWNER：店主
     * PARTNER：合作伙伴
     * HEADQUARTER：总部
     * BRAND：品牌方
     * DISTRIBUTOR：分销商
     * USER：用户
     * SUPPLIER：供应商
     * CUSTOM：自定义
     */
    private String relation_type;
    /**
     * 自定义的分账关系
     * 子商户与接收方具体的关系，本字段最多10个字。
     * 当字段relation_type的值为CUSTOM时，本字段必填
     * 当字段relation_type的值不为CUSTOM时，本字段无需填写
     */
    private String custom_relation;

}
