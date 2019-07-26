package org.aoju.bus.setting.profile;

import org.aoju.bus.core.lang.Singleton;
import org.aoju.bus.setting.Setting;

/**
 * 全局的Profile配置中心
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class GlobalProfile {

    private GlobalProfile() {
    }

    /**
     * 设置全局环境
     *
     * @param profile 环境
     * @return {@link Profile}
     */
    public static Profile setProfile(String profile) {
        return Singleton.get(Profile.class, profile);
    }

    /**
     * 获得全局的当前环境下对应的配置文件
     *
     * @param settingName 配置文件名，可以忽略默认后者（.setting）
     * @return {@link Setting}
     */
    public static Setting getSetting(String settingName) {
        return Singleton.get(Profile.class).getSetting(settingName);
    }

}
