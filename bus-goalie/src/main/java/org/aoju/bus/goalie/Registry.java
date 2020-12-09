/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.goalie;

import java.util.Set;

/**
 * api registry
 *
 * @author Justubborn
 * @version 6.1.5
 * @since JDK 1.8+
 */
public interface Registry {

    /**
     * 注入服务
     *
     * @param athlete 运动员
     */
    void setAthlete(Athlete athlete);

    /**
     * 初始化
     *
     * @return 路由
     */
    Set<Assets> init();

    /**
     * 添加
     *
     * @param assets 路由
     * @return true or false
     */
    boolean add(Assets assets);

    /**
     * 删除
     *
     * @param id 路由id
     * @return true or false
     */
    boolean remove(String id);

    /**
     * 修改
     *
     * @param assets 路由
     * @return true or false
     */
    boolean amendAssets(Assets assets);

    /**
     * 刷新路由
     */
    void refresh();

}
