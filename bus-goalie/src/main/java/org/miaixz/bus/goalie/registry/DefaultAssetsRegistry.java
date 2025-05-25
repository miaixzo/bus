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
package org.miaixz.bus.goalie.registry;

import org.miaixz.bus.goalie.Assets;

/**
 * 默认路由注册实现类，基于 AbstractRegistry 提供资产（Assets）的注册、修改和查询功能
 *
 * @author Justubborn
 * @since Java 17+
 */
public class DefaultAssetsRegistry extends AbstractRegistry<Assets> implements AssetsRegistry {

    /**
     * 添加资产到注册表，使用方法名和版本号的组合作为键
     *
     * @param assets 要添加的资产对象
     */
    @Override
    public void addAssets(Assets assets) {
        super.add(assets.getMethod() + assets.getVersion(), assets);
    }

    /**
     * 修改注册表中的资产，使用方法名和版本号的组合作为键
     *
     * @param assets 要更新的资产对象
     */
    @Override
    public void amendAssets(Assets assets) {
        super.amend(assets.getMethod() + assets.getVersion(), assets);
    }

    /**
     * 根据方法名和版本号获取对应的资产
     *
     * @param method  方法名
     * @param version 版本号
     * @return 匹配的资产对象，若不存在返回 null
     */
    @Override
    public Assets getAssets(String method, String version) {
        return get(method + version);
    }

    /**
     * 初始化注册表，当前实现为空，子类可根据需要扩展
     */
    @Override
    public void init() {
        // 空实现，留给子类扩展
    }

}