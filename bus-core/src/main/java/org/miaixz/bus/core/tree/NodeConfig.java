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
import java.io.Serializable;

/**
 * 树配置属性相关
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NodeConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852283325162L;

    /**
     * 默认属性配置对象
     */
    public static final NodeConfig DEFAULT_CONFIG = new NodeConfig();

    /**
     * 属性名配置字段
     */
    private String idKey = "id";
    private String parentIdKey = "parentId";
    private String weightKey = "selector";
    private String nameKey = "name";
    private String childrenKey = "children";
    /**
     * 可以配置递归深度 从0开始计算 默认此配置为空,即不限制
     */
    private Integer deep;

    /**
     * 获取ID对应的名称
     *
     * @return ID对应的名称
     */
    public String getIdKey() {
        return this.idKey;
    }

    /**
     * 设置ID对应的名称
     *
     * @param idKey ID对应的名称
     * @return this
     */
    public NodeConfig setIdKey(final String idKey) {
        this.idKey = idKey;
        return this;
    }

    /**
     * 获取权重对应的名称
     *
     * @return 权重对应的名称
     */
    public String getWeightKey() {
        return this.weightKey;
    }

    /**
     * 设置权重对应的名称
     *
     * @param weightKey 权重对应的名称
     * @return this
     */
    public NodeConfig setWeightKey(final String weightKey) {
        this.weightKey = weightKey;
        return this;
    }

    /**
     * 获取节点名对应的名称
     *
     * @return 节点名对应的名称
     */
    public String getNameKey() {
        return this.nameKey;
    }

    /**
     * 设置节点名对应的名称
     *
     * @param nameKey 节点名对应的名称
     * @return this
     */
    public NodeConfig setNameKey(final String nameKey) {
        this.nameKey = nameKey;
        return this;
    }

    /**
     * 获取子点对应的名称
     *
     * @return 子点对应的名称
     */
    public String getChildrenKey() {
        return this.childrenKey;
    }

    /**
     * 设置子点对应的名称
     *
     * @param childrenKey 子点对应的名称
     * @return this
     */
    public NodeConfig setChildrenKey(final String childrenKey) {
        this.childrenKey = childrenKey;
        return this;
    }

    /**
     * 获取父节点ID对应的名称
     *
     * @return 父点对应的名称
     */
    public String getParentIdKey() {
        return this.parentIdKey;
    }

    /**
     * 设置父点对应的名称
     *
     * @param parentIdKey 父点对应的名称
     * @return this
     */
    public NodeConfig setParentIdKey(final String parentIdKey) {
        this.parentIdKey = parentIdKey;
        return this;
    }

    /**
     * 获取递归深度
     *
     * @return 递归深度
     */
    public Integer getDeep() {
        return this.deep;
    }

    /**
     * 设置递归深度
     *
     * @param deep 递归深度
     * @return this
     */
    public NodeConfig setDeep(final Integer deep) {
        this.deep = deep;
        return this;
    }

}
