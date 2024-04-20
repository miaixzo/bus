/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.base.entity;

import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.miaixz.bus.base.normal.Consts;
import org.miaixz.bus.core.key.ObjectID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.*;

import java.util.List;
import java.util.Objects;

/**
 * Entity 基本信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseEntity extends Tracer {

    private static final long serialVersionUID = 1L;

    /**
     * 数据状态:-1删除,0无效,1正常
     */
    protected String status;

    /**
     * 创建者
     */
    protected String creator;

    /**
     * 创建时间
     */
    protected String created;

    /**
     * 修改者
     */
    protected String modifier;

    /**
     * 修改时间
     */
    protected String modified;

    /**
     * 搜索参数
     */
    @Transient
    protected transient String params;

    /**
     * 分页页码
     */
    @Transient
    protected transient Integer pageNo;

    /**
     * 分页大小
     */
    @Transient
    protected transient Integer pageSize;

    /**
     * 排序方式,asc desc
     */
    @Transient
    protected transient String orderBy;

    /**
     * 设置访问信息
     *
     * @param <T>    对象泛型
     * @param source 源始实体
     * @param target 目标实体
     */
    public static <T extends BaseEntity> void setAccess(T source, T target) {
        if (Objects.isNull(source) || Objects.isNull(target)) {
            return;
        }
        target.setX_org_id(source.getX_org_id());
        target.setX_user_id(source.getX_user_id());
    }

    /**
     * 设置访问信息
     *
     * @param <T>    对象泛型
     * @param source 源始实体
     * @param target 目标实体
     */
    public static <T extends BaseEntity> void setAccess(T source, T... target) {
        if (Objects.isNull(source) || ArrayKit.isEmpty(target)) {
            return;
        }
        for (T targetEntity : target) {
            setAccess(source, targetEntity);
        }
    }

    /**
     * 设置访问信息
     *
     * @param <S>    源对象泛型
     * @param <E>    集合元素对象泛型
     * @param source 源始实体
     * @param target 目标实体
     */
    public static <S extends BaseEntity, E extends BaseEntity> void setAccess(S source, List<E> target) {
        if (Objects.isNull(source) || CollKit.isEmpty(target)) {
            return;
        }
        target.forEach(targetEntity -> setAccess(source, targetEntity));
    }

    /**
     * 重置数字型字符串为null，防止插入数据库表异常
     *
     * @param <T>    对象泛型
     * @param entity 实体对象
     * @param fields 数字型字符串属性数组
     * @param values 值数据
     */
    public static <T extends BaseEntity> void resetIntField(T entity, String[] fields, String[] values) {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (Consts.EMPTY.equals(values[i]) && ReflectKit.hasField(entity, field)) {
                ReflectKit.invokeSetter(entity, field, null);
            }
        }
    }

    /**
     * 快速将bean的creator、created附上相关值
     *
     * @param <T>    对象
     * @param entity 反射对象
     */
    public <T> void setCreateInfo(T entity) {
        String id = ObjectKit.isEmpty(getValue(entity, "id")) ? ObjectID.id() : (String) getValue(entity, "id");
        String timestamp = StringKit.toString(DateKit.timestamp());
        String[] fields = {"id", "created"};
        Object[] value = new Object[]{id, timestamp};
        if (ObjectKit.isEmpty(getValue(entity, "creator"))) {
            fields = new String[]{"id", "creator", "created"};
            value = new Object[]{id,
                    ObjectKit.isEmpty(getValue(entity, "x_user_id")) ? "-1" : getValue(entity, "x_user_id"),
                    timestamp};
        }
        setValue(entity, fields, value);
    }

    /**
     * 快速将bean的modifier、modified附上相关值
     *
     * @param <T>    对象
     * @param entity 反射对象
     */
    public <T> void setUpdatedInfo(T entity) {
        String timestamp = StringKit.toString(DateKit.timestamp());
        String[] fields = {"modified"};
        Object[] value = new Object[]{timestamp};
        if (ObjectKit.isEmpty(getValue(entity, "modifier"))) {
            fields = new String[]{"modifier", "modified"};
            value = new Object[]{ObjectKit.isEmpty(getValue(entity, "x_user_id")) ? "-1" : getValue(entity, "x_user_id"),
                    timestamp};
        }

        setValue(entity, fields, value);
    }

    public <T> void setCreatAndUpdatInfo(T entity) {
        setCreateInfo(entity);
        setUpdatedInfo(entity);
    }

    /**
     * 根据主键属性,判断主键是否值为空
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param field  属性
     * @return 主键为空, 则返回false；主键有值,返回true
     */
    public <T> boolean isPKNotNull(T entity, String field) {
        if (!ReflectKit.hasField(entity, field)) {
            return false;
        }
        Object value = ReflectKit.getFieldValue(entity, field);
        return null != value && !Normal.EMPTY.equals(value);
    }

    /**
     * 依据对象的属性获取对象值
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param field  属性数组
     * @return 返回对象属性值
     */
    public <T> Object getValue(T entity, String field) {
        if (ReflectKit.hasField(entity, field)) {
            Object object = ReflectKit.invokeGetter(entity, field);
            return null != object ? object.toString() : null;
        }
        return null;
    }

    /**
     * 依据对象的属性数组和值数组对进行赋值
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param fields 属性数组
     * @param value  值数组
     */
    public <T> void setValue(T entity, String[] fields, Object[] value) {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (ReflectKit.hasField(entity, field)) {
                ReflectKit.invokeSetter(entity, field, value[i]);
            }
        }
    }

}