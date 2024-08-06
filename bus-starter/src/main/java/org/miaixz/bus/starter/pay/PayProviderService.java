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
package org.miaixz.bus.starter.pay;

import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Provider;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.ErrorCode;
import org.miaixz.bus.pay.metric.alipay.AliPayProvider;
import org.miaixz.bus.pay.metric.jdpay.JdPayProvider;
import org.miaixz.bus.pay.metric.paypal.PaypalProvider;
import org.miaixz.bus.pay.metric.tenpay.TenpayProvider;
import org.miaixz.bus.pay.metric.unionpay.UnionPayProvider;
import org.miaixz.bus.pay.metric.wechat.WechatPayProvider;

/**
 * 集合支付服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PayProviderService {

    /**
     * 通知器配置
     */
    private static Map<Registry, Context> CACHE = MapKit.newSafeConcurrentHashMap();
    public PayProperties properties;
    public ExtendCache cache;
    public Complex complex;

    public PayProviderService(PayProperties properties) {
        this.properties = properties;
    }

    public PayProviderService(PayProperties properties, Complex complex) {
        this.properties = properties;
        this.complex = complex;
    }

    public PayProviderService(PayProperties properties, Complex complex, ExtendCache cache) {
        this.properties = properties;
        this.complex = complex;
        this.cache = cache;
    }

    /**
     * 注册组件
     *
     * @param registry 组件名称
     * @param context  组件对象
     */
    public static void register(Registry registry, Context context) {
        if (CACHE.containsKey(registry)) {
            throw new InternalException("重复注册同名称的组件：" + registry.name());
        }
        CACHE.putIfAbsent(registry, context);
    }

    /**
     * 返回type对象
     *
     * @param registry {@link Registry}
     * @return {@link Provider}
     */
    public Provider require(Registry registry) {
        Context context = CACHE.get(registry);
        if (ObjectKit.isEmpty(context)) {
            context = this.properties.getType().get(registry);
        }
        if (Registry.ALIPAY.equals(registry)) {
            return new AliPayProvider(context, complex, cache);
        } else if (Registry.JDPAY.equals(registry)) {
            return new JdPayProvider(context, complex, cache);
        } else if (Registry.PAYPAL.equals(registry)) {
            return new PaypalProvider(context, complex, cache);
        } else if (Registry.TENPAY.equals(registry)) {
            return new TenpayProvider(context, complex, cache);
        } else if (Registry.UNIONPAY.equals(registry)) {
            return new UnionPayProvider(context, complex, cache);
        } else if (Registry.WECHAT.equals(registry)) {
            return new WechatPayProvider(context, complex, cache);
        }
        throw new InternalException(ErrorCode.UNSUPPORTED.getDesc());
    }

}
