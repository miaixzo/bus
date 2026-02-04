/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

    /**
     * The function for retrieving the ID of a node.
     */
    private FunctionX<T, R> idKeyFun;

    /**
     * The function for retrieving the parent ID of a node.
     */
    private FunctionX<T, R> parentIdKeyFun;

    /**
     * The function for retrieving the weight of a node.
     */
    private FunctionX<T, Comparable<?>> weightKeyFun;

    /**
     * The function for retrieving the name of a node.
     */
    private FunctionX<T, CharSequence> nameKeyFun;

    /**
     * The function for retrieving the list of child nodes.
     */
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

    /**
     * Getidkey method.
     *
     * @return the String value
     */
    @Override
    public String getIdKey() {
        final FunctionX<?, ?> serFunction = getIdKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getIdKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    /**
     * Getparentidkey method.
     *
     * @return the String value
     */
    @Override
    public String getParentIdKey() {
        final FunctionX<?, ?> serFunction = getParentIdKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getParentIdKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    /**
     * Getweightkey method.
     *
     * @return the String value
     */
    @Override
    public String getWeightKey() {
        final FunctionX<?, ?> serFunction = getWeightKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getWeightKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    /**
     * Getnamekey method.
     *
     * @return the String value
     */
    @Override
    public String getNameKey() {
        final FunctionX<?, ?> serFunction = getNameKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getNameKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

    /**
     * Getchildrenkey method.
     *
     * @return the String value
     */
    @Override
    public String getChildrenKey() {
        final FunctionX<?, ?> serFunction = getChildrenKeyFun();
        if (Objects.isNull(serFunction)) {
            return super.getChildrenKey();
        }
        return LambdaKit.getFieldName(serFunction);
    }

}
