package org.aoju.bus.setting;

import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Setting工具类<br>
 * 提供静态方法获取配置文件
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SettingUtils {

    /**
     * 配置文件缓存
     */
    private static Map<String, Setting> settingMap = new ConcurrentHashMap<>();
    private static Object lock = new Object();

    /**
     * 获取当前环境下的配置文件<br>
     * name可以为不包括扩展名的文件名（默认.setting为结尾），也可以是文件名全称
     *
     * @param name 文件名，如果没有扩展名，默认为.setting
     * @return 当前环境下配置文件
     */
    public static Setting get(String name) {
        Setting setting = settingMap.get(name);
        if (null == setting) {
            synchronized (lock) {
                setting = settingMap.get(name);
                if (null == setting) {
                    String filePath = name;
                    String extName = FileUtils.extName(filePath);
                    if (StringUtils.isEmpty(extName)) {
                        filePath = filePath + "." + Setting.EXT_NAME;
                    }
                    setting = new Setting(filePath, true);
                    settingMap.put(name, setting);
                }
            }
        }
        return setting;
    }

}
