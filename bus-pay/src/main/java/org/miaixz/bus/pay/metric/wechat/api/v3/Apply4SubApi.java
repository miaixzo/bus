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
 * 微信支付 v3 版本接口-特约商户进件接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Apply4SubApi implements Matcher {

    /**
     * 提交申请单
     */
    APPLY_4_SUB("/v3/applyment4sub/applyment/", "提交申请单"),

    /**
     * 查询申请单状态
     */
    GET_APPLY_STATE("/v3/applyment4sub/applyment/business_code/%s", "查询申请单状态"),

    /**
     * 通过申请单号查询申请状态
     */
    GET_APPLY_STATE_BY_ID("/v3/applyment4sub/applyment/applyment_id/%s", "通过申请单号查询申请状态"),

    /**
     * 修改结算账号
     */
    MODIFY_SETTLEMENT("/v3/apply4sub/sub_merchants/%s/modify-settlement", "修改结算账号"),

    /**
     * 查询结算账户
     */
    GET_SETTLEMENT("/v3/apply4sub/sub_merchants/%s/settlement", "查询结算账户"),

    /**
     * 商户开户意愿确认-提交申请单 OR 查询申请单审核结果
     */
    MER_OPEN_APPLY_SUBMIT_OR_RESULT("/v3/apply4subject/applyment", "提交申请单/查询申请单审核结果"),

    /**
     * 商户开户意愿确认-撤销申请单
     */
    MER_OPEN_APPLY_CANCEL("/v3/apply4subject/applyment/%s/cancel", "撤销申请单"),

    /**
     * 商户开户意愿确认-获取商户开户意愿确认状态
     */
    GET_MER_OPEN_APPLY_STATE("/v3/apply4subject/applyment/merchants/%s/state", "获取商户开户意愿确认状态");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    Apply4SubApi(String method, String desc) {
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
