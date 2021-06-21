package org.aoju.bus.goalie.registry;

import org.aoju.bus.goalie.Assets;


/**
 * 抽象路由注册
 *
 * @author Justubborn
 * @version 6.2.3
 * @since JDK 1.8+
 */
public class DefaultAssetsRegistry extends AbstractRegistry<Assets> implements AssetsRegistry {

    @Override
    public void addAssets(Assets assets) {
        super.add(assets.getMethod() + assets.getVersion(), assets);
    }

    @Override
    public void amendAssets(Assets assets) {
        super.amend(assets.getMethod() + assets.getVersion(), assets);
    }

    @Override
    public Assets getAssets(String method, String version) {
        return get(method + version);
    }

    @Override
    public void init() {

    }

}

