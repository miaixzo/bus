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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v3 接口-商家转账到零钱接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TransferApi implements Matcher {

    /**
     * 发起商家转账
     */
    TRANSFER_BATCHES("/v3/transfer/batches", "发起商家转账"),

    /**
     * 微信支付批次单号查询批次单
     */
    TRANSFER_QUERY_BY_BATCH_ID("/v3/transfer/batches/batch-id/%s", "微信支付批次单号查询批次单"),

    /**
     * 微信支付批次单号查询批次单
     */
    PARTNER_TRANSFER_QUERY_BY_BATCH_ID("/v3/partner-transfer/batches/batch-id/%s", "微信支付批次单号查询批次单"),

    /**
     * 微信支付明细单号查询明细单
     */
    TRANSFER_QUERY_BY_DETAIL_ID("/v3/transfer/batches/batch-id/%s/details/detail-id/%s", "微信支付明细单号查询明细单"),

    /**
     * 微信支付明细单号查询明细单
     */
    PARTNER_TRANSFER_QUERY_BY_DETAIL_ID("/v3/partner-transfer/batches/batch-id/%s/details/detail-id/%s",
            "微信支付明细单号查询明细单"),

    /**
     * 商家批次单号查询批次单
     */
    TRANSFER_QUERY_BY_OUT_BATCH_NO("/v3/transfer/batches/out-batch-no/%s", "商家批次单号查询批次单"),

    /**
     * 商家批次单号查询批次单
     */
    PARTNER_TRANSFER_QUERY_BY_OUT_BATCH_NO("/v3/partner-transfer/batches/out-batch-no/%s", "商家批次单号查询批次单"),

    /**
     * 商家明细单号查询明细单
     */
    TRANSFER_QUERY_DETAIL_BY_OUT_BATCH_NO("/v3/transfer/batches/out-batch-no/%s/details/out-detail-no/%s",
            "商家明细单号查询明细单"),

    /**
     * 商家明细单号查询明细单
     */
    PARTNER_TRANSFER_QUERY_DETAIL_BY_OUT_BATCH_NO(
            "/v3/partner-transfer/batches/out-batch-no/%s/details/out-detail-no/%s", "商家明细单号查询明细单"),

    /**
     * 转账电子回单申请受理
     */
    TRANSFER_BILL_RECEIPT("/v3/transfer/bill-receipt", "转账电子回单申请受理"),

    /**
     * 查询转账电子回单
     */
    TRANSFER_BILL_RECEIPT_QUERY("/v3/transfer/bill-receipt/%s", "查询转账电子回单"),

    /**
     * 转账明细电子回单受理/查询转账明细电子回单受理结果
     */
    TRANSFER_ELECTRONIC_RECEIPTS("/v3/transfer-detail/electronic-receipts", "转账明细电子回单受理"),

    /**
     * 特约商户银行来账查询
     */
    PARTNER_INCOME_RECORDS("/v3/merchantfund/partner/income-records", "特约商户银行来账查询"),

    /**
     * 服务商银行来账查询
     */
    MERCHANT_INCOME_RECORDS("/v3/merchantfund/merchant/income-records", "服务商银行来账查询");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    TransferApi(String method, String desc) {
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
