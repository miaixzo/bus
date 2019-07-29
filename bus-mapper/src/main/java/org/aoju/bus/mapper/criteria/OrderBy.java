package org.aoju.bus.mapper.criteria;

import org.aoju.bus.mapper.MapperException;
import org.aoju.bus.mapper.entity.Condition;
import org.aoju.bus.mapper.entity.EntityColumn;

import java.util.Map;

public class OrderBy {

    //属性和列对应
    protected Map<String, EntityColumn> propertyMap;
    private Condition condition;
    private Boolean isProperty;

    public OrderBy(Condition condition, Map<String, EntityColumn> propertyMap) {
        this.condition = condition;
        this.propertyMap = propertyMap;
    }

    private String property(String property) {
        if (Assert.isEmpty(property) || Assert.isEmpty(property.trim())) {
            throw new MapperException("接收的property为空！");
        }
        property = property.trim();
        if (!propertyMap.containsKey(property)) {
            throw new MapperException("当前实体类不包含名为" + property + "的属性!");
        }
        return propertyMap.get(property).getColumn();
    }

    public OrderBy orderBy(String property) {
        String column = property(property);
        if (column == null) {
            isProperty = false;
            return this;
        }
        if (Assert.isNotEmpty(condition.getOrderByClause())) {
            condition.setOrderByClause(condition.getOrderByClause() + "," + column);
        } else {
            condition.setOrderByClause(column);
        }
        isProperty = true;
        return this;
    }

    public OrderBy desc() {
        if (isProperty) {
            condition.setOrderByClause(condition.getOrderByClause() + " DESC");
            isProperty = false;
        }
        return this;
    }

    public OrderBy asc() {
        if (isProperty) {
            condition.setOrderByClause(condition.getOrderByClause() + " ASC");
            isProperty = false;
        }
        return this;
    }

}
