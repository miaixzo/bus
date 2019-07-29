package org.aoju.bus.sensitive;

import org.aoju.bus.core.lang.exception.InstrumentException;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 脱敏的执行上下文
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class Context {

    /**
     * 当前对象
     */
    private Object currentObject;

    /**
     * 当前字段
     */
    private Field currentField;

    /**
     * 所有字段
     */
    private List<Field> allFieldList = new ArrayList<>();

    /**
     * 类信息
     *
     * @since 0.0.6
     */
    private Class beanClass;

    /**
     * 明细信息
     *
     * @since 0.0.6
     */
    private Object entry;

    /**
     * 新建一个对象实例
     *
     * @return this
     * @since 0.0.6
     */
    public static Context newInstance() {
        return new Context();
    }

    /**
     * 获取当前字段名称
     *
     * @return 字段名称
     * @since 0.0.4
     */
    public String getCurrentFieldName() {
        return this.currentField.getName();
    }


    /**
     * 获取当前字段值
     *
     * @return 字段值
     * @since 0.0.4
     */
    public Object getCurrentFieldValue() {
        try {
            return this.currentField.get(this.currentObject);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(e);
        }
    }

}
