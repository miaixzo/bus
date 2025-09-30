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
package org.miaixz.bus.starter.notify;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

/**
 * 通知服务提供类，用于管理和创建各种消息通知服务提供者实例。 该类维护了一个通知组件的缓存，支持通过配置或手动注册方式添加通知组件。
 *
 * <p>
 * 该类支持多种消息通知方式，包括但不限于：
 * </p>
 * <ul>
 * <li>短信服务：阿里云、腾讯云、华为云、百度云、网易云、七牛云等</li>
 * <li>邮件服务：阿里云邮件、通用邮件</li>
 * <li>企业通讯：钉钉、企业微信</li>
 * <li>微信通知：公众号、小程序、客服消息等</li>
 * </ul>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * // 创建配置
 * NotifyProperties properties = new NotifyProperties();
 * // 创建服务
 * NotifyProviderService service = new NotifyProviderService(properties);
 * // 获取阿里云短信服务提供者
 * Provider smsProvider = service.require(Registry.ALIYUN_SMS);
 * // 发送短信
 * smsProvider.send("手机号", "短信内容");
 * </pre>
 *
 * @author Justubborn
 * @since Java 17+
 */
public class NotifyService {

    /**
     * 通知组件缓存，用于存储已注册的通知组件。 使用ConcurrentHashMap保证线程安全。
     */
    private static Map<Registry, Context> CACHE = new ConcurrentHashMap<>();

    /**
     * 通知配置属性，包含各种通知组件的配置信息。
     */
    public NotifyProperties properties;

    /**
     * 使用指定的配置属性创建通知服务提供者实例。
     *
     * @param properties 通知配置属性，不能为null
     */
    public NotifyService(NotifyProperties properties) {
        this.properties = properties;
    }

    /**
     * 注册通知组件到缓存中。 如果已存在相同类型的组件，则抛出异常。
     *
     * @param registry 通知组件类型，不能为null
     * @param context  通知组件上下文，不能为null
     * @throws InternalException 如果已存在相同类型的组件
     */
    public static void register(Registry registry, Context context) {
        if (CACHE.containsKey(registry)) {
            throw new InternalException("重复注册同名称的组件：" + registry.name());
        }
        CACHE.putIfAbsent(registry, context);
    }

    /**
     * 根据通知组件类型获取对应的通知服务提供者实例。 首先从缓存中查找，如果不存在则从配置中获取。
     *
     * @param registry 通知组件类型，不能为null
     * @return 对应的通知服务提供者实例
     * @throws InternalException 如果找不到对应的通知组件
     */
    public Provider require(Registry registry) {
        // 从缓存中获取通知组件上下文
        Context context = CACHE.get(registry);
        // 如果缓存中不存在，则从配置中获取
        if (ObjectKit.isEmpty(context)) {
            context = this.properties.getType().get(registry);
        }

        // 根据不同的通知类型创建对应的通知服务提供者实例
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
        // 如果没有匹配的通知类型，抛出异常
        throw new InternalException(ErrorCode._100803.getValue());
    }

}
