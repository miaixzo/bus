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
package org.miaixz.bus.base.service;

import org.miaixz.bus.base.entity.Result;

import java.util.List;

/**
 * BaseService 接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BaseService<T> extends Service {

    /**
     * 通用:添加数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    String insert(T entity);

    /**
     * 通用:选择添加数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    String insertSelective(T entity);

    /**
     * 通用:批量添加数据
     *
     * @param list 对象参数
     * @return 操作结果
     */
    Object insertBatch(List<T> list);

    /**
     * 通用:批量选择添加数据
     *
     * @param list 对象参数
     * @return 操作结果
     */
    Object insertBatchSelective(List<T> list);

    /**
     * 通用:删除数据
     *
     * @param entity 对象参数
     */
    void delete(T entity);

    /**
     * 通用:删除数据
     *
     * @param id 对象主键
     */
    void deleteById(Object id);

    /**
     * 通用:删除数据
     *
     * @param id 多个对象主键
     * @return 操作结果
     */
    int deleteByIds(String id);

    /**
     * 通用:删除数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    int deleteByWhere(Object entity);

    /**
     * 通用:更新数据
     *
     * @param entity 对象参数
     */
    void updateById(T entity);

    /**
     * 通用:更新数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    int updateSelectiveById(T entity);

    /**
     * 通用:更新数据
     *
     * @param entity  对象参数
     * @param locking 锁定
     * @return 操作结果
     */
    T updateByIdCas(T entity, String locking);

    /**
     * 通用:更新添加数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    T updateSelectiveByIdOrInsert(T entity);

    /**
     * 通用:多条件更新数据
     *
     * @param entity 对象参数
     * @param object 条件
     * @return 操作结果
     */
    int updateByWhere(T entity, Object object);

    /**
     * 通用:选择更新数据
     *
     * @param entity 对象参数
     * @param object 条件
     * @return 操作结果
     */
    int updateByWhereSelective(T entity, Object object);

    /**
     * 通用:更新状态
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    int updateStatus(T entity);

    /**
     * 通用:查询数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    T selectOne(T entity);

    /**
     * 通用:查询数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    T selectById(Object entity);

    /**
     * 通用:查询统计数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    int selectCountByWhere(Object entity);

    /**
     * 通用:查询统计数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    int selectCount(T entity);

    /**
     * 通用:查询统计数据
     *
     * @param id 对象参数
     * @return 操作结果
     */
    List<T> selectListByIds(String id);

    /**
     * 通用:查询统计数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    List<T> selectList(T entity);

    /**
     * 通用:查询所有数据
     *
     * @return 操作结果
     */
    List<T> selectListAll();

    /**
     * 通用:多条件查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    List<T> selectByWhere(Object entity);

    /**
     * 通用:多条件分页查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    Result<T> page(T entity);

}
