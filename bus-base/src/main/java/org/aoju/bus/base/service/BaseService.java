package org.aoju.bus.base.service;

import org.aoju.bus.base.entity.Result;

import java.util.List;

/**
 * <p>
 * BaseService 接口.
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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