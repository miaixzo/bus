package org.aoju.bus.core.bean.copier.provider;

import org.aoju.bus.core.bean.BeanDesc;
import org.aoju.bus.core.bean.copier.ValueProvider;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Bean的值提供者
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BeanValueProvider implements ValueProvider<String> {

    final Map<String, BeanDesc.PropDesc> sourcePdMap;
    private Object source;
    private boolean ignoreError;

    /**
     * 构造
     *
     * @param bean        Bean
     * @param ignoreCase  是否忽略字段大小写
     * @param ignoreError 是否忽略字段值读取错误
     */
    public BeanValueProvider(Object bean, boolean ignoreCase, boolean ignoreError) {
        this.source = bean;
        this.ignoreError = ignoreError;
        sourcePdMap = BeanUtils.getBeanDesc(source.getClass()).getPropMap(ignoreCase);
    }

    @Override
    public Object value(String key, Type valueType) {
        BeanDesc.PropDesc sourcePd = sourcePdMap.get(key);
        if (null == sourcePd && (Boolean.class == valueType || boolean.class == valueType)) {
            //boolean类型字段字段名支持两种方式
            sourcePd = sourcePdMap.get(StringUtils.upperFirstAndAddPre(key, "is"));
        }

        if (null != sourcePd) {
            final Method getter = sourcePd.getGetter();
            if (null != getter) {
                try {
                    return getter.invoke(source);
                } catch (Exception e) {
                    if (false == ignoreError) {
                        throw new CommonException("Inject [{}] error!", key);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return sourcePdMap.containsKey(key) || sourcePdMap.containsKey(StringUtils.upperFirstAndAddPre(key, "is"));
    }

}
