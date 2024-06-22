/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.base.service.impl;

import org.miaixz.bus.base.mapper.BaseMapper;
import org.miaixz.bus.core.basics.entity.BaseEntity;
import org.miaixz.bus.core.basics.entity.Result;
import org.miaixz.bus.core.basics.normal.Consts;
import org.miaixz.bus.core.basics.service.BaseService;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.entity.Condition;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.PageContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于spring 实现BaseService 接口
 * 根据业务需要如无status，creator等相关属性内容
 * 重写此类及{@link BaseEntity} 业务类继承新类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaseServiceImpl<Mapper extends BaseMapper<T>, T extends BaseEntity>
        implements BaseService<T> {

    @Autowired
    protected Mapper mapper;

    @Override
    public String insert(T entity) {
        this.setValue(entity);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public String insertSelective(T entity) {
        this.setValue(entity);
        mapper.insertSelective(entity);
        return entity.getId();
    }

    @Override
    public Object insertBatch(List<T> list) {
        List<String> data = new ArrayList<>();
        list.forEach(item -> {
            String id = insertSelective(item);
            data.add(id);
        });
        return data;
    }

    @Override
    public Object insertBatchSelective(List<T> list) {
        List<String> data = new ArrayList<>();
        list.forEach(item -> {
            String id = insertSelective(item);
            data.add(id);
        });
        return data;
    }

    @Override
    public void delete(T entity) {
        mapper.delete(entity);
    }

    @Override
    public void deleteById(Object id) {
        mapper.deleteByPrimaryKey(id);
    }

    @Override
    public int deleteByIds(String id) {
        return mapper.deleteByIds(StringKit.split(id));
    }

    @Override
    public int deleteByWhere(Object object) {
        return mapper.deleteByCondition(object);
    }

    @Override
    public void updateById(T entity) {
        entity.setUpdatedInfo(entity);
        mapper.updateByPrimaryKey(entity);
    }

    @Override
    public int updateSelectiveById(T entity) {
        entity.setUpdatedInfo(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    public T updateByIdCas(T entity, String locking) {
        Condition condition = new Condition(entity.getClass());
        Object before = FieldKit.getFieldValue(entity, locking);
        Object id = FieldKit.getFieldValue(entity, "id");
        condition.createCriteria().andEqualTo(locking, before);
        condition.createCriteria().andEqualTo("id", id);
        updateByWhereSelective(entity, condition);
        return entity;
    }

    @Override
    public T updateSelectiveByIdOrInsert(T entity) {
        if (StringKit.isEmpty(entity.getId())) {
            this.insert(entity);
        } else {
            entity.setUpdatedInfo(entity);
            mapper.updateByPrimaryKeySelective(entity);
        }
        return entity;
    }

    @Override
    public int updateByWhere(T entity, Object object) {
        entity.setUpdatedInfo(entity);
        return mapper.updateByCondition(entity, object);
    }

    @Override
    public int updateByWhereSelective(T entity, Object object) {
        entity.setUpdatedInfo(entity);
        return mapper.updateByConditionSelective(entity, object);
    }

    @Override
    public int updateStatus(T entity) {
        entity.setUpdatedInfo(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    public T selectOne(T entity) {
        return mapper.selectOne(entity);
    }

    @Override
    public T selectById(Object id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public int selectCountByWhere(Object object) {
        return mapper.selectCountByCondition(object);
    }

    @Override
    public int selectCount(T entity) {
        return mapper.selectCount(entity);
    }

    @Override
    public List<T> selectListByIds(String id) {
        return mapper.selectByIds(StringKit.split(id));
    }

    @Override
    public List<T> selectList(T entity) {
        return mapper.select(entity);
    }

    @Override
    public List<T> selectListAll() {
        return mapper.selectAll();
    }

    @Override
    public List<T> selectByWhere(Object where) {
        return mapper.selectByCondition(where);
    }

    @Override
    public Result<T> page(T entity) {
        PageContext.startPage(entity.getPageNo(), entity.getPageSize());
        if (StringKit.isNotEmpty(entity.getOrderBy())) {
            PageContext.orderBy(entity.getOrderBy());
        }
        Page<T> list = (Page<T>) mapper.select(entity);
        return Result.<T>builder()
                .rows(list.getResult())
                .total((int) list.getTotal())
                .build();
    }

    private String setValue(T entity) {
        if (ObjectKit.isEmpty(entity)) {
            return null;
        }
        if (StringKit.isEmpty(entity.getStatus())) {
            entity.setStatus(Consts.STATUS_ONE);
        }
        entity.setCreateInfo(entity);
        entity.setUpdatedInfo(entity);
        return entity.getId();
    }

}
