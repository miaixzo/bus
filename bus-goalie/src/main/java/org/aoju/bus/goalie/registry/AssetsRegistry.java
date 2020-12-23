package org.aoju.bus.goalie.registry;

import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.Registry;

/**
 * 路由注册
 *
 * @author Justubborn
 * @since 2020/12/22
 */
public interface AssetsRegistry extends Registry<Assets> {

    void addAssets(Assets assets);

    void amendAssets(Assets assets);

    Assets getAssets(String method, String version);
}
