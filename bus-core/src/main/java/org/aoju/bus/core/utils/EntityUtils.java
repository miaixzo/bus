package org.aoju.bus.core.utils;

import org.aoju.bus.core.key.ObjectID;

/**
 * 实体类相关工具类
 * 解决问题： 1、快速对实体的常驻字段
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class EntityUtils {

    public static <T> void setCreatAndUpdatInfo(T entity) {
        setCreateInfo(entity);
        setUpdatedInfo(entity);
    }

    /**
     * 快速将bean的creator、created附上相关值
     */
    public static <T> void setCreateInfo(T entity) {
        String id = ObjectID.id();
        String[] fields = {"id", "creator", "created"};
        Object[] value = new Object[]{id, getValue(entity, "x_user_id"), DateUtils.getTimestamp()};
        setValue(entity, fields, value);
    }

    /**
     * 快速将bean的modifier、modified附上相关值
     *
     * @param entity 实体bean
     */
    public static <T> void setUpdatedInfo(T entity) {
        String[] fields = {"modifier", "modified"};
        Object[] value = new Object[]{getValue(entity, "x_user_id"), DateUtils.getTimestamp()};
        setValue(entity, fields, value);
    }

    /**
     * 依据对象的属性数组和值数组对进行赋值
     *
     * @param entity 对象
     * @param fields 属性数组
     * @param value  值数组
     */
    private static <T> void setValue(T entity, String[] fields, Object[] value) {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (ReflectUtils.hasField(entity, field)) {
                ReflectUtils.invokeSetter(entity, field, value[i]);
            }
        }
    }

    /**
     * 依据对象的属性获取对象值
     *
     * @param entity 对象
     * @param field  属性数组
     */
    private static <T> Object getValue(T entity, String field) {
        if (ReflectUtils.hasField(entity, field)) {
            Object object = ReflectUtils.invokeGetter(entity, field);
            return object != null ? object.toString() : null;
        }
        return null;
    }

    /**
     * 根据主键属性，判断主键是否值为空
     *
     * @param entity
     * @param field
     * @return 主键为空，则返回false；主键有值，返回true
     */
    public static <T> boolean isPKNotNull(T entity, String field) {
        if (!ReflectUtils.hasField(entity, field)) {
            return false;
        }
        Object value = ReflectUtils.getFieldValue(entity, field);
        return value != null && !"".equals(value);
    }

}
