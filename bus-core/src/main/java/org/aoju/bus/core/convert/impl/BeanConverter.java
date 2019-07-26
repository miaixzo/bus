package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.bean.copier.BeanCopier;
import org.aoju.bus.core.bean.copier.CopyOptions;
import org.aoju.bus.core.bean.copier.ValueProvider;
import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.core.utils.ReflectUtils;

import java.util.Map;

/**
 * Bean转换器，支持：
 * <pre>
 * Map =》 Bean
 * Bean =》 Bean
 * ValueProvider =》 Bean
 * </pre>
 *
 * @param <T> Bean类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BeanConverter<T> extends AbstractConverter<T> {

    private Class<T> beanClass;
    private CopyOptions copyOptions;

    /**
     * 构造，默认转换选项，注入失败的字段忽略
     *
     * @param beanClass 转换成的目标Bean类
     */
    public BeanConverter(Class<T> beanClass) {
        this(beanClass, CopyOptions.create().setIgnoreError(true));
        this.beanClass = beanClass;
    }

    /**
     * 构造
     *
     * @param beanClass   转换成的目标Bean类
     * @param copyOptions Bean转换选项参数
     */
    public BeanConverter(Class<T> beanClass, CopyOptions copyOptions) {
        this.beanClass = beanClass;
        this.copyOptions = copyOptions;
    }

    @Override
    protected T convertInternal(Object value) {
        if (value instanceof Map || value instanceof ValueProvider || BeanUtils.isBean(value.getClass())) {
            //限定被转换对象类型
            return BeanCopier.create(value, ReflectUtils.newInstanceIfPossible(this.beanClass), copyOptions).copy();
        }
        return null;
    }

    @Override
    public Class<T> getTargetType() {
        return this.beanClass;
    }

}
