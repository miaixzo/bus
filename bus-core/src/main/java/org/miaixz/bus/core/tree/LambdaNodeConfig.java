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
package org.miaixz.bus.core.tree;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.xyz.LambdaKit;

/**
 * Tree configuration properties using lambda expressions to avoid hard-coding field names.
 *
 * @param <T> The type of the object containing the properties.
 * @param <R> The return type of the ID getter.
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaNodeConfig<T, R> extends NodeConfig {

    @Serial
    private static final long serialVersionUID = 2852250050652L;

    private FunctionX<T, R> idKeyFun;
    private FunctionX<T, R> parentIdKeyFun;
    private FunctionX<T, Comparable<?>> weightKeyFun;
    private FunctionX<T, CharSequence> nameKeyFun;
    private FunctionX<T, List<T>> childrenKeyFun;

    /**
     * Gets the function for retrieving the ID.
     * 
     * @return The ID getter function.
     */
    public FunctionX<T, R> getIdKeyFun() {
        return idKeyFun;
    }

    /**
     * Sets the function for retrieving the ID.
     * 
     * @param idKeyFun The ID getter function.
     * @return this
     */
    public LambdaNodeConfig<T, R> setIdKeyFun(final FunctionX<T, R> idKeyFun) {
        this.idKeyFun = idKeyFun;
        return this;
    }

    /**
     * Gets the function for retrieving the parent ID.
     * 
     * @return The parent ID getter function.
     */
    public FunctionX<T, R> getParentIdKeyFun() {
        return parentIdKeyFun;
    }

    /**
     * Sets the function for retrieving the parent ID.
     * 
     * @param parentIdKeyFun The parent ID getter function.
     * @return this
     */
    public LambdaNodeConfig<T, R> setParentIdKeyFun(final FunctionX<T, R> parentIdKeyFun) {
        this.parentIdKeyFun = parentIdKeyFun;
        return this;
    }

    /**
     * Gets the function for retrieving the weight.
     * 
     * @return The weight getter function.
     */
    public FunctionX<T, Comparable<?>> getWeightKeyFun() {
        return weightKeyFun;
    }

    /**
     * Sets the function for retrieving the weight.
     * 
     * @param weightKeyFun The weight getter function.
     * @return this
     */
    public LambdaNodeConfig<T, R> setWeightKeyFun(final FunctionX<T, Comparable<?>> weightKeyFun) {
        this.weightKeyFun = weightKeyFun;
        return this;
    }

    /**
     * Gets the function for retrieving the node name.
     * 
     * @return The node name getter function.
     */
    public FunctionX<T, CharSequence> getNameKeyFun() {
        return nameKeyFun;
    }

    /**
     * Sets the function for retrieving the node name.
     * 
     * @param nameKeyFun The node name getter function.
     * @return this
     */
    public LambdaNodeConfig<T, R> setNameKeyFun(final FunctionX<T, CharSequence> nameKeyFun) {
        this.nameKeyFun = nameKeyFun;
        return this;
    }

    /**
     * Gets the function for retrieving the list of child nodes.
     * 
     * @return The children getter function.
     */
    public FunctionX<T, List<T>> getChildrenKeyFun() {
        return childrenKeyFun;
    }

    /**
     * Sets the function for retrieving the list of child nodes.
     * 
     * @param childrenKeyFun The children getter function.
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
