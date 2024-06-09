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
package org.miaixz.bus.starter.notify;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.Provider;
import org.miaixz.bus.notify.Registry;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.aliyun.AliyunEmailProvider;
import org.miaixz.bus.notify.metric.aliyun.AliyunSmsProvider;
import org.miaixz.bus.notify.metric.aliyun.AliyunVmsProvider;
import org.miaixz.bus.notify.metric.baidu.BaiduSmsProvider;
import org.miaixz.bus.notify.metric.dingtalk.DingTalkProvider;
import org.miaixz.bus.notify.metric.generic.GenericEmailProvider;
import org.miaixz.bus.notify.metric.huawei.HuaweiSmsProvider;
import org.miaixz.bus.notify.metric.jdcloud.JdcloudSmsProvider;
import org.miaixz.bus.notify.metric.jpush.JpushSmsProvider;
import org.miaixz.bus.notify.metric.netease.NeteaseSmsProvider;
import org.miaixz.bus.notify.metric.qiniu.QiniuSmsProvider;
import org.miaixz.bus.notify.metric.tencent.TencentSmsProvider;
import org.miaixz.bus.notify.metric.upyun.UpyunSmsProvider;
import org.miaixz.bus.notify.metric.wechat.WechatCpProvider;
import org.miaixz.bus.notify.metric.wechat.WechatKfProvider;
import org.miaixz.bus.notify.metric.wechat.WechatMiniProvider;
import org.miaixz.bus.notify.metric.wechat.WechatMpProvider;
import org.miaixz.bus.notify.metric.yunpian.YunpianSmsProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知提供服务
 *
 * @author Justubborn
 * @since Java 17+
 */
public class NotifyProviderService {

    /**
     * 通知器配置
     */
    private static Map<Registry, Context> CACHE = new ConcurrentHashMap<>();
    public NotifyProperties properties;

    public NotifyProviderService(NotifyProperties properties) {
        this.properties = properties;
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
        if (Registry.ALIYUN_SMS.equals(registry)) {
            return new AliyunSmsProvider(context);
        } else if (Registry.ALIYUN_VMS.equals(registry)) {
            return new AliyunVmsProvider(context);
        } else if (Registry.ALIYUN_EDM.equals(registry)) {
            return new AliyunEmailProvider(context);
        } else if (Registry.BAIDU_SMS.equals(registry)) {
            return new BaiduSmsProvider(context);
        } else if (Registry.DINGTALK.equals(registry)) {
            return new DingTalkProvider(context);
        } else if (Registry.GENERIC_EDM.equals(registry)) {
            return new GenericEmailProvider(context);
        } else if (Registry.HUAWEI_SMS.equals(registry)) {
            return new HuaweiSmsProvider(context);
        } else if (Registry.JDCLOUD_SMS.equals(registry)) {
            return new JdcloudSmsProvider(context);
        } else if (Registry.JPUSH_SMS.equals(registry)) {
            return new JpushSmsProvider(context);
        } else if (Registry.NETEASE_SMS.equals(registry)) {
            return new NeteaseSmsProvider(context);
        } else if (Registry.QINIU_SMS.equals(registry)) {
            return new QiniuSmsProvider(context);
        } else if (Registry.TENCENT_SMS.equals(registry)) {
            return new TencentSmsProvider(context);
        } else if (Registry.UPYUN_SMS.equals(registry)) {
            return new UpyunSmsProvider(context);
        } else if (Registry.WECHAT_CP.equals(registry)) {
            return new WechatCpProvider(context);
        } else if (Registry.WECHAT_KF.equals(registry)) {
            return new WechatKfProvider(context);
        } else if (Registry.WECHAT_MINI.equals(registry)) {
            return new WechatMiniProvider(context);
        } else if (Registry.WECHAT_MP.equals(registry)) {
            return new WechatMpProvider(context);
        } else if (Registry.YUNPIAN_SMS.equals(registry)) {
            return new YunpianSmsProvider(context);
        }
        throw new InternalException(ErrorCode.UNSUPPORTED.getMsg());
    }

}
