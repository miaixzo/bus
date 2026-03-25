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
 * A service provider class for managing and creating various message notification provider instances. This class
 * maintains a cache of notification components and supports adding them through configuration or manual registration.
 * <p>
 * It supports a variety of notification methods, including but not limited to:
 *
 * <ul>
 * <li>SMS Services: Aliyun, Tencent Cloud, Huawei Cloud, Baidu Cloud, Netease Cloud, Qiniu Cloud, etc.</li>
 * <li>Email Services: Aliyun Email, Generic Email</li>
 * <li>Enterprise Communication: DingTalk, WeChat Work</li>
 * <li>WeChat Notifications: Official Accounts, Mini Programs, Customer Service Messages, etc.</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * // Create configuration
 * NotifyProperties properties = new NotifyProperties();
 * // Create the service
 * NotifyService service = new NotifyService(properties);
 * // Get the Aliyun SMS service provider
 * Provider smsProvider = service.require(Registry.ALIYUN_SMS);
 * // Send an SMS
 * smsProvider.send("phoneNumber", "messageContent");
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NotifyService {

    /**
     * Cache for storing registered notification components. Uses {@link ConcurrentHashMap} for thread safety.
     */
    private static final Map<Registry, Context> CACHE = new ConcurrentHashMap<>();

    /**
     * Notification configuration properties, containing settings for various notification components.
     */
    public NotifyProperties properties;

    /**
     * Constructs an instance of the notification service provider with the specified configuration properties.
     *
     * @param properties The notification configuration properties (must not be null).
     */
    public NotifyService(NotifyProperties properties) {
        this.properties = properties;
    }

    /**
     * Registers a notification component in the cache. Throws an exception if a component of the same type is already
     * registered.
     *
     * @param registry The type of the notification component (must not be null).
     * @param context  The context of the notification component (must not be null).
     * @throws InternalException if a component of the same type already exists.
     */
    public static void register(Registry registry, Context context) {
        if (CACHE.containsKey(registry)) {
            throw new InternalException("A component with the same name is already registered: " + registry.name());
        }
        CACHE.putIfAbsent(registry, context);
    }

    /**
     * Retrieves the corresponding notification service provider instance based on the component type. It first searches
     * the cache; if not found, it retrieves from the configuration.
     *
     * @param registry The type of the notification component (must not be null).
     * @return The corresponding notification service provider instance.
     * @throws InternalException if the corresponding notification component cannot be found.
     */
    public Provider require(Registry registry) {
        // Get the notification component context from the cache
        Context context = CACHE.get(registry);
        // If not in the cache, get it from the properties
        if (ObjectKit.isEmpty(context)) {
            context = this.properties.getType().get(registry);
        }

        // Create the corresponding provider instance based on the notification type
        switch (registry) {
            case ALIYUN_SMS:
                return new AliyunSmsProvider(context);

            case ALIYUN_VMS:
                return new AliyunVmsProvider(context);

            case ALIYUN_EDM:
                return new AliyunEmailProvider(context);

            case BAIDU_SMS:
                return new BaiduSmsProvider(context);

            case DINGTALK:
                return new DingTalkProvider(context);

            case GENERIC_EDM:
                return new GenericEmailProvider(context);

            case HUAWEI_SMS:
                return new HuaweiSmsProvider(context);

            case JDCLOUD_SMS:
                return new JdcloudSmsProvider(context);

            case JPUSH_SMS:
                return new JpushSmsProvider(context);

            case NETEASE_SMS:
                return new NeteaseSmsProvider(context);

            case QINIU_SMS:
                return new QiniuSmsProvider(context);

            case TENCENT_SMS:
                return new TencentSmsProvider(context);

            case UPYUN_SMS:
                return new UpyunSmsProvider(context);

            case WECHAT_CP:
                return new WechatCpProvider(context);

            case WECHAT_KF:
                return new WechatKfProvider(context);

            case WECHAT_MINI:
                return new WechatMiniProvider(context);

            case WECHAT_MP:
                return new WechatMpProvider(context);

            case YUNPIAN_SMS:
                return new YunpianSmsProvider(context);

            default:
                // If no matching notification type is found, throw an exception
                throw new InternalException(ErrorCode._100803.getValue());
        }
    }

}
