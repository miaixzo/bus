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
 * 微信支付 v3 接口-微信支付分接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayScoreApi implements Matcher {

    /**
     * 创建/查询支付分订单
     */
    PAY_SCORE_SERVICE_ORDER("/v3/payscore/serviceorder", "创建/查询支付分订单"),

    /**
     * 取消支付分订单
     */
    PAY_SCORE_SERVICE_ORDER_CANCEL("/v3/payscore/serviceorder/%s/cancel", "取消支付分订单"),

    /**
     * 修改订单金额
     */
    PAY_SCORE_SERVICE_ORDER_MODIFY("/v3/payscore/serviceorder/%s/modify", "修改订单金额"),

    /**
     * 完结支付分订单
     */
    PAY_SCORE_SERVICE_ORDER_COMPLETE("/v3/payscore/serviceorder/%s/complete", "完结支付分订单"),

    /**
     * 同步服务订单信息
     */
    PAY_SCORE_SERVICE_ORDER_SYNC("/v3/payscore/serviceorder/%s/sync", "同步服务订单信息"),

    /**
     * 商户预授权
     */
    PAY_SCORE_PERMISSIONS("/v3/payscore/permissions", "商户预授权"),

    /**
     * 查询与用户授权记录（授权协议号）
     */
    PAY_SCORE_PERMISSIONS_AUTHORIZATION_CODE("/v3/payscore/permissions/authorization-code/%s", "查询与用户授权记录（授权协议号）"),

    /**
     * 解除用户授权关系（授权协议号）
     */
    PAY_SCORE_PERMISSIONS_AUTHORIZATION_CODE_TERMINATE("/v3/payscore/permissions/authorization-code/%s/terminate",
            "解除用户授权关系（授权协议号）"),

    /**
     * 查询与用户授权记录（openid）
     */
    PAY_SCORE_PERMISSIONS_OPENID("/v3/payscore/permissions/openid/%s", "查询与用户授权记录（openid）"),

    /**
     * 解除用户授权关系（openid）
     */
    PAY_SCORE_PERMISSIONS_OPENID_TERMINATE("/v3/payscore/permissions/openid/%s/terminate", "解除用户授权关系（openid）");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    PayScoreApi(String method, String desc) {
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
