/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.mapper.version;

/**
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public interface NextVersion<T> {

    /**
     * 获取下一个版本
     *
     * @param nextVersionClass 下个版本对象
     * @param current          内容
     * @return 结果对象
     * @throws VersionException 异常
     */
    static Object version(String nextVersionClass, Object current) throws VersionException {
        try {
            NextVersion nextVersion = (NextVersion) Class.forName(nextVersionClass).newInstance();
            return nextVersion.nextVersion(current);
        } catch (Exception e) {
            throw new VersionException("获取下一个版本号失败!", e);
        }
    }

    /**
     * 返回下一个版本
     *
     * @param current 对象
     * @return 结果
     */
    T nextVersion(T current) throws VersionException;
}
