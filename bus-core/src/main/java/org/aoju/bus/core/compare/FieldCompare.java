package org.aoju.bus.core.compare;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * Bean字段排序器<br>
 * 参阅feilong-core中的PropertyComparator
 *
 * @param <T> 被比较的Bean
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class FieldCompare<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Field field;

    /**
     * 构造
     *
     * @param beanClass Bean类
     * @param fieldName 字段名
     */
    public FieldCompare(Class<T> beanClass, String fieldName) {
        this.field = ClassUtils.getDeclaredField(beanClass, fieldName);
        if (this.field == null) {
            throw new IllegalArgumentException(StringUtils.format("Field [{}] not found in Class [{}]", fieldName, beanClass.getName()));
        }
    }

    @Override
    public int compare(T o1, T o2) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1) {// null 排在后面
            return 1;
        } else if (null == o2) {
            return -1;
        }

        java.lang.Comparable<?> v1;
        java.lang.Comparable<?> v2;
        try {
            v1 = (java.lang.Comparable<?>) ReflectUtils.getFieldValue(o1, this.field);
            v2 = (java.lang.Comparable<?>) ReflectUtils.getFieldValue(o2, this.field);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }

        return compare(o1, o2, v1, v2);
    }

    private int compare(T o1, T o2, java.lang.Comparable fieldValue1, java.lang.Comparable fieldValue2) {
        int result = ObjectUtils.compare(fieldValue1, fieldValue2);
        if (0 == result) {
            //避免TreeSet / TreeMap 过滤掉排序字段相同但是对象不相同的情况
            result = CompareUtils.compare(o1, o2, true);
        }
        return result;
    }

}
