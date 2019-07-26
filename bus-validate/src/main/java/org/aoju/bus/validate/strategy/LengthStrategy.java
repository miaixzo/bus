package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.Length;
import org.aoju.bus.validate.validators.Complex;

import java.util.Collection;
import java.util.Map;

/**
 * 数据长度校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class LengthStrategy implements Complex<Object, Length> {

    @Override
    public boolean on(Object object, Length annotation, Context context) {
        if (ObjectUtils.isEmpty(object)) {
            return true;
        }

        int num;
        if (object instanceof String) {
            num = ((String) object).length();
        } else if (object.getClass().isArray()) {
            num = ((Object[]) object).length;
        } else if (object instanceof Collection) {
            num = ((Collection) object).size();
        } else if (object instanceof Map) {
            num = ((Map) object).keySet().size();
        } else {
            throw new IllegalArgumentException("不支持的检查长度的对象类型:" + object.getClass());
        }
        return (annotation.zeroAble() && num == 0) || (num >= annotation.min() && num <= annotation.max());
    }

}
