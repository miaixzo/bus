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
package org.miaixz.bus.pay;

import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.alipay.AliPayProvider;
import org.miaixz.bus.pay.metric.jdpay.JdPayProvider;
import org.miaixz.bus.pay.metric.paypal.PayPalProvider;
import org.miaixz.bus.pay.metric.tenpay.TenpayProvider;
import org.miaixz.bus.pay.metric.unionpay.UnionPayProvider;
import org.miaixz.bus.pay.metric.wechat.WechatPayProvider;

/**
 * 支付平台类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry implements Complex {

    /**
     * 支付宝
     */
    ALIPAY {
        @Override
        public String sandbox() {
            return "https://openapi.alipaydev.com/gateway.do?";
        }

        @Override
        public String service() {
            // 消息验证地址
            // return "https://mapi.alipay.com/gateway.do?";
            return "https://openapi.alipay.com/gateway.do?";

        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return AliPayProvider.class;
        }
    },
    /**
     * 京东支付
     */
    JDPAY {
        @Override
        public String sandbox() {
            return null;
        }

        @Override
        public String service() {
            return "https://paygate.jd.com/service";
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return JdPayProvider.class;
        }
    },
    /**
     * Paypal
     */
    PAYPAL {
        @Override
        public String sandbox() {
            return "https://api.sandbox.paypal.com";
        }

        @Override
        public String service() {
            return "https://api.paypal.com";
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return PayPalProvider.class;
        }
    },
    /**
     * QQ钱包
     */
    TENPAY {
        @Override
        public String sandbox() {
            return null;
        }

        @Override
        public String service() {
            // https://api.qpay.qq.com/cgi-bin
            return "https://qpay.qq.com/cgi-bin";
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return TenpayProvider.class;
        }
    },
    /**
     * 银联云闪付
     */
    UNIONPAY {
        @Override
        public String sandbox() {
            return null;
        }

        @Override
        public String service() {
            return "https://qr.95516.com/qrcGtwWeb-web/api/userAuth?version=1.0.0&redirectUrl=%s";
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return UnionPayProvider.class;
        }
    },
    /**
     * 微信
     */
    WECHAT {
        @Override
        public String sandbox() {
            return null;
        }

        @Override
        public String service() {
            return R.CHINA.url;
        }

        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WechatPayProvider.class;
        }

        /**
         * 按照区域分地址
         */
        enum R {
            /**
             * 中国国内
             */
            CHINA("https://api.mch.weixin.qq.com"),
            /**
             * 中国国内(备用域名)
             */
            CHINA2("https://api2.mch.weixin.qq.com"),
            /**
             * 东南亚
             */
            HK("https://apihk.mch.weixin.qq.com"),
            /**
             * 其它
             */
            US("https://apius.mch.weixin.qq.com"),
            /**
             * 获取公钥
             */
            FRAUD("https://fraud.mch.weixin.qq.com"),
            /**
             * 活动
             */
            ACTION("https://action.weixin.qq.com"),
            /**
             * 刷脸支付 PAY_APP
             */
            PAY_APP("https://payapp.weixin.qq.com");

            /**
             * 域名
             */
            private final String url;

            R(String url) {
                this.url = url;
            }
        }
    };

    /**
     * 根据名称获取第三方支付信息
     *
     * @param name 第三方名称简写
     * @return 第三方支付信息, 找不到时直接抛出异常
     */
    public static Registry require(String name) {
        for (Registry registry : Registry.values()) {
            if (registry.name().equalsIgnoreCase(name)) {
                return registry;
            }
        }
        throw new IllegalArgumentException("Unsupported type for " + name);
    }

}
