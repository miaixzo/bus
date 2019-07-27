package org.aoju.bus.mapper.builder;

import org.aoju.bus.mapper.MapperException;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.criteria.Criteria;
import org.aoju.bus.mapper.criteria.Criterion;
import org.aoju.bus.mapper.entity.Condition;
import org.aoju.bus.mapper.entity.EntityTableName;

/**
 * OGNL静态方法
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class OGNL {

    /**
     * 校验通用 Condition 的 entityClass 和当前方法是否匹配
     *
     * @param parameter
     * @param entityFullName
     * @return
     */
    public static boolean checkEntityClass(Object parameter, String entityFullName) {
        if (parameter != null && parameter instanceof Condition && Assert.isNotEmpty(entityFullName)) {
            Condition condition = (Condition) parameter;
            Class<?> entityClass = condition.getEntityClass();
            if (!entityClass.getCanonicalName().equals(entityFullName)) {
                throw new MapperException("当前 Condition 方法对应实体为:" + entityFullName
                        + ", 但是参数 Condition 中的 entityClass 为:" + entityClass.getCanonicalName());
            }
        }
        return true;
    }

    /**
     * 是否包含自定义查询列
     *
     * @param parameter
     * @return
     */
    public static boolean hasSelectColumns(Object parameter) {
        if (parameter != null && parameter instanceof Condition) {
            Condition condition = (Condition) parameter;
            if (condition.getSelectColumns() != null && condition.getSelectColumns().size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含自定义 Count 列
     *
     * @param parameter
     * @return
     */
    public static boolean hasCountColumn(Object parameter) {
        if (parameter != null && parameter instanceof Condition) {
            Condition condition = (Condition) parameter;
            return Assert.isNotEmpty(condition.getCountColumn());
        }
        return false;
    }

    /**
     * 是否包含 forUpdate
     *
     * @param parameter
     * @return
     */
    public static boolean hasForUpdate(Object parameter) {
        if (parameter != null && parameter instanceof Condition) {
            Condition condition = (Condition) parameter;
            return condition.isForUpdate();
        }
        return false;
    }

    /**
     * 不包含自定义查询列
     *
     * @param parameter
     * @return
     */
    public static boolean hasNoSelectColumns(Object parameter) {
        return !hasSelectColumns(parameter);
    }

    /**
     * 判断参数是否支持动态表名
     *
     * @param parameter
     * @return true支持，false不支持
     */
    public static boolean isDynamicParameter(Object parameter) {
        if (parameter != null && parameter instanceof EntityTableName) {
            return true;
        }
        return false;
    }

    /**
     * 判断参数是否b支持动态表名
     *
     * @param parameter
     * @return true不支持，false支持
     */
    public static boolean isNotDynamicParameter(Object parameter) {
        return !isDynamicParameter(parameter);
    }

    /**
     * 判断条件是 and 还是 or
     *
     * @param parameter
     * @return
     */
    public static String andOr(Object parameter) {
        if (parameter instanceof Criteria) {
            return ((Criteria) parameter).getAndOr();
        } else if (parameter instanceof Criterion) {
            return ((Criterion) parameter).getAndOr();
        } else if (parameter.getClass().getCanonicalName().endsWith("Criteria")) {
            return "or";
        } else {
            return "and";
        }
    }

}
