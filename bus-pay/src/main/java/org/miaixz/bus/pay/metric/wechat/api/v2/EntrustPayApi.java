/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v2 版本-扣款服务接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum EntrustPayApi implements Matcher {

    /**
     * 公众号纯签约
     */
    ENTRUST_WEB("/papay/entrustweb", "公众号纯签约"),

    /**
     * 公众号纯签约（服务商模式）
     */
    PARTNER_ENTRUST_WEB("/papay/partner/entrustweb", "公众号纯签约（服务商模式）"),

    /**
     * APP纯签约
     */
    PRE_ENTRUST_WEB("/papay/preentrustweb", "APP纯签约"),

    /**
     * APP纯签约（服务商模式）
     */
    PARTNER_PRE_ENTRUST_WEB("/papay/partner/preentrustweb", "APP纯签约（服务商模式）"),

    /**
     * H5纯签约
     */
    H5_ENTRUST_WEB("/papay/h5entrustweb", "H5纯签约"),
    /**
     * H5纯签约（服务商模式）
     */
    PARTNER_H5_ENTRUST_WEB("/papay/partner/h5entrustweb", "H5纯签约（服务商模式"),

    /**
     * 支付中签约
     */
    PAY_CONTRACT_ORDER("/pay/contractorder", "支付中签约"),

    /**
     * 查询签约关系
     */
    QUERY_ENTRUST_CONTRACT("/papay/querycontract", "查询签约关系"),

    /**
     * 查询签约关系（服务商模式）
     */
    PARTNER_QUERY_ENTRUST_CONTRACT("/papay/partner/querycontract", "查询签约关系（服务商模式）"),

    /**
     * 申请扣款
     */
    PAP_PAY_APPLY("/pay/pappayapply", "申请扣款"),

    /**
     * 申请扣款（服务商模式）
     */
    PARTNER_PAP_PAY_APPLY("/pay/partner/pappayapply", "申请扣款（服务商模式）"),

    /**
     * 代扣申请解约
     */
    DELETE_ENTRUST_CONTRACT("/papay/deletecontract", "代扣申请解约"),
    /**
     * 代扣申请解约（服务商模式）
     */
    PARTNER_DELETE_ENTRUST_CONTRACT("/papay/partner/deletecontract", "代扣申请解约（服务商模式）");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    EntrustPayApi(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    /**
     * 交易类型
     *
     * @return the string
     */
    @Override
    public String type() {
        return this.name();
    }

    /**
     * 类型描述
     *
     * @return the string
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * 接口方法
     *
     * @return the string
     */
    @Override
    public String method() {
        return this.method;
    }

}
