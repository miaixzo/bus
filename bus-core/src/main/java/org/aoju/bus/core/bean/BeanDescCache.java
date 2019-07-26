package org.aoju.bus.core.bean;


import org.aoju.bus.core.lang.SimpleCache;

/**
 * Bean属性缓存<br>
 * 缓存用于防止多次反射造成的性能问题
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum BeanDescCache {
    INSTANCE;

    private SimpleCache<Class<?>, BeanDesc> bdCache = new SimpleCache<>();

    /**
     * 获得属性名和{@link BeanDesc}Map映射
     *
     * @param beanClass Bean的类
     * @return 属性名和{@link BeanDesc}映射
     */
    public BeanDesc getBeanDesc(Class<?> beanClass) {
        return bdCache.get(beanClass);
    }

    /**
     * 加入缓存
     *
     * @param beanClass Bean的类
     * @param BeanDesc  属性名和{@link BeanDesc}映射
     */
    public void putBeanDesc(Class<?> beanClass, BeanDesc BeanDesc) {
        bdCache.put(beanClass, BeanDesc);
    }
}
