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
package org.aoju.bus.mapper.criteria;

import org.aoju.bus.mapper.MapperException;
import org.aoju.bus.mapper.entity.Condition;
import org.aoju.bus.mapper.entity.EntityColumn;

import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.3.3
 * @since JDK 1.8+
 */
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
