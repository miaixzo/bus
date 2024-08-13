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
package org.miaixz.bus.core.tree;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.xyz.LambdaKit;

/**
 * 树配置属性相关（使用Lambda语法） 避免对字段名称硬编码
 *
 * @param <T> 方法对象类型
 * @param <R> 返回值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaNodeConfig<T, R> extends NodeConfig {

    private static final long serialVersionUID = -1L;

    private FunctionX<T, R> idKeyFun;
    private FunctionX<T, R> parentIdKeyFun;
    private FunctionX<T, Comparable<?>> weightKeyFun;
    private FunctionX<T, CharSequence> nameKeyFun;
    private FunctionX<T, List<T>> childrenKeyFun;

    /**
     * 获取ID方法
     * 
     * @return ID方法
     */
    public FunctionX<T, R> getIdKeyFun() {
        return idKeyFun;
    }

    /**
     * 设置ID方法
     * 
     * @param idKeyFun ID方法
     * @return this
     */
    public LambdaNodeConfig<T, R> setIdKeyFun(final FunctionX<T, R> idKeyFun) {
        this.idKeyFun = idKeyFun;
        return this;
    }

    /**
     * 获取父ID方法
     * 
     * @return 父ID方法
     */
    public FunctionX<T, R> getParentIdKeyFun() {
        return parentIdKeyFun;
    }

    /**
     * 设置父ID方法
     * 
     * @param parentIdKeyFun 父ID方法
     * @return this
     */
    public LambdaNodeConfig<T, R> setParentIdKeyFun(final FunctionX<T, R> parentIdKeyFun) {
        this.parentIdKeyFun = parentIdKeyFun;
        return this;
    }

    /**
     * 设置权重方法
     * 
     * @return 权重方法
     */
    public FunctionX<T, Comparable<?>> getWeightKeyFun() {
        return weightKeyFun;
    }

    /**
     * 设置权重方法
     * 
     * @param weightKeyFun 权重方法
     * @return this
     */
    public LambdaNodeConfig<T, R> setWeightKeyFun(final FunctionX<T, Comparable<?>> weightKeyFun) {
        this.weightKeyFun = weightKeyFun;
        return this;
    }

    /**
     * 获取节点名称方法
     * 
     * @return 节点名称方法
     */
    public FunctionX<T, CharSequence> getNameKeyFun() {
        return nameKeyFun;
    }

    /**
     * 设置节点名称方法
     * 
     * @param nameKeyFun 节点名称方法
     * @return this
     */
    public LambdaNodeConfig<T, R> setNameKeyFun(final FunctionX<T, CharSequence> nameKeyFun) {
        this.nameKeyFun = nameKeyFun;
        return this;
    }

    /**
     * 获取子节点名称方法
     * 
     * @return 子节点名称方法
     */
    public FunctionX<T, List<T>> getChildrenKeyFun() {
        return childrenKeyFun;
    }

    /**
     * 设置子节点名称方法
     * 
     * @param childrenKeyFun 子节点名称方法
     * @return this
     */
    public LambdaNodeConfig<T, R> setChildrenKeyFun(final FunctionX<T, List<T>> childrenKeyFun) {
        this.childrenKeyFun = childrenKeyFun;
        return this;
    }

    @Override
    public String getIdKey() {
        final FunctionX<?, ?> serFunction = getIdKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getIdKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    @Override
    public String getParentIdKey() {
        final FunctionX<?, ?> serFunction = getParentIdKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getParentIdKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    @Override
    public String getWeightKey() {
        final FunctionX<?, ?> serFunction = getWeightKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getWeightKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    @Override
    public String getNameKey() {
        final FunctionX<?, ?> serFunction = getNameKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getNameKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    @Override
    public String getChildrenKey() {
        final FunctionX<?, ?> serFunction = getChildrenKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getChildrenKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

}
