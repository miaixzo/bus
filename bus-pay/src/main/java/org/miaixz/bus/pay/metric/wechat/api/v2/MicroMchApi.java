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
 * 微信支付 v2 版本-小微商户相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum MicroMchApi implements Matcher {

    /**
     * 申请入驻
     */
    SUBMIT("/applyment/micro/submit", "申请入驻"),

    /**
     * 查询申请状态
     */
    GET_SUBMIT_STATE("/applyment/micro/getstate", "查询申请状态"),

    /**
     * 查询提现状态
     */
    QUERY_AUTO_WITH_DRAW_BY_DATE("/fund/queryautowithdrawbydate", "查询提现状态"),

    /**
     * 修改结算银行卡
     */
    MODIFY_ARCHIVES("/applyment/micro/modifyarchives", "修改结算银行卡"),

    /**
     * 修改联系信息
     */
    MODIFY_CONTACT_INFO("/applyment/micro/modifycontactinfo", "修改联系信息"),

    /**
     * 关注配置
     */
    ADD_RECOMMEND_CONF("/secapi/mkt/addrecommendconf", "关注配置"),

    /**
     * 添加开发配置
     */
    ADD_SUB_DEV_CONFIG("/secapi/mch/addsubdevconfig", "开发配置"),

    /**
     * 开发配置查询
     */
    QUERY_SUB_DEV_CONFIG("/secapi/mch/querysubdevconfig", "开发配置查询"),

    /**
     * 提交升级申请
     */
    SUBMIT_UPGRADE("/applyment/micro/submitupgrade", "提交升级申请"),

    /**
     * 查询升级申请单状态
     */
    GET_UPGRADE_STATE("/applyment/micro/getupgradestate", "查询升级申请单状态"),

    /**
     * 服务商帮小微商户重新发起自动提现
     */
    RE_AUTO_WITH_DRAW_BY_DATE("/fund/reautowithdrawbydate", "服务商帮小微商户重新发起自动提现");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    MicroMchApi(String method, String desc) {
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
