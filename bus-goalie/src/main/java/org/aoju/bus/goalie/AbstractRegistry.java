package org.aoju.bus.goalie;

import org.aoju.bus.core.collection.ConcurrentHashSet;
import org.springframework.beans.factory.InitializingBean;

import java.util.Set;

/**
 * 抽象注册类
 *
 * @author Justubborn
 * @version 6.1.6
 * @since JDK 1.8+
 */
public abstract class AbstractRegistry implements Registry, InitializingBean {

    private final Set<Assets> assets = new ConcurrentHashSet<>();

    @Override
    public abstract void init();

    @Override
    public boolean add(Assets assets) {
        return this.assets.add(assets);
    }

    @Override
    public boolean remove(String id) {
        Assets assets = new Assets();
        assets.setId(id);
        return this.assets.remove(assets);
    }

    @Override
    public boolean amendAssets(Assets assets) {
        if (this.assets.contains(assets)) {
            this.assets.remove(assets);
            return this.assets.add(assets);
        } else {
            return false;
        }
    }

    @Override
    public void refresh() {
        assets.clear();
        init();
    }

    @Override
    public Set<Assets> getAssets() {
        return assets;
    }

    @Override
    public void afterPropertiesSet()  {
        refresh();
    }
}
