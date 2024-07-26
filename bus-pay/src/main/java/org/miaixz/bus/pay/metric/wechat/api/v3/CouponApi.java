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
 * 微信支付 v3 接口-优惠券接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum CouponApi implements Matcher {

    /**
     * 创建代金券批次
     */
    CREATE_COUPON_STOCKS("/v3/marketing/favor/coupon-stocks", "创建代金券批次"),

    /**
     * 激活代金券批次
     */
    START_COUPON_STOCKS("/v3/marketing/favor/stocks/%s/start", "激活代金券批次"),

    /**
     * 发放代金券
     */
    SEND_COUPON("/v3/marketing/favor/users/%s/coupons", "发放代金券"),

    /**
     * 暂停代金券批次
     */
    PAUSE_COUPON_STOCKS("/v3/marketing/favor/stocks/%s/pause", "暂停代金券批次"),

    /**
     * 重启代金券批次
     */
    RESTART_COUPON_STOCKS("/v3/marketing/favor/stocks/%s/restart", "重启代金券批次"),
    /**
     * 条件查询批次列表
     */
    QUERY_COUPON_STOCKS("/v3/marketing/favor/stocks", "条件查询批次列表"),
    /**
     * 查询批次详情
     */
    QUERY_COUPON_STOCKS_INFO("/v3/marketing/favor/stocks/%s", "查询批次详情"),
    /**
     * 查询代金券详情
     */
    QUERY_COUPON_INFO("/v3/marketing/favor/users/%s/coupons/%s", "查询代金券详情"),

    /**
     * 查询代金券可用商户
     */
    QUERY_COUPON_MERCHANTS("/v3/marketing/favor/stocks/%s/merchants", "查询代金券可用商户"),

    /**
     * 查询代金券可用单品
     */
    QUERY_COUPON_ITEMS("/v3/marketing/favor/stocks/%s/items", "查询代金券可用单品"),

    /**
     * 根据商户号查用户的券
     */
    QUERY_USER_COUPON("/v3/marketing/favor/users/%s/coupons", "根据商户号查用户的券"),

    /**
     * 下载批次核销明细
     */
    DOWNLOAD_COUPON_STOCKS_USER_FLOW("/v3/marketing/favor/stocks/%s/use-flow", "下载批次核销明细"),

    /**
     * 下载批次退款明细
     */
    DOWNLOAD_COUPON_STOCKS_REFUND_FLOW("/v3/marketing/favor/stocks/%s/refund-flow", "下载批次退款明细"),

    /**
     * 设置消息通知地址
     */
    SETTING_COUPON_CALLBACKS("/v3/marketing/favor/callbacks", "设置消息通知地址"),

    /**
     * 创建商家券
     */
    CREATE_BUSINESS_COUPON("/v3/marketing/busifavor/stocks", "创建商家券"),

    /**
     * 查询商家券批次详情
     */
    QUERY_BUSINESS_COUPON_STOCKS_INFO("/v3/marketing/busifavor/stocks/%s", "查询商家券批次详情"),

    /**
     * 核销用户券
     */
    USE_BUSINESS_COUPON("/v3/marketing/busifavor/coupons/use", "核销用户券"),

    /**
     * 根据过滤条件查询用户券
     */
    QUERY_BUSINESS_USER_COUPON("/v3/marketing/busifavor/users/%s/coupons", "根据过滤条件查询用户券"),

    /**
     * 查询用户单张券详情
     */
    QUERY_BUSINESS_USER_COUPON_INFO("/v3/marketing/busifavor/users/%s/coupons/%s/appids/%s", "查询用户单张券详情"),

    /**
     * 上传预存code
     */
    BUSINESS_COUPON_UPLOAD_CODE("/v3/marketing/busifavor/stocks/%s/couponcodes", "上传预存code"),

    /**
     * 设置/查询商家券事件通知地址
     */
    BUSINESS_COUPON_CALLBACKS("/v3/marketing/busifavor/callbacks", "设置/查询商家券事件通知地址"),

    /**
     * 关联订单信息
     */
    BUSINESS_COUPON_ASSOCIATE("/v3/marketing/busifavor/coupons/associate", "关联订单信息"),

    /**
     * 取消关联订单信息
     */
    BUSINESS_COUPON_DISASSOCIATE("/v3/marketing/busifavor/coupons/disassociate", "取消关联订单信息"),

    /**
     * 修改批次预算
     */
    MODIFY_BUSINESS_COUPON_STOCKS_BUDGET("/v3/marketing/busifavor/stocks/%s/budget", "修改批次预算"),

    /**
     * 修改商家券基本信息
     */
    MODIFY_BUSINESS_COUPON_INFO("/v3/marketing/busifavor/stocks/%s", "修改商家券基本信息"),

    /**
     * 申请退券
     */
    APPLY_REFUND_COUPONS("/v3/marketing/busifavor/coupons/return", "申请退券"),

    /**
     * 使券失效
     */
    COUPON_DEACTIVATE("/v3/marketing/busifavor/coupons/deactivate", "使券失效"),

    /**
     * 营销补差付款
     */
    COUPON_SUBSIDY_PAY("/v3/marketing/busifavor/subsidy/pay-receipts", "营销补差付款"),

    /**
     * 查询营销补差付款单详情
     */
    COUPON_SUBSIDY_PAY_INFO("/v3/marketing/busifavor/subsidy/pay-receipts/%s", "查询营销补差付款单详情"),

    /**
     * 委托营销-建立合作关系
     */
    PARTNERSHIPS_BUILD("/v3/marketing/partnerships/build", "建立合作关系"),

    /**
     * 委托营销-终止合作关系
     */
    PARTNERSHIPS_TERMINATE("/v3/marketing/partnerships/terminate", "终止合作关系"),

    /**
     * 发放消费卡
     */
    SEND_BUSINESS_COUPON("/v3/marketing/busifavor/coupons/%s/send", "发放消费卡");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    CouponApi(String method, String desc) {
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
