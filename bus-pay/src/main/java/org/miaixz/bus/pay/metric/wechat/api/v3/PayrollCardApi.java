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
 * 微信支付 v3 接口-务工卡接口相关
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayrollCardApi implements Matcher {

    /**
     * 生成授权token
     */
    TOKEN("/v3/payroll-card/tokens", "生成授权token"),

    /**
     * 查询务工卡授权关系
     */
    RELATION("/v3/payroll-card/relations/%s", "查询务工卡授权关系"),

    /**
     * 务工卡核身预下单
     */
    AUTHENTICATION_PRE_ORDER("/v3/payroll-card/authentications/pre-order", "务工卡核身预下单"),

    /**
     * 获取核身结果
     */
    AUTHENTICATION_RESULT("/v3/payroll-card/authentications/%s", "获取核身结果"),

    /**
     * 查询核身记录
     */
    AUTHENTICATION_LIST("/v3/payroll-card/authentications", "查询核身记录"),

    /**
     * 务工卡核身预下单（流程中完成授权）
     */
    PRE_ORDER_WITH_AUTH("/v3/payroll-card/authentications/pre-order-with-auth", "务工卡核身预下单（流程中完成授权）"),

    /**
     * 发起批量转账
     */
    BATCH_TRANSFER("/v3/payroll-card/transfer-batches", "发起批量转账");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    PayrollCardApi(String method, String desc) {
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
