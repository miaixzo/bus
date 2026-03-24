/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay;

import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.alipay.AliPayProvider;
import org.miaixz.bus.pay.metric.jdpay.JdPayProvider;
import org.miaixz.bus.pay.metric.paypal.PaypalProvider;
import org.miaixz.bus.pay.metric.tenpay.TenpayProvider;
import org.miaixz.bus.pay.metric.unionpay.UnionPayProvider;
import org.miaixz.bus.pay.metric.wechat.WechatPayProvider;

/**
 * Represents the types of payment platforms.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Registry implements Complex {

    /**
     * Alipay.
     */
    ALIPAY {

        /**
         * Returns the sandbox gateway URL for Alipay.
         *
         * @return The sandbox gateway URL.
         */
        @Override
        public String sandbox() {
            return "https://openapi.alipaydev.com/gateway.do?";
        }

        /**
         * Returns the production gateway URL for Alipay.
         *
         * @return The production gateway URL.
         */
        @Override
        public String service() {
            // Message verification address
            // return "https://mapi.alipay.com/gateway.do?";
            return "https://openapi.alipay.com/gateway.do?";

        }

        /**
         * Returns the provider class for Alipay.
         *
         * @return The {@link AliPayProvider} class.
         */
        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return AliPayProvider.class;
        }
    },
    /**
     * JD Pay.
     */
    JDPAY {

        /**
         * JD Pay does not provide a sandbox environment.
         *
         * @return null.
         */
        @Override
        public String sandbox() {
            return null;
        }

        /**
         * Returns the production service URL for JD Pay.
         *
         * @return The production service URL.
         */
        @Override
        public String service() {
            return "https://paygate.jd.com/service";
        }

        /**
         * Returns the provider class for JD Pay.
         *
         * @return The {@link JdPayProvider} class.
         */
        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return JdPayProvider.class;
        }
    },
    /**
     * Paypal.
     */
    PAYPAL {

        /**
         * Returns the sandbox API URL for Paypal.
         *
         * @return The sandbox API URL.
         */
        @Override
        public String sandbox() {
            return "https://api.sandbox.paypal.com";
        }

        /**
         * Returns the production API URL for Paypal.
         *
         * @return The production API URL.
         */
        @Override
        public String service() {
            return "https://api.paypal.com";
        }

        /**
         * Returns the provider class for Paypal.
         *
         * @return The {@link PaypalProvider} class.
         */
        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return PaypalProvider.class;
        }
    },
    /**
     * QQ Wallet.
     */
    TENPAY {

        /**
         * QQ Wallet does not provide a sandbox environment.
         *
         * @return null.
         */
        @Override
        public String sandbox() {
            return null;
        }

        /**
         * Returns the production service URL for QQ Wallet.
         *
         * @return The production service URL.
         */
        @Override
        public String service() {
            // https://api.qpay.qq.com/cgi-bin
            return "https://qpay.qq.com/cgi-bin";
        }

        /**
         * Returns the provider class for QQ Wallet.
         *
         * @return The {@link TenpayProvider} class.
         */
        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return TenpayProvider.class;
        }
    },
    /**
     * UnionPay Cloud QuickPass.
     */
    UNIONPAY {

        /**
         * UnionPay does not provide a sandbox environment for this API.
         *
         * @return null.
         */
        @Override
        public String sandbox() {
            return null;
        }

        /**
         * Returns the production service URL for UnionPay user authentication.
         *
         * @return The production service URL.
         */
        @Override
        public String service() {
            return "https://qr.95516.com/qrcGtwWeb-web/api/userAuth?version=1.0.0&redirectUrl=%s";
        }

        /**
         * Returns the provider class for UnionPay.
         *
         * @return The {@link UnionPayProvider} class.
         */
        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return UnionPayProvider.class;
        }
    },
    /**
     * Wechat Pay.
     */
    WECHAT {

        /**
         * Wechat Pay does not provide a sandbox environment for this API.
         *
         * @return null.
         */
        @Override
        public String sandbox() {
            return null;
        }

        /**
         * Returns the production service URL for Wechat Pay in China.
         *
         * @return The production service URL.
         */
        @Override
        public String service() {
            return R.CHINA.url;
        }

        /**
         * Returns the provider class for Wechat Pay.
         *
         * @return The {@link WechatPayProvider} class.
         */
        @Override
        public Class<? extends AbstractProvider> getTargetClass() {
            return WechatPayProvider.class;
        }

        /**
         * Wechat Pay API URLs for different regions.
         */
        enum R {

            /**
             * China.
             */
            CHINA("https://api.mch.weixin.qq.com"),
            /**
             * China (alternative domain).
             */
            CHINA2("https://api2.mch.weixin.qq.com"),
            /**
             * Southeast Asia.
             */
            HK("https://apihk.mch.weixin.qq.com"),
            /**
             * Other regions.
             */
            US("https://apius.mch.weixin.qq.com"),
            /**
             * For retrieving public keys.
             */
            FRAUD("https://fraud.mch.weixin.qq.com"),
            /**
             * For activities.
             */
            ACTION("https://action.weixin.qq.com"),
            /**
             * For face recognition payment (PAY_APP).
             */
            PAY_APP("https://payapp.weixin.qq.com");

            /**
             * The domain URL.
             */
            private final String url;

            R(String url) {
                this.url = url;
            }
        }
    };

    /**
     * Gets the payment platform information by its name.
     *
     * @param name The abbreviated name of the third-party platform.
     * @return The payment platform information.
     * @throws IllegalArgumentException if the platform type is not supported.
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
