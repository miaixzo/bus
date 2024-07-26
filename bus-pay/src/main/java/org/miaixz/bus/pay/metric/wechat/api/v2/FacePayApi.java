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
 * 微信支付 v2 版本-刷脸支付相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum FacePayApi implements Matcher {

    /**
     * 获取调用凭证
     */
    GET_AUTH_INFO("/face/get_wxpayface_authinfo", "获取调用凭证"),

    /**
     * 刷脸支付
     */
    FACE_PAY("/pay/facepay", "刷脸支付"),

    /**
     * 查询刷脸支付订单
     */
    FACE_PAY_QUERY("/pay/facepayqueryy", "查询刷脸支付订单"),

    /**
     * 撤销刷脸支付订单
     */
    FACE_PAY_REVERSE("/secapi/pay/facepayreverse", "撤销刷脸支付订单");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    FacePayApi(String method, String desc) {
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
