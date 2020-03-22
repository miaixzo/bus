package org.aoju.bus.notify;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.notify.metric.Properties;
import org.aoju.bus.notify.provider.aliyun.AliyunSmsProperties;
import org.aoju.bus.notify.provider.aliyun.AliyunSmsProvider;
import org.aoju.bus.notify.provider.aliyun.AliyunVmsProperties;
import org.aoju.bus.notify.provider.aliyun.AliyunVmsProvider;
import org.aoju.bus.notify.provider.dingtalk.DingTalkCropMsgProvider;
import org.aoju.bus.notify.provider.dingtalk.DingTalkProperties;
import org.aoju.bus.notify.provider.netease.NeteaseAttachMsgProvider;
import org.aoju.bus.notify.provider.netease.NeteaseMsgProvider;
import org.aoju.bus.notify.provider.netease.NeteaseProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知提供服务
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
public class NotifyProviderService {

    /**
     * 通知器配置
     */
    private static Map<Registry, Properties> NOTIFY_CACHE = new ConcurrentHashMap<>();

    /**
     * 注册组件
     *
     * @param registry   组件名称
     * @param properties 组件对象
     */
    public static void register(Registry registry, Properties properties) {
        if (NOTIFY_CACHE.containsKey(registry)) {
            throw new InstrumentException("重复注册同名称的校验器：" + registry.name());
        }
        NOTIFY_CACHE.putIfAbsent(registry, properties);
    }

    /**
     * 返回type对象
     *
     * @param registry {@link Registry}
     * @return {@link Provider}
     */
    public Provider get(Registry registry) {
        if (Registry.ALIYUN_SMS.equals(registry)) {
            return new AliyunSmsProvider((AliyunSmsProperties) NOTIFY_CACHE.get(registry));
        } else if (Registry.ALIYUN_VMS.equals(registry)) {
            return new AliyunVmsProvider((AliyunVmsProperties) NOTIFY_CACHE.get(registry));
        } else if (Registry.DINGTALK_CORP_MSG.equals(registry)) {
            return new DingTalkCropMsgProvider((DingTalkProperties) NOTIFY_CACHE.get(registry));
        } else if (Registry.NETEASE_ATTACH_MSG.equals(registry)) {
            return new NeteaseAttachMsgProvider((NeteaseProperties) NOTIFY_CACHE.get(registry));
        } else if (Registry.NETEASE_MSG.equals(registry)) {
            return new NeteaseMsgProvider((NeteaseProperties) NOTIFY_CACHE.get(registry));
        }
        return null;
    }


}
