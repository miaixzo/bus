package org.aoju.bus.office.metric;

import org.aoju.bus.office.Provider;

/**
 * 保存{@link OfficeManager}的唯一实例，
 * 当没有向转换器生成器提供office管理器时，
 * 创建的{@link Provider}将使用该实例.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public final class InstalledOfficeHolder {

    private static OfficeManager instance;

    /**
     * 获取静态holder类的静态实例.
     *
     * @return 主默认的office管理器.
     */
    public static OfficeManager getInstance() {
        synchronized (InstalledOfficeHolder.class) {
            return instance;
        }
    }

    /**
     * 设置静态holder类的静态实例.
     *
     * @param manager 主默认的office管理器.
     * @return 以前安装的office管理器，如果没有安装office管理器，则为{@code null}.
     */
    public static OfficeManager setInstance(final OfficeManager manager) {
        synchronized (InstalledOfficeHolder.class) {
            final OfficeManager oldManager = instance;
            instance = manager;
            return oldManager;
        }
    }

}
