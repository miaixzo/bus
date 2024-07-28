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
package org.miaixz.bus.pay.metric.tenpay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * QQ 钱包API
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TenpayApi implements Matcher {

    /**
     * 提交付款码支付
     */
    MICRO_PAY_URL("/pay/qpay_micro_pay.cgi", "提交付款码支付"),
    /**
     * 统一下单
     */
    UNIFIED_ORDER_URL("/pay/qpay_unified_order.cgi", "统一下单"),
    /**
     * 订单查询
     */
    ORDER_QUERY_URL("/pay/qpay_order_query.cgi", "订单查询"),
    /**
     * 关闭订单
     */
    CLOSE_ORDER_URL("/pay/qpay_close_order.cgi", "关闭订单"),
    /**
     * 撤销订单
     */
    ORDER_REVERSE_URL("/pay/qpay_reverse.cgi", "撤销订单"),
    /**
     * 申请退款
     */
    ORDER_REFUND_URL("/pay/qpay_refund.cgi", "申请退款"),
    /**
     * 退款查询
     */
    REFUND_QUERY_URL("/pay/qpay_refund_query.cgi", "退款查询"),
    /**
     * 对账单下载
     */
    DOWNLOAD_BILL_URL("/sp_download/qpay_mch_statement_down.cgi", "对账单下载"),
    /**
     * 创建现金红包
     */
    CREATE_READ_PACK_URL("/hongbao/qpay_hb_mch_send.cgi", "创建现金红包"),
    /**
     * 查询红包详情
     */
    GET_HB_INFO_URL("/mch_query/qpay_hb_mch_list_query.cgi", "查询红包详情"),
    /**
     * 红包对账单下载
     */
    DOWNLOAD_HB_BILL_URL("/hongbao/qpay_hb_mch_down_list_file.cgi", "红包对账单下载"),
    /**
     * 企业付款到余额
     */
    TRANSFER_URL("/epay/qpay_epay_b2c.cgi", "企业付款到余额"),
    /**
     * 查询企业付款
     */
    GET_TRANSFER_INFO_URL("/pay/qpay_epay_query.cgi", "查询企业付款"),
    /**
     * 企业付款对账单下载
     */
    DOWNLOAD_TRANSFER_BILL_URL("/pay/qpay_epay_statement_down.cgi", "企业付款对账单下载");

    /**
     * 接口方法
     */
    private final String method;
    /**
     * 描述
     */
    private final String desc;

    TenpayApi(String method, String desc) {
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
