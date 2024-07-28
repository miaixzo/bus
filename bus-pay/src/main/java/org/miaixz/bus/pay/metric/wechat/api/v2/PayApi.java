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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v2 版本-支付相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayApi implements Matcher {

    /**
     * 沙箱环境
     */
    SAND_BOX_NEW("/sandboxnew", "沙箱环境"),

    /**
     * V2 版本沙箱环境
     */
    API_V2_SANDBOX("/xdc/apiv2sandbox", "V2 版本沙箱环境"),

    /**
     * 获取沙箱环境验签秘钥
     */
    GET_SIGN_KEY("/xdc/apiv2getsignkey/sign/getsignkey", "获取沙箱环境验签秘钥"),

    /**
     * 统一下单
     */
    UNIFIED_ORDER("/pay/unifiedorder", "统一下单"),

    /**
     * 付款码支付
     */
    MICRO_PAY("/pay/micropay", "付款码支付"),

    /**
     * 查询订单
     */
    ORDER_QUERY("/pay/orderquery", "查询订单"),

    /**
     * 关闭订单
     */
    CLOSE_ORDER("/pay/closeorder", "关闭订单"),

    /**
     * 撤销订单
     */
    REVERSE("/secapi/pay/reverse", "撤销订单"),

    /**
     * 申请退款
     */
    REFUND("/secapi/pay/refund", "申请退款"),

    /**
     * 单品优惠-申请退款 <a href="https://pay.weixin.qq.com/wiki/doc/api/danpin.php?chapter=9_103&index=3">官方文档</a>
     */
    REFUND_V2("/secapi/pay/refundv2", "单品优惠-申请退款"),

    /**
     * 查询退款
     */
    REFUND_QUERY("/pay/refundquery", "查询退款"),

    /**
     * 单品优惠-查询退款
     */
    REFUND_QUERY_V2("/pay/refundqueryv2", "单品优惠-查询退款"),

    /**
     * 下载对账单
     */
    DOWNLOAD_BILL("/pay/downloadbill", "下载对账单"),

    /**
     * 下载资金对账单
     */
    DOWNLOAD_FUND_FLOW("/pay/downloadfundflow", "下载资金对账单"),

    /**
     * 交易保障
     */
    REPORT("/payitil/report", "交易保障"),

    /**
     * 付款码查询openid
     */
    AUTH_CODE_TO_OPENID("/tools/authcodetoopenid", "付款码查询openid"),

    /**
     * 转换短链接
     */
    SHORT_URL("/tools/shorturl", "转换短链接");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    PayApi(String method, String desc) {
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
