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
 * 微信支付 v2 版本-分账接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ProfitSharingApi implements Matcher {

    /**
     * 请求单次分账
     */
    PROFIT_SHARING("/secapi/pay/profitsharing", "请求单次分账"),

    /**
     * 请求多次分账
     */
    MULTI_PROFIT_SHARING("/secapi/pay/multiprofitsharing", "请求多次分账"),

    /**
     * 查询分账结果
     */
    PROFIT_SHARING_QUERY("/pay/profitsharingquery", "查询分账结果"),

    /**
     * 添加分账接收方
     */
    PROFIT_SHARING_ADD_RECEIVER("/pay/profitsharingaddreceiver", "添加分账接收方"),

    /**
     * 删除分账接收方
     */
    PROFIT_SHARING_REMOVE_RECEIVER("/pay/profitsharingremovereceiver", "删除分账接收方"),

    /**
     * 完结分账
     */
    PROFIT_SHARING_FINISH("/secapi/pay/profitsharingfinish", "完结分账"),

    /**
     * 查询订单待分账金额
     */
    PROFIT_SHARING_ORDER_AMOUNT_QUERY("/pay/profitsharingorderamountquery", "查询订单待分账金额"),

    /**
     * 分账回退
     */
    PROFIT_SHARING_RETURN("/secapi/pay/profitsharingreturn", "分账回退"),

    /**
     * 分账回退结果查询
     */
    PROFIT_SHARING_RETURN_QUERY("/pay/profitsharingreturnquery", "分账回退结果查询");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    ProfitSharingApi(String method, String desc) {
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
