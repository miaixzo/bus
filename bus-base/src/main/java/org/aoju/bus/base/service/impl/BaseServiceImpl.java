/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.base.service.impl;

import org.aoju.bus.base.consts.Consts;
import org.aoju.bus.base.entity.BaseEntity;
import org.aoju.bus.base.entity.Result;
import org.aoju.bus.base.mapper.BaseMapper;
import org.aoju.bus.base.service.BaseService;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.EntityUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.mapper.entity.Condition;
import org.aoju.bus.pager.Page;
import org.aoju.bus.pager.PageContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * BaseService 接口实现
 * </p>
 *
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
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
        return mapper.deleteByIds(StringUtils.split(id));
    }

    @Override
    public int deleteByWhere(Object object) {
        return mapper.deleteByWhere(object);
    }

    @Override
    public void updateById(T entity) {
        EntityUtils.setUpdatedInfo(entity);
        mapper.updateByPrimaryKey(entity);
    }

    @Override
    public int updateSelectiveById(T entity) {
        EntityUtils.setUpdatedInfo(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    public T updateByIdCas(T entity, String locking) {
        Condition condition = new Condition(entity.getClass());
        Object before = ObjectUtils.getAttributeValue(entity, locking);
        Object id = ObjectUtils.getAttributeValue(entity, "id");
        condition.createCriteria().andEqualTo(locking, before);
        condition.createCriteria().andEqualTo("id", id);
        updateByWhereSelective(entity, condition);
        return entity;
    }

    @Override
    public T updateSelectiveByIdOrInsert(T entity) {
        EntityUtils.setUpdatedInfo(entity);
        mapper.updateByPrimaryKeySelective(entity);
        return entity;
    }

    @Override
    public int updateByWhere(T entity, Object object) {
        EntityUtils.setUpdatedInfo(entity);
        return mapper.updateByWhere(entity, object);
    }

    @Override
    public int updateByWhereSelective(T entity, Object object) {
        EntityUtils.setUpdatedInfo(entity);
        return mapper.updateByWhereSelective(entity, object);
    }

    @Override
    public int updateStatus(T entity) {
        EntityUtils.setUpdatedInfo(entity);
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
        return mapper.selectCountByWhere(object);
    }

    @Override
    public Long selectCount(T entity) {
        return new Long(mapper.selectCount(entity));
    }

    @Override
    public List<T> selectListByIds(String id) {
        return mapper.selectByIds(StringUtils.split(id));
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
        return mapper.selectByWhere(where);
    }

    @Override
    public Result<T> page(int pageNo, int pageSize, T entity, String... orderBy) {
        PageContext.startPage(pageNo, pageSize);
        if (ArrayUtils.isNotEmpty(orderBy)) {
            PageContext.orderBy(orderBy[0]);
        }
        Page<T> list = (Page<T>) mapper.select(entity);
        return new Result<>((int) list.getTotal(), list.getResult());
    }

    @Override
    public Result<T> page(String pageNo, String pageSize, T entity, String... orderBy) {
        return page(Integer.parseInt(pageNo), Integer.parseInt(pageSize), entity, orderBy);
    }

    private String setValue(T entity) {
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }
        if (StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus(Consts.STATUS_ENABLED);
        }
        EntityUtils.setCreateInfo(entity);
        EntityUtils.setUpdatedInfo(entity);
        return entity.getId();
    }

}
