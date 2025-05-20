/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.base.entity;

import java.util.List;

import org.miaixz.bus.core.basic.entity.Tracer;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.mapper.annotation.Logical;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity 基本信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity extends Tracer {

    private static final long serialVersionUID = -1L;

    /**
     * 数据状态:-1删除,0无效,1正常
     */
    @Logical
    protected Integer status;

    /**
     * 创建者
     */
    protected String creator;

    /**
     * 创建时间
     */
    protected Long created;

    /**
     * 修改者
     */
    protected String modifier;

    /**
     * 修改时间
     */
    protected Long modified;

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
            if (Consts.EMPTY.equals(values[i]) && FieldKit.hasField(entity.getClass(), field)) {
                MethodKit.invokeSetter(entity, field, null);
            }
        }
    }

    /**
     * 设置访问信息
     *
     * @param <T>    对象泛型
     * @param source 源始实体
     * @param target 目标实体
     */
    public <T extends BaseEntity> void setAccess(T source, T target) {
        if (ObjectKit.isNull(source) || ObjectKit.isNull(target)) {
            return;
        }
        target.setX_tenant_id(source.getX_tenant_id());
        target.setX_user_id(source.getX_user_id());
    }

    /**
     * 设置访问信息
     *
     * @param <T>    对象泛型
     * @param source 源始实体
     * @param target 目标实体
     */
    public <T extends BaseEntity> void setAccess(T source, T... target) {
        if (ObjectKit.isNull(source) || ArrayKit.isEmpty(target)) {
            return;
        }
        for (T targetEntity : target) {
            this.setAccess(source, targetEntity);
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
    public <S extends BaseEntity, E extends BaseEntity> void setAccess(S source, List<E> target) {
        if (ObjectKit.isNull(source) || CollKit.isEmpty(target)) {
            return;
        }
        target.forEach(targetEntity -> this.setAccess(source, targetEntity));
    }

    /**
     * 快速设置操作者属性值
     *
     * @param <T>    对象
     * @param entity 反射对象
     */
    public <T> void setInsert(T entity) {
        String id = ObjectKit.isEmpty(getValue(entity, "id")) ? ID.objectId() : (String) getValue(entity, "id");
        String timestamp = StringKit.toString(DateKit.current());
        String[] fields = { "id", "created" };
        Object[] value = new Object[] { id, timestamp };
        if (ObjectKit.isEmpty(getValue(entity, "creator"))) {
            fields = new String[] { "id", "creator", "created" };
            value = new Object[] { id,
                    ObjectKit.isEmpty(getValue(entity, "x_user_id")) ? "-1" : getValue(entity, "x_user_id"),
                    timestamp };
        }
        this.setValue(entity, fields, value);
    }

    /**
     * 快速设置操作者属性值
     *
     * @param <T>    泛型对象
     * @param entity 反射对象
     */
    public <T> void setUpdate(T entity) {
        String timestamp = StringKit.toString(DateKit.current());
        String[] fields = { "modified" };
        Object[] value = new Object[] { timestamp };
        if (ObjectKit.isEmpty(getValue(entity, "modifier"))) {
            fields = new String[] { "modifier", "modified" };
            value = new Object[] {
                    ObjectKit.isEmpty(getValue(entity, "x_user_id")) ? "-1" : getValue(entity, "x_user_id"),
                    timestamp };
        }

        this.setValue(entity, fields, value);
    }

    /**
     * 快速设置操作者属性值
     *
     * @param entity 反射对象
     * @param <T>    泛型对象
     */
    public <T> void setValue(T entity) {
        this.setInsert(entity);
        this.setUpdate(entity);
    }

}