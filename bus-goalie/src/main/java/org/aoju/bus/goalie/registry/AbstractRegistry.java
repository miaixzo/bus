package org.aoju.bus.goalie.registry;

import org.aoju.bus.goalie.Registry;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象注册类
 *
 * @author Justubborn
 * @version 6.2.8
 * @since JDK 1.8+
 */
public abstract class AbstractRegistry<T> implements Registry<T>, InitializingBean {

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    @Override
    public abstract void init();

    @Override
    public boolean add(String key, T reg) {
        if (null != cache.get(key)) {
            return false;
        }
        cache.put(key, reg);
        return true;
    }

    @Override
    public boolean remove(String id) {
        return null != this.cache.remove(id);
    }

    @Override
    public boolean amend(String key, T reg) {
        cache.remove(key);
        return add(key, reg);

    }

    @Override
    public void refresh() {
        cache.clear();
        init();
    }

    @Override
    public T get(String key) {
        return cache.get(key);
    }

    @Override
    public void afterPropertiesSet() {
        refresh();
    }

}
