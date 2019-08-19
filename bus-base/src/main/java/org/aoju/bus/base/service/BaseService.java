/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.base.service;

import org.aoju.bus.base.entity.Result;

import java.util.List;

/**
 * <p>
 * BaseService 接口.
 * </p>
 *
 * @author Kimi Liu
 * @version 3.1.0
 * @since JDK 1.8
 */
public interface BaseService<T> extends Service {

    /*  添加数据*/
    String insert(T entity);

    String insertSelective(T entity);

    Object insertBatch(List<T> list);

    Object insertBatchSelective(List<T> list);

    /*  删除数据*/
    void delete(T entity);

    void deleteById(Object id);

    int deleteByIds(String id);

    int deleteByWhere(Object object);


    /*  更新数据*/
    void updateById(T entity);

    int updateSelectiveById(T entity);

    T updateByIdCas(T entity, String locking);

    T updateSelectiveByIdOrInsert(T entity);

    int updateByWhere(T entity, Object object);

    int updateByWhereSelective(T entity, Object object);

    int updateStatus(T entity);

    /*  查询数据*/
    T selectOne(T entity);

    T selectById(Object id);

    int selectCountByWhere(Object object);

    Long selectCount(T entity);

    List<T> selectListByIds(String id);

    List<T> selectList(T entity);

    List<T> selectListAll();

    List<T> selectByWhere(Object object);

    Result<T> page(int pageNum, int pageSize, T entity, String... params);

    Result<T> page(String pageNum, String pageSize, T entity, String... params);
}
