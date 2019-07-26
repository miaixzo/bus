package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.NumberUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.IntRange;
import org.aoju.bus.validate.validators.Complex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * INT RANGE 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class IntRangeStrategy implements Complex<Object, IntRange> {

    private static Set<Class<?>> NumberTypes = new HashSet<>();

    static {
        NumberTypes.add(Integer.class);
        NumberTypes.add(Long.class);
        NumberTypes.add(Double.class);
        NumberTypes.add(Float.class);
        NumberTypes.add(int.class);
        NumberTypes.add(long.class);
        NumberTypes.add(double.class);
        NumberTypes.add(float.class);
        NumberTypes.add(BigDecimal.class);
        NumberTypes.add(BigInteger.class);
    }

    @Override
    public boolean on(Object object, IntRange annotation, Context context) {
        if (ObjectUtils.isEmpty(object)) {
            return true;
        }
        BigDecimal num;
        if (object instanceof String) {
            num = NumberUtils.add((String) object);
        } else if (NumberTypes.contains(object.getClass())) {
            String numString = String.valueOf(object);
            num = NumberUtils.add(numString);
        } else {
            throw new IllegalArgumentException("不支持的数字格式:" + object.toString());
        }
        BigDecimal max = new BigDecimal(annotation.max());
        BigDecimal min = new BigDecimal(annotation.min());

        return max.compareTo(num) >= 0 && min.compareTo(num) <= 0;
    }

}
