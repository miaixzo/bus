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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v3 接口-支付有礼接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayGiftActivityApi implements Matcher {

    /**
     * 创建全场满额送活动
     */
    PAY_GIFT_ACTIVITY("/v3/marketing/paygiftactivity/unique-threshold-activity", "创建全场满额送活动"),

    /**
     * 查询活动详情接口
     */
    PAY_GIFT_ACTIVITY_INFO("/v3/marketing/paygiftactivity/activities/%s", "查询活动详情接口"),

    /**
     * 查询活动发券商户号
     */
    PAY_GIFT_ACTIVITY_QUERY_MERCHANTS("/v3/marketing/paygiftactivity/activities/%s/merchants", "查询活动发券商户号"),

    /**
     * 查询活动指定商品列表
     */
    PAY_GIFT_ACTIVITY_QUERY_GOODS("/v3/marketing/paygiftactivity/activities/%s/goods", "查询活动指定商品列表"),

    /**
     * 终止活动
     */
    PAY_GIFT_ACTIVITY_TERMINATE("/v3/marketing/paygiftactivity/activities/%s/terminate", "终止活动"),

    /**
     * 新增活动发券商户号
     */
    PAY_GIFT_ACTIVITY_ADD_MERCHANTS("/v3/marketing/paygiftactivity/activities/%s/merchants/add", "新增活动发券商户号"),

    /**
     * 获取支付有礼活动列表
     */
    PAY_GIFT_ACTIVITY_LIST("/v3/marketing/paygiftactivity/activities", "获取支付有礼活动列表"),

    /**
     * 删除活动发券商户号
     */
    PAY_GIFT_ACTIVITY_DELETE_MERCHANTS("/v3/marketing/paygiftactivity/activities/%s/merchants/delete", "删除活动发券商户号");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    PayGiftActivityApi(String method, String desc) {
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
