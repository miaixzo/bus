package org.aoju.bus.goalie.registry;

import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.Registry;

/**
 * 路由注册
 *
 * @author Justubborn
 * @version 6.2.8
 * @since JDK 1.8+
 */
public interface AssetsRegistry extends Registry<Assets> {

    void addAssets(Assets assets);

    void amendAssets(Assets assets);

    Assets getAssets(String method, String version);

}
